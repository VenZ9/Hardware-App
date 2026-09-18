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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Warning
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
import com.example.ui.components.CyberGauge
import com.example.ui.components.SpecRow

@Composable
fun BatteryThermalsScreen(hardwareMonitor: HardwareMonitor) {
    val batteryInfo by hardwareMonitor.batteryInfo.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Battery Health & Level Hero Card
        CyberCard(
            title = "Battery Management System",
            icon = Icons.Default.BatteryChargingFull,
            accentColor = Color(0xFF00FF9D)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CyberGauge(
                    title = "CHARGE",
                    valueText = "${batteryInfo.levelPercent}%",
                    unitText = batteryInfo.status.uppercase(),
                    progress = batteryInfo.levelPercent / 100f,
                    primaryColor = if (batteryInfo.levelPercent > 20) Color(0xFF00FF9D) else Color(0xFFFF3366),
                    secondaryColor = Color(0xFF00F0FF),
                    size = 120.dp
                )

                Column(
                    modifier = Modifier.weight(1f).padding(start = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SpecRow(label = "Health Status", value = batteryInfo.health, valueColor = Color(0xFF00FF9D))
                    SpecRow(label = "Power Source", value = batteryInfo.pluggedSource)
                    SpecRow(label = "Technology", value = batteryInfo.technology)
                    SpecRow(label = "Cell Voltage", value = "${batteryInfo.voltageMv} mV", isMonospace = true)
                }
            }
        }

        // Thermal Monitoring Card
        CyberCard(
            title = "Thermals & Temperature",
            icon = Icons.Default.Thermostat,
            accentColor = Color(0xFFFFB800)
        ) {
            val tempC = batteryInfo.temperatureC
            val tempF = (tempC * 9 / 5) + 32
            val tempColor = when {
                tempC > 45f -> Color(0xFFFF3366)
                tempC > 38f -> Color(0xFFFFB800)
                else -> Color(0xFF00FF9D)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CyberGauge(
                    title = "TEMP",
                    valueText = "${tempC.toInt()}°C",
                    unitText = "${tempF.toInt()}°F",
                    progress = (tempC / 60f).coerceIn(0f, 1f),
                    primaryColor = tempColor,
                    secondaryColor = Color(0xFFFF5E3A),
                    size = 120.dp
                )

                Column(
                    modifier = Modifier.weight(1f).padding(start = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val statusText = when {
                        tempC > 45f -> "High Heat Warning"
                        tempC > 38f -> "Warm (Gaming Load)"
                        else -> "Optimal Thermal Zone"
                    }
                    SpecRow(label = "Thermal State", value = statusText, valueColor = tempColor)
                    SpecRow(label = "Celsius", value = String.format("%.1f °C", tempC), isMonospace = true)
                    SpecRow(label = "Fahrenheit", value = String.format("%.1f °F", tempF), isMonospace = true)
                    SpecRow(label = "Throttling", value = if (tempC > 45f) "Active" else "None")
                }
            }

            // Thermal notice card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF1E293B)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ElectricBolt,
                        contentDescription = "Thermal Alert",
                        tint = Color(0xFF00F0FF),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Real-time thermal metrics sampled from kernel battery thermistor. Ideal gaming temperature is below 40°C.",
                        color = Color(0xFFCBD5E1),
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
