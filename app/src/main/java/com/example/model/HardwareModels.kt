package com.example.model

enum class HudSizeMode(val label: String) {
    COMPACT("Compact"),
    NORMAL("Normal"),
    EXPANDED("Expanded")
}

data class LiveMetrics(
    val fps: Float = 60f,
    val fpsHistory: List<Float> = emptyList(),
    val cpuUsagePercent: Int = 0,
    val cpuHistory: List<Int> = emptyList(),
    val gpuUsagePercent: Int = 0,
    val gpuHistory: List<Int> = emptyList(),
    val ramUsedMb: Long = 0L,
    val ramTotalMb: Long = 0L,
    val batteryTempC: Float = 0f,
    val batteryLevel: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

data class CpuInfo(
    val modelName: String,
    val architecture: String,
    val coreCount: Int,
    val liveUsagePercent: Int,
    val perCoreFrequenciesKhz: List<Long>,
    val governor: String,
    val supportedAbis: List<String>
)

data class GpuInfo(
    val renderer: String,
    val vendor: String,
    val glVersion: String,
    val estimatedUsagePercent: Int,
    val maxTextureSize: Int,
    val extensionsCount: Int
)

data class DisplayInfo(
    val resolution: String,
    val currentRefreshRate: Float,
    val supportedRefreshRates: List<Float>,
    val densityDpi: Int,
    val densityScale: Float,
    val isHdrCapable: Boolean,
    val isWideColorGamut: Boolean
)

data class BatteryInfo(
    val levelPercent: Int,
    val voltageMv: Int,
    val temperatureC: Float,
    val health: String,
    val status: String,
    val pluggedSource: String,
    val technology: String
)

data class MemoryInfo(
    val totalRamMb: Long,
    val availableRamMb: Long,
    val usedRamMb: Long,
    val usedPercent: Int,
    val isLowMemory: Boolean,
    val jvmHeapUsedMb: Long,
    val jvmHeapMaxMb: Long
)

data class StorageInfo(
    val totalBytes: Long,
    val freeBytes: Long,
    val usedBytes: Long,
    val usedPercent: Int
)

data class DeviceSpec(
    val manufacturer: String,
    val brand: String,
    val model: String,
    val device: String,
    val board: String,
    val androidVersion: String,
    val sdkInt: Int,
    val buildId: String,
    val securityPatch: String,
    val kernelVersion: String
)

data class SensorData(
    val name: String,
    val typeName: String,
    val vendor: String,
    val powerMa: Float,
    val resolution: Float,
    val values: List<Float> = emptyList(),
    val history: List<Float> = emptyList()
)

data class HudConfig(
    val isEnabled: Boolean = false,
    val isVisible: Boolean = true,
    val isMinimized: Boolean = false,
    val isLocked: Boolean = false,
    val sizeMode: HudSizeMode = HudSizeMode.NORMAL,
    val opacity: Float = 0.88f,
    val customWidthDp: Int = 220,
    val customHeightDp: Int = 130,
    val showFps: Boolean = true,
    val showCpu: Boolean = true,
    val showGpu: Boolean = true,
    val showRam: Boolean = true,
    val showBatteryTemp: Boolean = true
)
