package com.forgeprom.codestudio.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * خدمة الاتصال بالخوادم السحابية (CloudApiService)
 * تتصل بخادم Forgeprom السحابي أو واجهات الذكاء الاصطناعي عند توفر الإنترنت وبموافقة المستخدم،
 * مع العودة الفورية التلقائية للوضع المحلي (Local-First Fallback) عند انقطاع الشبكة أو تفعيل وضع الطيران.
 */
class CloudApiService(
    private var baseUrl: String = DEFAULT_SERVER_URL
) {

    companion object {
        // رابط الخادم السحابي الافتراضي للتطبيق
        const val DEFAULT_SERVER_URL = "https://ais-pre-wg5ba7jzr4vldg2v6hfqek-381610229333.europe-west3.run.app"
    }

    data class ServerHealthResult(
        val isConnected: Boolean,
        val serverMode: String,
        val latencyMs: Long,
        val message: String
    )

    fun setServerUrl(url: String) {
        baseUrl = url.trimEnd('/')
    }

    fun getServerUrl(): String = baseUrl

    /**
     * فحص الاتصال الفعلي بالخادم السحابي (/api/health)
     */
    suspend fun checkServerConnection(): ServerHealthResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        try {
            val url = URL("$baseUrl/api/health")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 6000
                readTimeout = 6000
                setRequestProperty("Accept", "application/json")
            }

            val code = conn.responseCode
            val latency = System.currentTimeMillis() - startTime
            if (code in 200..299) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val responseText = reader.use { it.readText() }
                val json = JSONObject(responseText)
                val mode = json.optString("mode", "Forgeprom Cloud Engine")
                ServerHealthResult(
                    isConnected = true,
                    serverMode = mode,
                    latencyMs = latency,
                    message = "متصل بالخادم السحابي بنجاح (${latency}ms)"
                )
            } else {
                ServerHealthResult(
                    isConnected = false,
                    serverMode = "Local-First Offline",
                    latencyMs = latency,
                    message = "الخادم استجاب برمز $code — يعمل التطبيق بالوضع المحلي"
                )
            }
        } catch (e: Exception) {
            ServerHealthResult(
                isConnected = false,
                serverMode = "Local-First Offline",
                latencyMs = -1,
                message = "لا يوجد اتصال بالخادم (وضع محلي 100% آمن)"
            )
        }
    }

    /**
     * تحليل الفكرة وتوليد البرومبت الهندسي عبر الخادم السحابي مع بديل محلي فوري عند عدم الاتصال
     */
    suspend fun analyzeAndGeneratePrompt(
        idea: String,
        targetPlatform: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/api/analyze-idea")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 12000
                readTimeout = 20000
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            }

            val payload = JSONObject().apply {
                put("idea", idea)
                put("projectType", "full_app")
                put("platform", targetPlatform)
            }

            OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { writer ->
                writer.write(payload.toString())
                writer.flush()
            }

            if (conn.responseCode in 200..299) {
                val responseText = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
                val json = JSONObject(responseText)
                val problem = json.optString("problem", idea)
                val primaryGoal = json.optString("primaryGoal", "")
                val mustHave = json.optJSONObject("functionalReqs")?.optJSONArray("mustHave")

                val sb = StringBuilder()
                sb.appendLine("# البرومبت الهندسي المُولّد (عبر خادم Forgeprom السحابي)")
                sb.appendLine("## 1. المشكلة الأساسية")
                sb.appendLine(problem)
                sb.appendLine("\n## 2. الهدف الرئيسي")
                sb.appendLine(primaryGoal)
                sb.appendLine("\n## 3. المتطلبات الوظيفية الإلزامية (Must-Have)")
                if (mustHave != null) {
                    for (i in 0 until mustHave.length()) {
                        sb.appendLine("- ${mustHave.getString(i)}")
                    }
                }
                sb.appendLine("\n## 4. المنصة المستهدفة")
                sb.appendLine(targetPlatform)
                Result.success(sb.toString())
            } else {
                Result.success(generateLocalFallbackPrompt(idea, targetPlatform))
            }
        } catch (e: Exception) {
            // في حال عدم توفر الإنترنت أو تفعيل وضع الطيران يعمل المحرك المحلي تلقائياً
            Result.success(generateLocalFallbackPrompt(idea, targetPlatform))
        }
    }

    private fun generateLocalFallbackPrompt(idea: String, targetPlatform: String): String {
        return """
# البرومبت الهندسي القياسي (توليد محلي 100% — Offline Local Engine)
- **المنصة المستهدفة:** $targetPlatform
- **وصف الفكرة:** $idea

## المتطلبات المعمارية المعتمدة:
1. تطبيق Clean Architecture (Presentation, Domain, Data).
2. قاعدة بيانات محلية Room Database تعمل 100% دون إنترنت.
3. واجهة عربية أصيلة تدعم اتجاه RTL والوضع الداكن (Dark Mode).
4. إدارة ذاكرة صارمة لمنع استنزاف الـ RAM أو حدوث OOM.
        """.trimIndent()
    }
}
