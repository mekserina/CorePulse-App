package com.architecture.corepulse.data.source

import com.architecture.corepulse.data.model.CpuTelemetry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.RandomAccessFile

class ProcfsCpuReader {
    private var lastTotal: Long = 0
    private var lastIdle: Long = 0

    suspend fun readCpuTelemetry(): CpuTelemetry = withContext(Dispatchers.IO) {
        var totalUsage = 0f
        
        try {
            RandomAccessFile("/proc/stat", "r").use { file ->
                val line = file.readLine() // First line is total CPU
                if (line != null && line.startsWith("cpu")) {
                    val parts = line.split("\\s+".toRegex()).filter { it.isNotBlank() }
                    if (parts.size >= 5) {
                        // parts[0] is "cpu"
                        val user = parts[1].toLong()
                        val nice = parts[2].toLong()
                        val system = parts[3].toLong()
                        val idle = parts[4].toLong()
                        val iowait = parts.getOrNull(5)?.toLong() ?: 0
                        val irq = parts.getOrNull(6)?.toLong() ?: 0
                        val softirq = parts.getOrNull(7)?.toLong() ?: 0
                        val steal = parts.getOrNull(8)?.toLong() ?: 0

                        val currentIdle = idle + iowait
                        val currentTotal = user + nice + system + currentIdle + irq + softirq + steal

                        if (lastTotal > 0) {
                            val deltaTotal = currentTotal - lastTotal
                            val deltaIdle = currentIdle - lastIdle
                            if (deltaTotal > 0) {
                                totalUsage = (deltaTotal - deltaIdle).toFloat() / deltaTotal.toFloat() * 100f
                            }
                        }

                        lastTotal = currentTotal
                        lastIdle = currentIdle
                    }
                }
            }
        } catch (e: Exception) {
            // Log in production
        }

        CpuTelemetry(totalUsagePercent = totalUsage)
    }
}
