package com.bck.handshakebet.feature.records.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bck.handshakebet.feature.records.domain.repository.RecordsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Records screen.
 *
 * Loads the current user's completed-bet history and win/draw/loss record on
 * creation. Exposes [refresh] so the user can manually pull-to-refresh.
 *
 * @property recordsRepository Data source for records.
 */
@HiltViewModel
class RecordsViewModel @Inject constructor(
    private val recordsRepository: RecordsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<RecordsUiState>(RecordsUiState.Loading)
    val uiState: StateFlow<RecordsUiState> = _uiState.asStateFlow()

    init {
        loadRecords()
    }

    /** Reloads records from the server. */
    fun refresh() = loadRecords()

    // ── Private ───────────────────────────────────────────────────────────────

    private fun loadRecords() {
        viewModelScope.launch {
            _uiState.value = RecordsUiState.Loading
            recordsRepository.loadRecords()
                .onSuccess { data ->
                    _uiState.value = RecordsUiState.Success(
                        wins          = data.wins,
                        draws         = data.draws,
                        losses        = data.losses,
                        prideBalance  = data.prideBalance,
                        completedBets = data.completedBets
                    )
                }
                .onFailure { e ->
                    _uiState.value = RecordsUiState.Error(
                        e.message ?: "Could not load your records."
                    )
                }
        }
    }
}
