package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
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
import com.example.ui.components.CyberGauge
import com.example.ui.components.MultiCoreBar
import com.example.ui.components.SensorWaveform
import com.example.ui.components.SpecRow

@Composable
fun CpuGpuScreen(hardwareMonitor: HardwareMonitor) {
    val cpuInfo by hardwareMonitor.cpuInfo.collectAsState()
    val gpuInfo by hardwareMonitor.gpuInfo.collectAsState()
    val liveMetrics by hardwareMonitor.liveMetrics.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // CPU Hero Card
        CyberCard(
            title = "Central Processing Unit (CPU)",
            icon = Icons.Default.Memory,
            accentColor = Color(0xFF00F0FF)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CyberGauge(
                    title = "CPU USAGE",
                    valueText = "${liveMetrics.cpuUsagePercent}%",
                    unitText = "LOAD",
                    progress = liveMetrics.cpuUsagePercent / 100f,
                    primaryColor = Color(0xFF00F0FF),
                    secondaryColor = Color(0xFF38BDF8),
                    size = 115.dp
                )

                Column(
                    modifier = Modifier.weight(1f).padding(start = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    SpecRow(label = "Processor", value = cpuInfo.modelName)
                    SpecRow(label = "Architecture", value = cpuInfo.architecture, isMonospace = true)
                    SpecRow(label = "Total Cores", value = "${cpuInfo.coreCount} Cores")
                    SpecRow(label = "Governor", value = cpuInfo.governor, isMonospace = true)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Multi-Core Frequency Distribution
            Text(
                text = "PER-CORE FREQUENCIES",
                color = Color(0xFF94A3B8),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            cpuInfo.perCoreFrequenciesKhz.forEachIndexed { index, freqKhz ->
                val freqMhz = freqKhz / 1000L
                MultiCoreBar(
                    coreIndex = index,
                    frequencyMhz = freqMhz
                )
            }
        }

        // Live CPU Load Waveform
        CyberCard(
            title = "Real-Time CPU Load Wave",
            icon = Icons.Default.Speed,
            accentColor = Color(0xFF38BDF8)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF070B14))
                    .padding(8.dp)
            ) {
                SensorWaveform(
                    history = liveMetrics.cpuHistory.map { it.toFloat() },
                    lineColor = Color(0xFF00F0FF),
                    modifier = Modifier.fillMaxSize(),
                    minValue = 0f,
                    maxValue = 100f
                )
            }
            Text(
                text = "Dynamic load sampled from /proc/stat core delta intervals",
                color = Color(0xFF64748B),
                fontSize = 11.sp
            )
        }

        // GPU Acceleration Card
        CyberCard(
            title = "Graphics Processing Unit (GPU)",
            icon = Icons.Default.DeveloperBoard,
            accentColor = Color(0xFFFF5E3A)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CyberGauge(
                    title = "GPU LOAD",
                    valueText = "${liveMetrics.gpuUsagePercent}%",
                    unitText = "RENDER",
                    progress = liveMetrics.gpuUsagePercent / 100f,
                    primaryColor = Color(0xFFFF5E3A),
                    secondaryColor = Color(0xFFFFB800),
                    size = 115.dp
                )

                Column(
                    modifier = Modifier.weight(1f).padding(start = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    SpecRow(label = "GPU Vendor", value = gpuInfo.vendor)
                    SpecRow(label = "Renderer", value = gpuInfo.renderer)
                    SpecRow(label = "OpenGL ES", value = gpuInfo.glVersion, isMonospace = true)
                    SpecRow(label = "Max Texture", value = "${gpuInfo.maxTextureSize}px")
                    SpecRow(label = "GL Extensions", value = "${gpuInfo.extensionsCount}+ available")
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "REAL-TIME GPU RENDER PACING",
                color = Color(0xFF94A3B8),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF070B14))
                    .padding(8.dp)
            ) {
                SensorWaveform(
                    history = liveMetrics.gpuHistory.map { it.toFloat() },
                    lineColor = Color(0xFFFF5E3A),
                    modifier = Modifier.fillMaxSize(),
                    minValue = 0f,
                    maxValue = 100f
                )
            }
        }

        // Supported ABIs
        CyberCard(
            title = "Supported Instruction Sets (ABIs)",
            accentColor = Color(0xFF10B981)
        ) {
            cpuInfo.supportedAbis.forEach { abi ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Target ABI", color = Color(0xFF94A3B8), fontSize = 13.sp)
                    Text(text = abi, color = Color(0xFF00FF9D), fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
