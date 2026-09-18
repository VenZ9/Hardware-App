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
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import com.example.hud.FloatingHudView
import com.example.hud.HudStateManager
import com.example.model.HudSizeMode
import com.example.ui.components.CyberCard
import com.example.ui.components.SpecRow
import kotlin.math.roundToInt

@Composable
fun HudSettingsScreen(hardwareMonitor: HardwareMonitor) {
    val context = LocalContext.current
    val hudConfig by HudStateManager.hudConfig.collectAsState()
    val hasOverlayPermission = Settings.canDrawOverlays(context)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Master Activation Switch
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF0F172A),
            border = androidx.compose.foundation.BorderStroke(
                1.5.dp,
                if (hudConfig.isEnabled) Color(0xFF00FF9D) else Color(0xFF1E293B)
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "FLOATING GAMING HUD",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = if (hudConfig.isEnabled) "HUD is running on top of all apps" else "Turn on to float over games",
                        color = if (hudConfig.isEnabled) Color(0xFF00FF9D) else Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                }

                Switch(
                    checked = hudConfig.isEnabled,
                    onCheckedChange = { enable ->
                        if (enable) {
                            if (hasOverlayPermission) {
                                FloatingHudService.start(context)
                            } else {
                                openOverlaySettings(context)
                            }
                        } else {
                            FloatingHudService.stop(context)
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF00FF9D),
                        uncheckedThumbColor = Color(0xFF94A3B8),
                        uncheckedTrackColor = Color(0xFF1E293B)
                    ),
                    modifier = Modifier.testTag("master_hud_switch")
                )
            }
        }

        // Overlay Permission Status
        CyberCard(
            title = "System Overlay Permission",
            icon = Icons.Default.Security,
            accentColor = if (hasOverlayPermission) Color(0xFF00FF9D) else Color(0xFFFFB800)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (hasOverlayPermission) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = "Permission Status",
                        tint = if (hasOverlayPermission) Color(0xFF00FF9D) else Color(0xFFFFB800),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = if (hasOverlayPermission) "Permission Granted" else "Draw Over Other Apps Required",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (!hasOverlayPermission) {
                    Button(
                        onClick = { openOverlaySettings(context) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB800)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = "Grant", color = Color(0xFF0F172A), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Notification Controls Explanation
        CyberCard(
            title = "Notification Controls Guide",
            icon = Icons.Default.NotificationsActive,
            accentColor = Color(0xFF00F0FF)
        ) {
            Text(
                text = "When the Floating HUD is active, a persistent high-priority notification is placed in your Android notification shade with quick actions:",
                color = Color(0xFFCBD5E1),
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            NotificationTipItem(
                label = "Hide / Show HUD",
                desc = "Temporarily hides or unhides the overlay without stopping the service. Perfect during game cutscenes!"
            )
            NotificationTipItem(
                label = "Cycle Size",
                desc = "Switches between Compact (micro pill), Normal (balanced), and Expanded (with real-time graphs)."
            )
            NotificationTipItem(
                label = "Stop HUD",
                desc = "Immediately closes the overlay and terminates the background monitor service."
            )
        }

        // Size Mode Picker
        CyberCard(
            title = "HUD Size Mode",
            icon = Icons.Default.AspectRatio,
            accentColor = Color(0xFF8B5CF6)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HudSizeMode.entries.forEach { mode ->
                    val isSelected = (hudConfig.sizeMode == mode)
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) Color(0xFF8B5CF6) else Color(0xFF1E293B))
                            .clickable {
                                HudStateManager.updateConfig { it.copy(sizeMode = mode, isMinimized = false) }
                            }
                            .padding(vertical = 10.dp),
                        color = if (isSelected) Color(0xFF8B5CF6) else Color(0xFF1E293B)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = mode.label,
                                color = if (isSelected) Color.White else Color(0xFF94A3B8),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Transparency / Opacity Slider
        CyberCard(
            title = "HUD Opacity (${(hudConfig.opacity * 100).roundToInt()}%)",
            icon = Icons.Default.Tune,
            accentColor = Color(0xFF38BDF8)
        ) {
            Slider(
                value = hudConfig.opacity,
                onValueChange = { newAlpha ->
                    HudStateManager.updateConfig { it.copy(opacity = newAlpha) }
                },
                valueRange = 0.4f..1.0f,
                steps = 11,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF00F0FF),
                    activeTrackColor = Color(0xFF00F0FF),
                    inactiveTrackColor = Color(0xFF1E293B)
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "Lower opacity ensures the HUD doesn't obstruct in-game mini-maps or UI elements.",
                color = Color(0xFF64748B),
                fontSize = 11.sp
            )
        }

        // Movement & Resizing Tips
        CyberCard(
            title = "Move & Resize Gestures",
            icon = Icons.Default.OpenWith,
            accentColor = Color(0xFF00FF9D)
        ) {
            SpecRow(
                label = "Drag to Move",
                value = "Touch & drag anywhere on HUD to position it anywhere on screen",
                valueColor = Color(0xFF00FF9D)
            )
            SpecRow(
                label = "Drag to Resize",
                value = "Drag the bottom-right cyan icon to dynamically stretch dimensions",
                valueColor = Color(0xFF38BDF8)
            )
            SpecRow(
                label = "Lock Position",
                value = if (hudConfig.isLocked) "LOCKED (touches pass through/prevent accidental moves)" else "UNLOCKED",
                valueColor = if (hudConfig.isLocked) Color(0xFFFFB800) else Color(0xFF94A3B8)
            )
            SpecRow(
                label = "Minimize Bubble",
                value = "Tap the minimize button to collapse into a 54dp circular FPS badge",
                valueColor = Color(0xFFCBD5E1)
            )
        }

        // Live In-App HUD Preview Box
        Text(
            text = "LIVE HUD PREVIEW",
            color = Color(0xFF94A3B8),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF030712))
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(20.dp))
                .padding(20.dp),
            color = Color(0xFF030712)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                FloatingHudView(
                    hardwareMonitor = hardwareMonitor,
                    onDrag = { _, _ -> },
                    onResize = { _, _ -> },
                    onClose = { }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun NotificationTipItem(label: String, desc: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(Color(0xFF00F0FF))
                .align(Alignment.CenterVertically)
        )
        Column {
            Text(text = label, color = Color(0xFF00F0FF), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(text = desc, color = Color(0xFF94A3B8), fontSize = 11.sp, lineHeight = 15.sp)
        }
    }
}

private fun openOverlaySettings(context: Context) {
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
