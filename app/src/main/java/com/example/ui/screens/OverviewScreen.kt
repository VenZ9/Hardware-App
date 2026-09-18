package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hardware.HardwareMonitor
import com.example.hud.FloatingHudService
import com.example.hud.HudStateManager
import com.example.model.HudSizeMode
import com.example.ui.components.CyberCard
import com.example.ui.components.CyberGauge
import com.example.ui.components.SpecRow
import kotlin.math.roundToInt

@Composable
fun OverviewScreen(
    hardwareMonitor: HardwareMonitor,
    onNavigateToHudSettings: () -> Unit
) {
    val context = LocalContext.current
    val liveMetrics by hardwareMonitor.liveMetrics.collectAsState()
    val deviceSpec by hardwareMonitor.deviceSpec.collectAsState()
    val memoryInfo by hardwareMonitor.memoryInfo.collectAsState()
    val storageInfo by hardwareMonitor.storageInfo.collectAsState()
    val batteryInfo by hardwareMonitor.batteryInfo.collectAsState()
    val displayInfo by hardwareMonitor.displayInfo.collectAsState()
    val hudConfig by HudStateManager.hudConfig.collectAsState()

    val hasOverlayPermission = Settings.canDrawOverlays(context)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Floating HUD Control Banner
        HudBanner(
            hudEnabled = hudConfig.isEnabled,
            hasPermission = hasOverlayPermission,
            onToggleHud = { enable ->
                if (enable) {
                    if (hasOverlayPermission) {
                        FloatingHudService.start(context)
                    } else {
                        requestOverlayPermission(context)
                    }
                } else {
                    FloatingHudService.stop(context)
                }
            },
            onRequestPermission = { requestOverlayPermission(context) },
            onConfigure = onNavigateToHudSettings
        )

        // Live Performance Dials Row
        Text(
            text = "LIVE PERFORMANCE METRICS",
            color = Color(0xFF94A3B8),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // FPS Gauge
            CyberGauge(
                title = "LIVE FPS",
                valueText = "${liveMetrics.fps.toInt()}",
                unitText = "HZ",
                progress = liveMetrics.fps / 120f,
                primaryColor = Color(0xFF00FF9D),
                secondaryColor = Color(0xFF00F0FF),
                size = 110.dp,
                modifier = Modifier.weight(1f)
            )

            // CPU Gauge
            CyberGauge(
                title = "CPU LOAD",
                valueText = "${liveMetrics.cpuUsagePercent}%",
                unitText = "ACTIVE",
                progress = liveMetrics.cpuUsagePercent / 100f,
                primaryColor = Color(0xFF00F0FF),
                secondaryColor = Color(0xFF38BDF8),
                size = 110.dp,
                modifier = Modifier.weight(1f)
            )

            // GPU Gauge
            CyberGauge(
                title = "GPU LOAD",
                valueText = "${liveMetrics.gpuUsagePercent}%",
                unitText = "RENDER",
                progress = liveMetrics.gpuUsagePercent / 100f,
                primaryColor = Color(0xFFFF5E3A),
                secondaryColor = Color(0xFFFFB800),
                size = 110.dp,
                modifier = Modifier.weight(1f)
            )
        }

        // Notification HUD Quick Control Tip
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF0B132B),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E293B)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "Notification Tip",
                        tint = Color(0xFF00F0FF),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Notification HUD Controls",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Toggle HUD visibility, cycle sizes, and stop directly from notifications while playing any game!",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        // Device & OS Card
        CyberCard(
            title = "Device & Platform",
            icon = Icons.Default.PhoneAndroid,
            accentColor = Color(0xFF38BDF8)
        ) {
            SpecRow(label = "Model", value = "${deviceSpec.manufacturer} ${deviceSpec.model}")
            SpecRow(label = "Android Release", value = "Android ${deviceSpec.androidVersion} (API ${deviceSpec.sdkInt})")
            SpecRow(label = "Board / Platform", value = deviceSpec.board, isMonospace = true)
            SpecRow(label = "Security Patch", value = deviceSpec.securityPatch)
            SpecRow(label = "Kernel", value = deviceSpec.kernelVersion, isMonospace = true)
        }

        // Memory & Storage Card
        CyberCard(
            title = "RAM & Storage",
            icon = Icons.Default.Storage,
            accentColor = Color(0xFF8B5CF6)
        ) {
            val ramUsedGb = (memoryInfo.usedRamMb / 1024f * 10).roundToInt() / 10f
            val ramTotalGb = (memoryInfo.totalRamMb / 1024f * 10).roundToInt() / 10f
            SpecRow(
                label = "RAM Usage",
                value = "$ramUsedGb / $ramTotalGb GB (${memoryInfo.usedPercent}%)",
                valueColor = if (memoryInfo.usedPercent > 85) Color(0xFFFF5E3A) else Color(0xFF00FF9D)
            )

            // RAM Progress Bar
            ProgressBar(progress = memoryInfo.usedPercent / 100f, color = Color(0xFF8B5CF6))

            Spacer(modifier = Modifier.height(6.dp))

            val storageUsedGb = (storageInfo.usedBytes / (1024.0 * 1024.0 * 1024.0) * 10).roundToInt() / 10f
            val storageTotalGb = (storageInfo.totalBytes / (1024.0 * 1024.0 * 1024.0) * 10).roundToInt() / 10f
            SpecRow(
                label = "Internal Storage",
                value = "$storageUsedGb / $storageTotalGb GB (${storageInfo.usedPercent}%)"
            )

            // Storage Progress Bar
            ProgressBar(progress = storageInfo.usedPercent / 100f, color = Color(0xFF38BDF8))
        }

        // Battery & Display Quick Card
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Battery Tile
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF0F172A))
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp)),
                color = Color(0xFF0F172A)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.BatteryChargingFull,
                            contentDescription = "Battery",
                            tint = Color(0xFF00FF9D),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Battery",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "${batteryInfo.levelPercent}%",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "${batteryInfo.temperatureC.toInt()}°C • ${batteryInfo.status}",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }
            }

            // Display Tile
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF0F172A))
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp)),
                color = Color(0xFF0F172A)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tv,
                            contentDescription = "Display",
                            tint = Color(0xFF00F0FF),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Display",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "${displayInfo.currentRefreshRate.toInt()} Hz",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = displayInfo.resolution,
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun HudBanner(
    hudEnabled: Boolean,
    hasPermission: Boolean,
    onToggleHud: (Boolean) -> Unit,
    onRequestPermission: () -> Unit,
    onConfigure: () -> Unit
) {
    val shape = RoundedCornerShape(22.dp)
    val accent = if (hudEnabled) Color(0xFF00FF9D) else Color(0xFF00F0FF)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(
                1.5.dp,
                Brush.horizontalGradient(
                    listOf(
                        accent.copy(alpha = 0.6f),
                        Color(0xFF1E293B),
                        accent.copy(alpha = 0.2f)
                    )
                ),
                shape
            ),
        color = Color(0xFF0A0F1D)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(if (hudEnabled) Color(0xFF00FF9D) else Color(0xFF64748B))
                    )
                    Column {
                        Text(
                            text = if (hudEnabled) "FLOATING HUD ACTIVE" else "FLOATING GAMING HUD",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = if (hudEnabled) "Floating live FPS, CPU & GPU over games" else "Overlay performance meter over any app/game",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                }

                Switch(
                    checked = hudEnabled,
                    onCheckedChange = onToggleHud,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF00FF9D),
                        uncheckedThumbColor = Color(0xFF94A3B8),
                        uncheckedTrackColor = Color(0xFF1E293B)
                    ),
                    modifier = Modifier.testTag("toggle_floating_hud_switch")
                )
            }

            if (!hasPermission) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x33FFB800))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Permission Needed",
                            tint = Color(0xFFFFB800),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Overlay Permission Required",
                            color = Color(0xFFFFE082),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        text = "GRANT",
                        color = Color(0xFFFFB800),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0x44FFB800))
                            .clickable(onClick = onRequestPermission)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onConfigure,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8))
                ) {
                    Icon(
                        imageVector = Icons.Default.Widgets,
                        contentDescription = "Customize HUD",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "HUD Studio", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { onToggleHud(!hudEnabled) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (hudEnabled) Color(0xFFEF4444) else Color(0xFF00E5FF),
                        contentColor = if (hudEnabled) Color.White else Color(0xFF0F172A)
                    )
                ) {
                    Icon(
                        imageVector = if (hudEnabled) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = if (hudEnabled) "Stop" else "Launch",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (hudEnabled) "Stop Overlay" else "Launch HUD",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgressBar(progress: Float, color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(Color(0xFF1E293B))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
        )
    }
}

private fun requestOverlayPermission(context: Context) {
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
