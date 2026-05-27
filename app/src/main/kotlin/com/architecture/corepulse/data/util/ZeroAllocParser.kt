package com.architecture.corepulse.data.util

object ZeroAllocParser {
    /**
     * Finds a token in the buffer and parses the next sequence of digits as a Long.
     * Returns 0 if not found or invalid.
     */
    fun parseLongAfterToken(buffer: ByteArray, bytesRead: Int, token: String): Long {
        val tokenBytes = token.toByteArray(Charsets.US_ASCII)
        val index = findTokenIndex(buffer, bytesRead, tokenBytes)
        if (index == -1) return 0L

        var pos = index + tokenBytes.size
        
        // Skip whitespace
        while (pos < bytesRead && buffer[pos].toInt().toChar().isWhitespace()) {
            pos++
        }

        var result = 0L
        var foundDigit = false
        while (pos < bytesRead && buffer[pos].toInt().toChar().isDigit()) {
            result = result * 10 + (buffer[pos] - '0'.toByte()).toLong()
            foundDigit = true
            pos++
        }

        return if (foundDigit) result else 0L
    }

    private fun findTokenIndex(buffer: ByteArray, bytesRead: Int, token: ByteArray): Int {
        if (token.isEmpty()) return -1
        for (i in 0 until (bytesRead - token.size + 1)) {
            var match = true
            for (j in token.indices) {
                if (buffer[i + j] != token[j]) {
                    match = false
                    break
                }
            }
            if (match) return i
        }
        return -1
    }
}
