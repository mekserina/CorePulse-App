package com.architecture.corepulse.data.source

import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

/**
 * Regression tests for the fix in ProcfsThermalReader that replaced an
 * illegal `break` inside a `withContext` lambda with a `found` flag pattern.
 *
 * Without the fix the loop would either fail to compile (bare `break` in a
 * lambda is a compile error in Kotlin) or, if the break were simply removed,
 * the loop would continue iterating over every thermal path and overwrite a
 * valid temperature with a later value.
 *
 * These tests verify that the loop stops at the first valid temperature.
 */
class ProcfsThermalReaderTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var thermalFile1: File
    private lateinit var thermalFile2: File
    private lateinit var thermalFile3: File

    @Before
    fun setUp() {
        thermalFile1 = tempFolder.newFile("thermal_zone0_temp")
        thermalFile2 = tempFolder.newFile("thermal_zone1_temp")
        thermalFile3 = tempFolder.newFile("thermal_zone7_temp")
    }

    /**
     * When the first path contains a valid milli-Celsius temperature, the
     * reader must return that value and not continue to subsequent paths.
     *
     * Without the fix (no break on valid temp) the loop would keep going and
     * `temp` would be overwritten by thermalFile2's value (85 °C), causing
     * this assertion to fail.
     */
    @Test
    fun loopBreaksOnFirstValidTemperature() = runTest {
        thermalFile1.writeText("45000\n") // 45 °C — valid
        thermalFile2.writeText("85000\n") // 85 °C — should never be reached
        thermalFile3.writeText("90000\n") // 90 °C — should never be reached

        val reader = ProcfsThermalReader(
            thermalPaths = listOf(
                thermalFile1.absolutePath,
                thermalFile2.absolutePath,
                thermalFile3.absolutePath
            )
        )

        val result = reader.readThermalTelemetry()

        assertEquals(45f, result.socTempCelsius, 0.01f)
        assertFalse(result.isCritical)
    }

    /**
     * When only a later path has a valid temperature (the earlier ones are
     * empty or zero), the reader should keep searching and return the first
     * valid one it finds.
     */
    @Test
    fun loopContinuesUntilValidTemperatureFound() = runTest {
        thermalFile1.writeText("0\n")     // zero — not valid
        thermalFile2.writeText("52000\n") // 52 °C — first valid
        thermalFile3.writeText("99000\n") // 99 °C — should not be reached

        val reader = ProcfsThermalReader(
            thermalPaths = listOf(
                thermalFile1.absolutePath,
                thermalFile2.absolutePath,
                thermalFile3.absolutePath
            )
        )

        val result = reader.readThermalTelemetry()

        assertEquals(52f, result.socTempCelsius, 0.01f)
        assertFalse(result.isCritical)
    }

    /**
     * A temperature above 75 °C must be flagged as critical.
     * This also proves the loop stopped at the first valid path, because the
     * second path has a non-critical value that would clear the flag if the
     * loop did not break.
     */
    @Test
    fun criticalTemperatureDetected() = runTest {
        thermalFile1.writeText("80000\n") // 80 °C — critical
        thermalFile2.writeText("30000\n") // 30 °C — would overwrite if no break

        val reader = ProcfsThermalReader(
            thermalPaths = listOf(
                thermalFile1.absolutePath,
                thermalFile2.absolutePath
            )
        )

        val result = reader.readThermalTelemetry()

        assertEquals(80f, result.socTempCelsius, 0.01f)
        assertTrue(result.isCritical)
    }

    /**
     * When none of the paths exist, the reader must return a zero-value
     * ThermalTelemetry without crashing.
     */
    @Test
    fun noValidPathsReturnsDefault() = runTest {
        val reader = ProcfsThermalReader(
            thermalPaths = listOf(
                "/nonexistent/path1",
                "/nonexistent/path2"
            )
        )

        val result = reader.readThermalTelemetry()

        assertEquals(0f, result.socTempCelsius, 0.01f)
        assertFalse(result.isCritical)
    }

    /**
     * Verifies that an exception in the first path does not prevent reading
     * a valid temperature from a subsequent path.
     */
    @Test
    fun exceptionInEarlierPathDoesNotBlockLaterPaths() = runTest {
        thermalFile1.writeText("not_a_number\n") // will throw NumberFormatException
        thermalFile2.writeText("60000\n")         // 60 °C — valid

        val reader = ProcfsThermalReader(
            thermalPaths = listOf(
                thermalFile1.absolutePath,
                thermalFile2.absolutePath
            )
        )

        val result = reader.readThermalTelemetry()

        assertEquals(60f, result.socTempCelsius, 0.01f)
        assertFalse(result.isCritical)
    }
}
