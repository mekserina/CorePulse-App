package com.architecture.corepulse.data.source

import android.util.Log
import com.architecture.corepulse.data.model.MemoryTelemetry
import com.architecture.corepulse.data.util.ZeroAllocParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.RandomAccessFile

private const val TAG = "ProcfsMemoryReader"

class ProcfsMemoryReader {
    private val buffer = ByteArray(2048) // Memory info is usually small

    suspend fun readMemoryTelemetry(): MemoryTelemetry = withContext(Dispatchers.IO) {
        var total: Long = 0
        var available: Long = 0
        
        try {
            RandomAccessFile("/proc/meminfo", "r").use { file ->
                val bytesRead = file.read(buffer)
                if (bytesRead > 0) {
                    total = ZeroAllocParser.parseLongAfterToken(buffer, bytesRead, "MemTotal:")
                    available = ZeroAllocParser.parseLongAfterToken(buffer, bytesRead, "MemAvailable:")
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to read /proc/meminfo", e)
        }

        val used = if (total >= available) total - available else 0
        MemoryTelemetry(totalKb = total, availableKb = available, usedKb = used)
    }
}
