package com.example

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.hardware.HardwareMonitor
import com.example.hud.FloatingHudService
import com.example.hud.HudStateManager
import com.example.ui.screens.BatteryThermalsScreen
import com.example.ui.screens.CpuGpuScreen
import com.example.ui.screens.DisplaySensorsScreen
import com.example.ui.screens.HudSettingsScreen
import com.example.ui.screens.OverviewScreen
import com.example.ui.theme.MyApplicationTheme

enum class NavTab(val title: String, val icon: ImageVector) {
    OVERVIEW("Overview", Icons.Default.Speed),
    CPU_GPU("CPU/GPU", Icons.Default.Memory),
    BATTERY("Battery", Icons.Default.BatteryChargingFull),
    SENSORS("Sensors", Icons.Default.Sensors),
    HUD_STUDIO("HUD Studio", Icons.Default.Widgets)
}

class MainActivity : ComponentActivity() {

    private lateinit var hardwareMonitor: HardwareMonitor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        hardwareMonitor = HardwareMonitor.getInstance(this)

        setContent {
            MyApplicationTheme {
                MainAppContent(hardwareMonitor = hardwareMonitor)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(hardwareMonitor: HardwareMonitor) {
    val context = LocalContext.current
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val hudConfig by HudStateManager.hudConfig.collectAsState()
    val liveMetrics by hardwareMonitor.liveMetrics.collectAsState()

    // Request notification permission for Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!permissionGranted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFF070A12),
        topBar = {
            Surface(
                color = Color(0xFF0F172A),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // App Logo & Title
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF00F0FF), Color(0xFF10B981))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = "Logo",
                                tint = Color(0xFF090D16),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "HARDWARE HUD",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (hudConfig.isEnabled) Color(0xFF00FF9D) else Color(0xFF64748B))
                                )
                                Text(
                                    text = if (hudConfig.isEnabled) "HUD ACTIVE • ${liveMetrics.fps.toInt()} FPS" else "HUD READY",
                                    color = if (hudConfig.isEnabled) Color(0xFF00FF9D) else Color(0xFF94A3B8),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    // Quick HUD toggle in Top Bar
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable {
                                if (hudConfig.isEnabled) {
                                    FloatingHudService.stop(context)
                                } else {
                                    if (Settings.canDrawOverlays(context)) {
                                        FloatingHudService.start(context)
                                    } else {
                                        openOverlayPermissionSettings(context)
                                    }
                                }
                            }
                            .testTag("top_bar_hud_toggle"),
                        color = if (hudConfig.isEnabled) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFF1E293B),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (hudConfig.isEnabled) Color(0xFF10B981) else Color(0xFF334155)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (hudConfig.isEnabled) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = "Toggle HUD",
                                tint = if (hudConfig.isEnabled) Color(0xFF00FF9D) else Color(0xFF00F0FF),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (hudConfig.isEnabled) "STOP" else "HUD",
                                color = if (hudConfig.isEnabled) Color(0xFF00FF9D) else Color(0xFF00F0FF),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                color = Color(0xFF0B101E),
                border = androidx.compose.foundation.BorderStroke(0.8.dp, Color(0xFF1E293B))
            ) {
                NavigationBar(
                    containerColor = Color.Transparent,
                    modifier = Modifier.navigationBarsPadding(),
                    tonalElevation = 0.dp
                ) {
                    NavTab.entries.forEachIndexed { index, tab ->
                        val isSelected = (selectedTab == index)
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { selectedTab = index },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.title,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF090D16),
                                selectedTextColor = Color(0xFF00F0FF),
                                indicatorColor = Color(0xFF00F0FF),
                                unselectedIconColor = Color(0xFF64748B),
                                unselectedTextColor = Color(0xFF64748B)
                            ),
                            modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF070A12))
        ) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ScreenTransition"
            ) { tabIndex ->
                when (tabIndex) {
                    0 -> OverviewScreen(
                        hardwareMonitor = hardwareMonitor,
                        onNavigateToHudSettings = { selectedTab = 4 }
                    )
                    1 -> CpuGpuScreen(hardwareMonitor = hardwareMonitor)
                    2 -> BatteryThermalsScreen(hardwareMonitor = hardwareMonitor)
                    3 -> DisplaySensorsScreen(hardwareMonitor = hardwareMonitor)
                    4 -> HudSettingsScreen(hardwareMonitor = hardwareMonitor)
                }
            }
        }
    }
}

private fun openOverlayPermissionSettings(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}
