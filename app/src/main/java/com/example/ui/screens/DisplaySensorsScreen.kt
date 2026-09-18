package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hardware.HardwareMonitor
import com.example.ui.components.CyberCard
import com.example.ui.components.SensorWaveform
import com.example.ui.components.SpecRow

@Composable
fun DisplaySensorsScreen(hardwareMonitor: HardwareMonitor) {
    val displayInfo by hardwareMonitor.displayInfo.collectAsState()
    val sensors by hardwareMonitor.sensors.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Display Specifications Card
        CyberCard(
            title = "Display & Screen Hardware",
            icon = Icons.Default.Tv,
            accentColor = Color(0xFF00F0FF)
        ) {
            SpecRow(label = "Resolution", value = displayInfo.resolution, isMonospace = true)
            SpecRow(label = "Refresh Rate", value = "${displayInfo.currentRefreshRate.toInt()} Hz", valueColor = Color(0xFF00FF9D))
            SpecRow(label = "Screen Density", value = "${displayInfo.densityDpi} DPI (${displayInfo.densityScale}x scale)")
            SpecRow(label = "HDR Support", value = if (displayInfo.isHdrCapable) "HDR10 / HLG Supported" else "Standard Dynamic Range")
            SpecRow(label = "Wide Color Gamut", value = if (displayInfo.isWideColorGamut) "Supported (DCI-P3)" else "sRGB Standard")

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "SUPPORTED DISPLAY MODES",
                color = Color(0xFF94A3B8),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                displayInfo.supportedRefreshRates.forEach { hz ->
                    val isCurrent = (hz.toInt() == displayInfo.currentRefreshRate.toInt())
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isCurrent) Color(0xFF00F0FF) else Color(0xFF1E293B),
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Text(
                            text = "${hz.toInt()} Hz",
                            color = if (isCurrent) Color(0xFF090D16) else Color(0xFFCBD5E1),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Live Sensors List
        Text(
            text = "LIVE SENSOR TELEMETRY",
            color = Color(0xFF94A3B8),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        sensors.forEach { sensor ->
            CyberCard(
                title = sensor.typeName,
                icon = Icons.Default.Sensors,
                accentColor = when (sensor.typeName) {
                    "Accelerometer" -> Color(0xFF00F0FF)
                    "Gyroscope" -> Color(0xFF10B981)
                    "Light Sensor" -> Color(0xFFFFB800)
                    else -> Color(0xFF8B5CF6)
                }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Vendor: ${sensor.vendor}", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    Text(text = "Power: ${sensor.powerMa} mA", color = Color(0xFF64748B), fontSize = 11.sp)
                }

                // Live values readout
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val labels = listOf("X", "Y", "Z")
                    sensor.values.take(3).forEachIndexed { index, value ->
                        val axisLabel = labels.getOrElse(index) { "#$index" }
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF070B14),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = axisLabel, color = Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = String.format("%.2f", value),
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }

                // Live waveform for dynamic sensors
                if (sensor.history.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF050811))
                            .padding(4.dp)
                    ) {
                        SensorWaveform(
                            history = sensor.history,
                            lineColor = when (sensor.typeName) {
                                "Accelerometer" -> Color(0xFF00F0FF)
                                "Gyroscope" -> Color(0xFF10B981)
                                "Light Sensor" -> Color(0xFFFFB800)
                                else -> Color(0xFF8B5CF6)
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
