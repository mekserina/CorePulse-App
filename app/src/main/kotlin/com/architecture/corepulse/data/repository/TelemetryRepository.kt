package com.architecture.corepulse.data.repository

import com.architecture.corepulse.data.model.TelemetryState
import com.architecture.corepulse.data.source.ProcfsCpuReader
import com.architecture.corepulse.data.source.ProcfsMemoryReader
import com.architecture.corepulse.data.source.ProcfsThermalReader
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class TelemetryRepository(
    private val memoryReader: ProcfsMemoryReader = ProcfsMemoryReader(),
    private val cpuReader: ProcfsCpuReader = ProcfsCpuReader(),
    private val thermalReader: ProcfsThermalReader = ProcfsThermalReader()
) {
    fun getTelemetryUpdates(intervalMs: Long = 1000L): Flow<TelemetryState> = flow {
        while (true) {
            val memory = memoryReader.readMemoryTelemetry()
            val cpu = cpuReader.readCpuTelemetry()
            val thermal = thermalReader.readThermalTelemetry()
            
            emit(TelemetryState(
                memory = memory,
                cpu = cpu,
                thermal = thermal,
                timestamp = System.currentTimeMillis()
            ))
            
            delay(intervalMs)
        }
    }
}
