package com.architecture.corepulse.data.model

data class MemoryTelemetry(
    val totalKb: Long = 0,
    val availableKb: Long = 0,
    val usedKb: Long = 0
)

data class CpuTelemetry(
    val totalUsagePercent: Float = 0f,
    val coreCount: Int = 0,
    val perCoreUsage: List<Float> = emptyList()
)

data class ThermalTelemetry(
    val socTempCelsius: Float = 0f,
    val isCritical: Boolean = false
)

data class TelemetryState(
    val memory: MemoryTelemetry = MemoryTelemetry(),
    val cpu: CpuTelemetry = CpuTelemetry(),
    val thermal: ThermalTelemetry = ThermalTelemetry(),
    val timestamp: Long = System.currentTimeMillis()
)
