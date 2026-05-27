package com.architecture.corepulse.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.architecture.corepulse.data.model.TelemetryState
import com.architecture.corepulse.data.repository.TelemetryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class TelemetryViewModel(
    repository: TelemetryRepository = TelemetryRepository()
) : ViewModel() {

    val uiState: StateFlow<TelemetryState> = repository.getTelemetryUpdates()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TelemetryState()
        )
}
