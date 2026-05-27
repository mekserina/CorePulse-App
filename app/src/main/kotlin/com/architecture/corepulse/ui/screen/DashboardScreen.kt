package com.architecture.corepulse.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.architecture.corepulse.ui.TelemetryViewModel
import com.architecture.corepulse.ui.component.RealTimeGraph
import com.architecture.corepulse.ui.component.TelemetryCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: TelemetryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Maintain history for graphs
    val cpuHistory = remember { mutableStateListOf<Float>() }
    val memoryHistory = remember { mutableStateListOf<Float>() }

    LaunchedEffect(uiState) {
        cpuHistory.add(uiState.cpu.totalUsagePercent)
        if (cpuHistory.size > 100) cpuHistory.removeAt(0)
        
        val memUsage = if (uiState.memory.totalKb > 0) {
            (uiState.memory.usedKb.toFloat() / uiState.memory.totalKb.toFloat()) * 100f
        } else 0f
        memoryHistory.add(memUsage)
        if (memoryHistory.size > 100) memoryHistory.removeAt(0)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("COREPULSE", style = MaterialTheme.typography.headlineSmall) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                Column {
                    Text(
                        "CPU LOAD (%)",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    RealTimeGraph(data = cpuHistory.toList())
                }
            }

            item {
                TelemetryCard(
                    title = "CPU",
                    value = "%.1f%%".format(uiState.cpu.totalUsagePercent)
                )
            }

            item {
                val memUsage = if (uiState.memory.totalKb > 0) {
                    (uiState.memory.usedKb.toFloat() / uiState.memory.totalKb.toFloat()) * 100f
                } else 0f
                TelemetryCard(
                    title = "MEMORY",
                    value = "%.1f%%".format(memUsage),
                    subValue = "${uiState.memory.usedKb / 1024} / ${uiState.memory.totalKb / 1024} MB"
                )
            }

            item {
                TelemetryCard(
                    title = "THERMAL",
                    value = "%.1f°C".format(uiState.thermal.socTempCelsius),
                    subValue = if (uiState.thermal.isCritical) "CRITICAL" else "OPTIMAL",
                    modifier = Modifier.padding(bottom = 0.dp) // Resetting padding if needed
                )
            }

            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                Column {
                    Text(
                        "MEMORY USAGE (%)",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    RealTimeGraph(data = memoryHistory.toList(), color = MaterialTheme.colorScheme.secondary)
                }
            }
        }
    }
}
