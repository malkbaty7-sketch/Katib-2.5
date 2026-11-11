package com.forgeprom.codestudio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import com.forgeprom.codestudio.core.hardware.HardwareProfiler
import com.forgeprom.codestudio.core.ui.ForgepromTheme
import com.forgeprom.codestudio.data.local.db.AppDatabase
import com.forgeprom.codestudio.data.local.model.ProjectEntity
import com.forgeprom.codestudio.data.remote.CloudApiService
import com.forgeprom.codestudio.presentation.screens.MainAppScreen
import kotlinx.coroutines.launch
import java.util.UUID

class MainActivity : ComponentActivity() {

    private lateinit var database: AppDatabase
    private lateinit var hardwareProfiler: HardwareProfiler
    private val cloudApiService = CloudApiService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        database = AppDatabase.getInstance(this)
        hardwareProfiler = HardwareProfiler(this)

        // Seed initial showcase projects if empty
        lifecycleScope.launch {
            val dao = database.projectDao()
            val initial = dao.getProjectById("forgeprom-seed-1")
            if (initial == null) {
                dao.insertOrUpdate(
                    ProjectEntity(
                        id = "forgeprom-seed-1",
                        name = "نظام نقاط البيع المحلي المتكامل (ForgePOS)",
                        description = "تطبيق POS مخصص لإدارة المبيعات والمخزون وطباعة الفواتير عبر البلوتوث محلياً 100% دون إنترنت مع مزامنة سحابية اختيارية.",
                        type = "NEW_APP",
                        targetPlatform = "ANDROID_KOTLIN",
                        rawIdea = "تطبيق أندرويد متكامل لنقاط البيع السريعة بدون إنترنت",
                        currentPromptText = "# البرومبت الهندسي المعتمد لـ ForgePOS\n- المعمارية: Clean Architecture + Room DB\n- الميزات: كاشير سريع، باركود ML Kit، طباعة حرارية ESC/POS دون إنترنت.",
                        isPinned = true
                    )
                )
                dao.insertOrUpdate(
                    ProjectEntity(
                        id = "forgeprom-seed-2",
                        name = "نظام إدارة العيادات الطبية الخاصة (ForgeClinic)",
                        description = "منصة إدارة ملفات المرضى والمواعيد والوصفات الطبية مع تشفير محلي صارم وحماية بيانات الخصوصية وفق HIPAA.",
                        type = "NEW_APP",
                        targetPlatform = "FLUTTER",
                        rawIdea = "إدارة ملفات المرضى وتاريخ الزيارات الطبية محلياً على التابلت",
                        currentPromptText = "# البرومبت الهندسي المعتمد لـ ForgeClinic\n- تشفير AES-256 محلي، سجل زيارات مرضى، وتصدير تقارير طبية PDF باللغة العربية.",
                        isPinned = false
                    )
                )
            }
        }

        setContent {
            ForgepromTheme {
                val projects by database.projectDao().getActiveProjectsFlow().collectAsState(initial = emptyList())
                val availableRam = remember { hardwareProfiler.getAvailableRamMB() }
                var serverStatus by remember { mutableStateOf<CloudApiService.ServerHealthResult?>(null) }
                var isCheckingServer by remember { mutableStateOf(false) }
                var currentServerUrl by remember { mutableStateOf(cloudApiService.getServerUrl()) }

                fun checkServer() {
                    lifecycleScope.launch {
                        isCheckingServer = true
                        serverStatus = cloudApiService.checkServerConnection()
                        isCheckingServer = false
                    }
                }

                // فحص تلقائي أولي لحالة الخادم عند التشغيل (مع بقاء العمل المحلي مضموناً 100%)
                LaunchedEffect(Unit) {
                    checkServer()
                }

                MainAppScreen(
                    projects = projects,
                    availableRamMB = availableRam,
                    serverStatus = serverStatus,
                    isCheckingServer = isCheckingServer,
                    currentServerUrl = currentServerUrl,
                    onCheckServer = { checkServer() },
                    onUpdateServerUrl = { newUrl ->
                        cloudApiService.setServerUrl(newUrl)
                        currentServerUrl = cloudApiService.getServerUrl()
                        checkServer()
                    },
                    onNewProject = { name, idea ->
                        lifecycleScope.launch {
                            val generatedPrompt = cloudApiService.analyzeAndGeneratePrompt(
                                idea = idea.ifBlank { name },
                                targetPlatform = "ANDROID_KOTLIN"
                            ).getOrNull()

                            database.projectDao().insertOrUpdate(
                                ProjectEntity(
                                    id = UUID.randomUUID().toString(),
                                    name = name,
                                    description = idea.ifBlank { name }.take(140),
                                    type = "NEW_APP",
                                    targetPlatform = "ANDROID_KOTLIN",
                                    rawIdea = idea,
                                    currentPromptText = generatedPrompt
                                )
                            )
                        }
                    },
                    onSelectProject = { /* Project dialog handled inside MainAppScreen */ }
                )
            }
        }
    }
}
