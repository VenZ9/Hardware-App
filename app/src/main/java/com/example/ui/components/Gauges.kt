package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CyberGauge(
    title: String,
    valueText: String,
    unitText: String,
    progress: Float, // 0f to 1f
    primaryColor: Color = Color(0xFF00F0FF),
    secondaryColor: Color = Color(0xFF10B981),
    size: Dp = 130.dp,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "gaugeProgress"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 10.dp.toPx()
            val diameter = this.size.minDimension - strokeWidth
            val arcSize = Size(diameter, diameter)
            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

            val startAngle = 135f
            val totalSweep = 270f

            // Background track
            drawArc(
                color = Color(0xFF1E293B),
                startAngle = startAngle,
                sweepAngle = totalSweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Animated progress arc
            if (animatedProgress > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        0.0f to primaryColor,
                        0.5f to secondaryColor,
                        1.0f to primaryColor
                    ),
                    startAngle = startAngle,
                    sweepAngle = totalSweep * animatedProgress,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // Glow point at tip of arc
            val currentAngleRad = Math.toRadians((startAngle + totalSweep * animatedProgress).toDouble())
            val radius = diameter / 2
            val center = Offset(this.size.width / 2, this.size.height / 2)
            val tipX = center.x + radius * cos(currentAngleRad).toFloat()
            val tipY = center.y + radius * sin(currentAngleRad).toFloat()

            drawCircle(
                color = primaryColor.copy(alpha = pulseAlpha),
                radius = strokeWidth * 0.7f,
                center = Offset(tipX, tipY)
            )
            drawCircle(
                color = Color.White,
                radius = strokeWidth * 0.35f,
                center = Offset(tipX, tipY)
            )
        }

        // Center Text Readout
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                color = Color(0xFF94A3B8),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Text(
                text = valueText,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = unitText,
                color = primaryColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun SensorWaveform(
    history: List<Float>,
    lineColor: Color,
    modifier: Modifier = Modifier,
    minValue: Float = -20f,
    maxValue: Float = 20f
) {
    Canvas(modifier = modifier) {
        if (history.size < 2) return@Canvas

        val w = size.width
        val h = size.height
        val step = w / (history.size - 1)
        val range = (maxValue - minValue).coerceAtLeast(1f)

        // Draw center reference line
        val zeroY = h - ((0f - minValue) / range * h).coerceIn(0f, h)
        drawLine(
            color = Color(0xFF1E293B),
            start = Offset(0f, zeroY),
            end = Offset(w, zeroY),
            strokeWidth = 1.dp.toPx()
        )

        val path = Path()
        history.forEachIndexed { i, value ->
            val x = i * step
            val normalized = ((value - minValue) / range).coerceIn(0f, 1f)
            val y = h - (normalized * h)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
fun MultiCoreBar(
    coreIndex: Int,
    frequencyMhz: Long,
    maxFreqMhz: Long = 2800L,
    modifier: Modifier = Modifier
) {
    val progress = (frequencyMhz.toFloat() / maxFreqMhz).coerceIn(0.1f, 1f)
    val color = when {
        progress > 0.8f -> Color(0xFFFF5E3A)
        progress > 0.5f -> Color(0xFF00F0FF)
        else -> Color(0xFF10B981)
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Core $coreIndex",
            color = Color(0xFF94A3B8),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.size(width = 54.dp, height = 16.dp)
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF1E293B))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
        }

        Text(
            text = "${frequencyMhz}MHz",
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.size(width = 65.dp, height = 16.dp)
        )
    }
}
