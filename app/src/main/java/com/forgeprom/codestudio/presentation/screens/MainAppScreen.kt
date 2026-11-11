package com.forgeprom.codestudio.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.forgeprom.codestudio.core.ui.ObsidianDarkBg
import com.forgeprom.codestudio.core.ui.ObsidianDarkSurface
import com.forgeprom.codestudio.core.ui.PurpleBrandAccent
import com.forgeprom.codestudio.core.ui.PurpleBrandPrimary
import com.forgeprom.codestudio.data.local.model.ProjectEntity
import com.forgeprom.codestudio.data.remote.CloudApiService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    projects: List<ProjectEntity>,
    availableRamMB: Long,
    serverStatus: CloudApiService.ServerHealthResult?,
    isCheckingServer: Boolean,
    currentServerUrl: String,
    onCheckServer: () -> Unit,
    onUpdateServerUrl: (String) -> Unit,
    onNewProject: (String, String) -> Unit,
    onSelectProject: (ProjectEntity) -> Unit
) {
    var showNewProjectDialog by remember { mutableStateOf(false) }
    var showServerDialog by remember { mutableStateOf(false) }
    var selectedProjectForDetails by remember { mutableStateOf<ProjectEntity?>(null) }

    var newProjectName by remember { mutableStateOf("") }
    var newProjectIdea by remember { mutableStateOf("") }
    var serverUrlInput by remember(currentServerUrl) { mutableStateOf(currentServerUrl) }

    val isOnline = serverStatus?.isConnected == true

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(PurpleBrandPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "F",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp
                            )
                        }
                        Column {
                            Text(
                                text = "Forgeprom Code Studio",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Text(
                                text = if (isOnline) "متصل بالخادم السحابي + محرك محلي" else "بيئة محلية 100% (Local-First)",
                                fontSize = 11.sp,
                                color = if (isOnline) Color(0xFF34D399) else Color(0xFFA0AEC0)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ObsidianDarkSurface
                ),
                actions = {
                    Surface(
                        onClick = { showServerDialog = true },
                        color = if (isOnline) Color(0xFF065F46).copy(alpha = 0.5f) else Color(0xFF1E3A8A).copy(alpha = 0.5f),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (isOnline) Icons.Default.CloudDone else Icons.Default.CloudOff,
                                contentDescription = "حالة الاتصال بالخادم",
                                tint = if (isOnline) Color(0xFF34D399) else Color(0xFF60A5FA),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (isCheckingServer) "جارٍ الفحص..." else if (isOnline) "متصل بالخادم" else "محلي 100%",
                                color = if (isOnline) Color(0xFFA7F3D0) else Color(0xFF93C5FD),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showNewProjectDialog = true },
                containerColor = PurpleBrandPrimary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "مشروع جديد")
            }
        },
        containerColor = ObsidianDarkBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Cloud & Hardware Status Card
            Card(
                colors = CardDefaults.cardColors(containerColor = ObsidianDarkSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "فاحص العتاد والاتصال بالخادم",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "الرام المتاح: $availableRamMB MB | حماية OOM نشطة",
                                color = Color(0xFF34D399),
                                fontSize = 11.sp
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedButton(
                                onClick = onCheckServer,
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("فحص الخادم", fontSize = 11.sp)
                            }
                            IconButton(
                                onClick = { showServerDialog = true },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Settings, contentDescription = "إعدادات الخادم", tint = PurpleBrandAccent)
                            }
                        }
                    }

                    if (serverStatus != null) {
                        Text(
                            text = serverStatus.message,
                            color = if (serverStatus.isConnected) Color(0xFFA7F3D0) else Color(0xFF93C5FD),
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Projects Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "المشاريع الهندسية النشطة (${projects.size})",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 15.sp
                )
            }

            // Projects List
            if (projects.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "لا توجد مشاريع حتى الآن",
                            color = Color.LightGray,
                            fontSize = 14.sp
                        )
                        Button(
                            onClick = { showNewProjectDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = PurpleBrandPrimary)
                        ) {
                            Text("إنشاء أول مشروع")
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(projects, key = { it.id }) { project ->
                        Card(
                            onClick = {
                                selectedProjectForDetails = project
                                onSelectProject(project)
                            },
                            colors = CardDefaults.cardColors(containerColor = ObsidianDarkSurface),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = project.name,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 14.sp
                                    )
                                    Surface(
                                        color = PurpleBrandPrimary.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = project.targetPlatform,
                                            color = PurpleBrandAccent,
                                            fontSize = 10.sp,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = project.description,
                                    color = Color(0xFFA0AEC0),
                                    fontSize = 12.sp,
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // New Project Dialog (Supports both Cloud AI analysis & Local Offline generation)
    if (showNewProjectDialog) {
        AlertDialog(
            onDismissRequest = { showNewProjectDialog = false },
            title = { Text("إنشاء مشروع وتحليل هندسي جديد", color = Color.White, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newProjectName,
                        onValueChange = { newProjectName = it },
                        label = { Text("اسم المشروع") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newProjectIdea,
                        onValueChange = { newProjectIdea = it },
                        label = { Text("وصف الفكرة الحرة باللغة الطبيعية") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = if (isOnline) "سيتم تحليل الفكرة عبر الخادم السحابي المتصل وحفظها محلياً."
                        else "سيتم تحليل الفكرة وتوليد البرومبت عبر المحرك المحلي (بدون إنترنت).",
                        color = if (isOnline) Color(0xFF34D399) else Color(0xFF93C5FD),
                        fontSize = 11.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newProjectName.isNotBlank()) {
                            onNewProject(newProjectName, newProjectIdea)
                            newProjectName = ""
                            newProjectIdea = ""
                            showNewProjectDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PurpleBrandPrimary)
                ) {
                    Text("تحليل وحفظ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewProjectDialog = false }) {
                    Text("إلغاء", color = Color.Gray)
                }
            },
            containerColor = ObsidianDarkSurface
        )
    }

    // Server Configuration Dialog
    if (showServerDialog) {
        AlertDialog(
            onDismissRequest = { showServerDialog = false },
            title = { Text("إعدادات الاتصال بالخادم السحابي", color = Color.White, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "يمكن للتطبيق العمل 100% بدون إنترنت، أو الارتباط بالخادم السحابي لتحليل الأفكار والمزامنة:",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                    OutlinedTextField(
                        value = serverUrlInput,
                        onValueChange = { serverUrlInput = it },
                        label = { Text("رابط الخادم (Server URL)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateServerUrl(serverUrlInput)
                        showServerDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PurpleBrandPrimary)
                ) {
                    Text("حفظ وفحص الاتصال")
                }
            },
            dismissButton = {
                TextButton(onClick = { showServerDialog = false }) {
                    Text("إغلاق", color = Color.Gray)
                }
            },
            containerColor = ObsidianDarkSurface
        )
    }

    // Project Prompt Details Dialog
    selectedProjectForDetails?.let { proj ->
        AlertDialog(
            onDismissRequest = { selectedProjectForDetails = null },
            title = { Text(proj.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "الوصف: ${proj.description}", color = Color.LightGray, fontSize = 12.sp)
                    HorizontalDivider(color = Color.DarkGray)
                    Text(
                        text = proj.currentPromptText ?: "البرومبت الهندسي جاهز للتوليد والتصدير.",
                        color = Color(0xFFD6BCFA),
                        fontSize = 11.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedProjectForDetails = null },
                    colors = ButtonDefaults.buttonColors(containerColor = PurpleBrandPrimary)
                ) {
                    Text("حسناً")
                }
            },
            containerColor = ObsidianDarkSurface
        )
    }
}
