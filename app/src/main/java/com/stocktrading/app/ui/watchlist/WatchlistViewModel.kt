package com.stocktrading.app.ui.watchlist

import androidx.lifecycle.viewModelScope
import com.stocktrading.app.data.models.WatchlistWithStocks
import com.stocktrading.app.data.repository.WatchlistRepository
import com.stocktrading.app.ui.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WatchlistViewModel @Inject constructor(
    private val watchlistRepository: WatchlistRepository
) : BaseViewModel() {



    private val _uiState = MutableStateFlow(WatchlistUiState())
    val uiState: StateFlow<WatchlistUiState> = _uiState.asStateFlow()


    fun loadWatchlists() {
        viewModelScope.launch {
            setLoading(true)
            try {
                watchlistRepository.getAllWatchlistsWithStocks().collect { watchlists ->
                    _uiState.value = _uiState.value.copy(watchlists = watchlists)
                    setLoading(false)
                }
            } catch (e: Exception) {
                setError("Failed to load watchlists: ${e.message}")
                setLoading(false)
            }
        }
    }

    fun createWatchlist(name: String) {
        viewModelScope.launch {
            try {
                val watchlistId = watchlistRepository.createWatchlist(name)
                if (watchlistId > 0) {
                    setSuccess("Watchlist '$name' created successfully")
                    loadWatchlists()
                } else {
                    setError("Failed to create watchlist")
                }
            } catch (e: Exception) {
                setError("Failed to create watchlist: ${e.message}")
            }
        }
    }

    fun clearMessages() {
        clearError()
        clearSuccess()
    }

    override fun onCleared() {
        super.onCleared()
        clearMessages()
    }
}

data class WatchlistUiState(
    val watchlists: List<WatchlistWithStocks> = emptyList()
)

