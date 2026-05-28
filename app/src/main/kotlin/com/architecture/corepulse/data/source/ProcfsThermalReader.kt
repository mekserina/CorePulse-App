package com.architecture.corepulse.data.source

import com.architecture.corepulse.data.model.ThermalTelemetry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.RandomAccessFile

class ProcfsThermalReader(
    private val thermalPaths: List<String> = listOf(
        "/sys/class/thermal/thermal_zone0/temp",
        "/sys/class/thermal/thermal_zone1/temp",
        "/sys/class/thermal/thermal_zone7/temp" // Often SoC on some vendors
    )
) {

    suspend fun readThermalTelemetry(): ThermalTelemetry = withContext(Dispatchers.IO) {
        var temp: Float = 0f
        
        var found = false
        for (path in thermalPaths) {
            if (found) break
            try {
                val file = File(path)
                if (file.exists()) {
                    RandomAccessFile(path, "r").use { raf ->
                        val content = raf.readLine()
                        if (content != null) {
                            val rawTemp = content.trim().toLong()
                            // Thermal values are usually in milli-Celsius
                            temp = if (rawTemp > 1000) rawTemp / 1000f else rawTemp.toFloat()
                            if (temp > 0) {
                                found = true
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Skip this path
            }
        }

        ThermalTelemetry(
            socTempCelsius = temp,
            isCritical = temp > 75f
        )
    }
}
