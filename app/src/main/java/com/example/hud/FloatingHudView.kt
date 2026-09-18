package com.example.hud

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.ZoomInMap
import androidx.compose.material.icons.filled.ZoomOutMap
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hardware.HardwareMonitor
import com.example.model.HudConfig
import com.example.model.HudSizeMode
import kotlin.math.roundToInt

@Composable
fun FloatingHudView(
    hardwareMonitor: HardwareMonitor,
    onDrag: (dx: Float, dy: Float) -> Unit,
    onResize: (dw: Float, dh: Float) -> Unit,
    onClose: () -> Unit
) {
    val liveMetrics by hardwareMonitor.liveMetrics.collectAsState()
    val hudConfig by HudStateManager.hudConfig.collectAsState()

    if (!hudConfig.isVisible) return

    val hudAlpha by animateFloatAsState(
        targetValue = hudConfig.opacity,
        animationSpec = tween(200),
        label = "hudAlpha"
    )

    if (hudConfig.isMinimized) {
        // Ultra-compact bubble mode
        MinimizedBubble(
            fps = liveMetrics.fps,
            alpha = hudAlpha,
            onExpand = { HudStateManager.toggleMinimized() },
            onDrag = onDrag
        )
    } else {
        when (hudConfig.sizeMode) {
            HudSizeMode.COMPACT -> CompactHudCard(
                fps = liveMetrics.fps,
                cpu = liveMetrics.cpuUsagePercent,
                gpu = liveMetrics.gpuUsagePercent,
                alpha = hudAlpha,
                isLocked = hudConfig.isLocked,
                onDrag = onDrag,
                onResize = onResize,
                onClose = onClose
            )
            HudSizeMode.NORMAL -> NormalHudCard(
                fps = liveMetrics.fps,
                cpu = liveMetrics.cpuUsagePercent,
                gpu = liveMetrics.gpuUsagePercent,
                ramUsedMb = liveMetrics.ramUsedMb,
                tempC = liveMetrics.batteryTempC,
                alpha = hudAlpha,
                isLocked = hudConfig.isLocked,
                onDrag = onDrag,
                onResize = onResize,
                onClose = onClose
            )
            HudSizeMode.EXPANDED -> ExpandedHudCard(
                fps = liveMetrics.fps,
                fpsHistory = liveMetrics.fpsHistory,
                cpu = liveMetrics.cpuUsagePercent,
                cpuHistory = liveMetrics.cpuHistory,
                gpu = liveMetrics.gpuUsagePercent,
                gpuHistory = liveMetrics.gpuHistory,
                ramUsedMb = liveMetrics.ramUsedMb,
                ramTotalMb = liveMetrics.ramTotalMb,
                tempC = liveMetrics.batteryTempC,
                alpha = hudAlpha,
                isLocked = hudConfig.isLocked,
                onDrag = onDrag,
                onResize = onResize,
                onClose = onClose
            )
        }
    }
}

@Composable
private fun MinimizedBubble(
    fps: Float,
    alpha: Float,
    onExpand: () -> Unit,
    onDrag: (dx: Float, dy: Float) -> Unit
) {
    val fpsColor = when {
        fps >= 58f -> Color(0xFF00FF9D)
        fps >= 40f -> Color(0xFFFFB800)
        else -> Color(0xFFFF3366)
    }

    Box(
        modifier = Modifier
            .size(54.dp)
            .alpha(alpha)
            .shadow(10.dp, CircleShape)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF1E293B), Color(0xFF090D16))
                )
            )
            .border(1.5.dp, fpsColor.copy(alpha = 0.8f), CircleShape)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDrag(dragAmount.x, dragAmount.y)
                }
            }
            .clickable(onClick = onExpand)
            .testTag("hud_minimized_bubble"),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${fps.toInt()}",
                color = fpsColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "FPS",
                color = Color(0xFF94A3B8),
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun CompactHudCard(
    fps: Float,
    cpu: Int,
    gpu: Int,
    alpha: Float,
    isLocked: Boolean,
    onDrag: (dx: Float, dy: Float) -> Unit,
    onResize: (dw: Float, dh: Float) -> Unit,
    onClose: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)

    Surface(
        modifier = Modifier
            .alpha(alpha)
            .shadow(12.dp, shape)
            .clip(shape)
            .border(1.dp, Brush.horizontalGradient(listOf(Color(0xFF00F0FF), Color(0xFF10B981))), shape)
            .background(Color(0xEE090D16))
            .pointerInput(isLocked) {
                if (!isLocked) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        onDrag(dragAmount.x, dragAmount.y)
                    }
                }
            }
            .testTag("hud_compact_card"),
        color = Color(0xDD090D16)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (!isLocked) {
                Icon(
                    imageVector = Icons.Default.DragHandle,
                    contentDescription = "Drag Grip",
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(16.dp)
                )
            }

            // FPS Pill
            MetricPill(label = "FPS", value = "${fps.toInt()}", color = if (fps >= 55f) Color(0xFF00FF9D) else Color(0xFFFFB800))

            // CPU Pill
            MetricPill(label = "CPU", value = "$cpu%", color = Color(0xFF00F0FF))

            // GPU Pill
            MetricPill(label = "GPU", value = "$gpu%", color = Color(0xFFFF7A00))

            // Quick Cycle Size button
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E293B))
                    .clickable { HudStateManager.cycleSizeMode() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.UnfoldMore,
                    contentDescription = "Expand HUD",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }

            // Close button
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF334155))
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close HUD",
                    tint = Color(0xFFCBD5E1),
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

@Composable
private fun NormalHudCard(
    fps: Float,
    cpu: Int,
    gpu: Int,
    ramUsedMb: Long,
    tempC: Float,
    alpha: Float,
    isLocked: Boolean,
    onDrag: (dx: Float, dy: Float) -> Unit,
    onResize: (dw: Float, dh: Float) -> Unit,
    onClose: () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)

    Surface(
        modifier = Modifier
            .alpha(alpha)
            .shadow(16.dp, shape)
            .clip(shape)
            .border(
                1.5.dp,
                Brush.linearGradient(listOf(Color(0xFF00F0FF), Color(0xFF8B5CF6), Color(0xFF00FF9D))),
                shape
            )
            .background(Color(0xF00A0F1D))
            .pointerInput(isLocked) {
                if (!isLocked) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        onDrag(dragAmount.x, dragAmount.y)
                    }
                }
            }
            .testTag("hud_normal_card"),
        color = Color(0xF00A0F1D)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Header bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00FF9D))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "HUD LIVE",
                        color = Color(0xFFE2E8F0),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Minimize to bubble
                    MiniIconButton(
                        icon = Icons.Default.ZoomInMap,
                        desc = "Minimize",
                        onClick = { HudStateManager.toggleMinimized() }
                    )
                    // Cycle size
                    MiniIconButton(
                        icon = Icons.Default.ZoomOutMap,
                        desc = "Expand Size",
                        onClick = { HudStateManager.cycleSizeMode() }
                    )
                    // Lock/Unlock position
                    MiniIconButton(
                        icon = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                        desc = "Lock Position",
                        tint = if (isLocked) Color(0xFFFFB800) else Color(0xFF94A3B8),
                        onClick = { HudStateManager.toggleLock() }
                    )
                    // Close button
                    MiniIconButton(
                        icon = Icons.Default.Close,
                        desc = "Close",
                        onClick = onClose
                    )
                }
            }

            // Stats grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // FPS Block
                MetricTile(
                    title = "FPS",
                    value = "${fps.toInt()}",
                    color = if (fps >= 58f) Color(0xFF00FF9D) else if (fps >= 40f) Color(0xFFFFB800) else Color(0xFFFF3366),
                    modifier = Modifier.weight(1f)
                )
                // CPU Block
                MetricTile(
                    title = "CPU",
                    value = "$cpu%",
                    color = Color(0xFF00F0FF),
                    progress = cpu / 100f,
                    modifier = Modifier.weight(1f)
                )
                // GPU Block
                MetricTile(
                    title = "GPU",
                    value = "$gpu%",
                    color = Color(0xFFFF7A00),
                    progress = gpu / 100f,
                    modifier = Modifier.weight(1f)
                )
            }

            // Secondary footer with RAM & Temp & Corner Resize Handle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "RAM ${(ramUsedMb / 1024f * 10).roundToInt() / 10f}GB • ${tempC.toInt()}°C",
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Opacity switch
                    Text(
                        text = "α ${(alpha * 100).toInt()}%",
                        color = Color(0xFF64748B),
                        fontSize = 9.sp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { HudStateManager.cycleOpacity() }
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    )

                    // Corner Drag-to-Resize handle
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .pointerInput(Unit) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    onResize(dragAmount.x, dragAmount.y)
                                }
                            }
                            .testTag("hud_resize_handle"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenWith,
                            contentDescription = "Resize HUD Handle",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpandedHudCard(
    fps: Float,
    fpsHistory: List<Float>,
    cpu: Int,
    cpuHistory: List<Int>,
    gpu: Int,
    gpuHistory: List<Int>,
    ramUsedMb: Long,
    ramTotalMb: Long,
    tempC: Float,
    alpha: Float,
    isLocked: Boolean,
    onDrag: (dx: Float, dy: Float) -> Unit,
    onResize: (dw: Float, dh: Float) -> Unit,
    onClose: () -> Unit
) {
    val shape = RoundedCornerShape(20.dp)

    Surface(
        modifier = Modifier
            .alpha(alpha)
            .shadow(20.dp, shape)
            .clip(shape)
            .border(
                1.5.dp,
                Brush.horizontalGradient(listOf(Color(0xFF00F0FF), Color(0xFF38BDF8), Color(0xFF10B981))),
                shape
            )
            .background(Color(0xF5080C19))
            .pointerInput(isLocked) {
                if (!isLocked) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        onDrag(dragAmount.x, dragAmount.y)
                    }
                }
            }
            .testTag("hud_expanded_card"),
        color = Color(0xF5080C19)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00FF9D))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "HARDWARE HUD PRO",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    MiniIconButton(
                        icon = Icons.Default.ZoomInMap,
                        desc = "Minimize",
                        onClick = { HudStateManager.toggleMinimized() }
                    )
                    MiniIconButton(
                        icon = Icons.Default.ZoomOutMap,
                        desc = "Cycle Size",
                        onClick = { HudStateManager.cycleSizeMode() }
                    )
                    MiniIconButton(
                        icon = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                        desc = "Lock Position",
                        tint = if (isLocked) Color(0xFFFFB800) else Color(0xFF94A3B8),
                        onClick = { HudStateManager.toggleLock() }
                    )
                    MiniIconButton(
                        icon = Icons.Default.Close,
                        desc = "Close",
                        onClick = onClose
                    )
                }
            }

            // Main stats tiles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MetricTile(
                    title = "LIVE FPS",
                    value = String.format("%.1f", fps),
                    color = if (fps >= 58f) Color(0xFF00FF9D) else if (fps >= 40f) Color(0xFFFFB800) else Color(0xFFFF3366),
                    modifier = Modifier.weight(1.2f)
                )
                MetricTile(
                    title = "CPU LOAD",
                    value = "$cpu%",
                    color = Color(0xFF00F0FF),
                    progress = cpu / 100f,
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    title = "GPU LOAD",
                    value = "$gpu%",
                    color = Color(0xFFFF7A00),
                    progress = gpu / 100f,
                    modifier = Modifier.weight(1f)
                )
            }

            // Real-time Waveform Graph
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF030712))
                    .border(0.8.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                MiniSparkline(
                    dataFps = fpsHistory,
                    dataCpu = cpuHistory,
                    modifier = Modifier.fillMaxSize()
                )
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 4.dp, top = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("• FPS", color = Color(0xFF00FF9D), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    Text("• CPU", color = Color(0xFF00F0FF), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Detailed Hardware row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val ramGbUsed = (ramUsedMb / 1024f * 10).roundToInt() / 10f
                val ramGbTotal = (ramTotalMb / 1024f * 10).roundToInt() / 10f
                Text(
                    text = "RAM: ${ramGbUsed}/${ramGbTotal} GB • BATT: ${tempC.toInt()}°C",
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "α ${(alpha * 100).toInt()}%",
                        color = Color(0xFF38BDF8),
                        fontSize = 9.sp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF1E293B))
                            .clickable { HudStateManager.cycleOpacity() }
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    )

                    // Corner Drag-to-Resize handle
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .pointerInput(Unit) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    onResize(dragAmount.x, dragAmount.y)
                                }
                            }
                            .testTag("hud_resize_handle_expanded"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenWith,
                            contentDescription = "Resize Handle",
                            tint = Color(0xFF00F0FF),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricPill(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF1E293B))
            .padding(horizontal = 6.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = label, color = Color(0xFF94A3B8), fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Text(text = value, color = color, fontSize = 11.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
    }
}

@Composable
private fun MetricTile(
    title: String,
    value: String,
    color: Color,
    progress: Float? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF111827))
            .border(0.8.dp, Color(0xFF1F2937), RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                color = Color(0xFF94A3B8),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = value,
                color = color,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
            if (progress != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(1.5.dp))
                        .background(Color(0xFF1F2937))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress.coerceIn(0f, 1f))
                            .height(3.dp)
                            .clip(RoundedCornerShape(1.5.dp))
                            .background(color)
                    )
                }
            }
        }
    }
}

@Composable
private fun MiniIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    desc: String,
    tint: Color = Color(0xFFCBD5E1),
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(22.dp)
            .clip(CircleShape)
            .background(Color(0xFF1E293B))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = desc,
            tint = tint,
            modifier = Modifier.size(13.dp)
        )
    }
}

@Composable
private fun MiniSparkline(
    dataFps: List<Float>,
    dataCpu: List<Int>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (dataFps.size < 2 && dataCpu.size < 2) return@Canvas

        val w = size.width
        val h = size.height

        // Draw CPU curve (scaled 0..100)
        if (dataCpu.size >= 2) {
            val step = w / (dataCpu.size - 1)
            val cpuPath = Path()
            dataCpu.forEachIndexed { i, cpu ->
                val x = i * step
                val y = h - (cpu.toFloat() / 100f * h).coerceIn(2f, h - 2f)
                if (i == 0) cpuPath.moveTo(x, y) else cpuPath.lineTo(x, y)
            }
            drawPath(
                path = cpuPath,
                color = Color(0xFF00F0FF).copy(alpha = 0.75f),
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        // Draw FPS curve (scaled 0..120)
        if (dataFps.size >= 2) {
            val step = w / (dataFps.size - 1)
            val fpsPath = Path()
            dataFps.forEachIndexed { i, fps ->
                val x = i * step
                val y = h - ((fps / 120f) * h).coerceIn(2f, h - 2f)
                if (i == 0) fpsPath.moveTo(x, y) else fpsPath.lineTo(x, y)
            }
            drawPath(
                path = fpsPath,
                color = Color(0xFF00FF9D),
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}
