package com.stocktrading.app.ui.watchlist

import androidx.lifecycle.viewModelScope
import com.stocktrading.app.data.models.NetworkResult
import com.stocktrading.app.data.models.Stock
import com.stocktrading.app.data.models.Watchlist
import com.stocktrading.app.data.repository.StockRepository
import com.stocktrading.app.data.repository.WatchlistRepository
import com.stocktrading.app.ui.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

@HiltViewModel
class WatchlistDetailViewModel @Inject constructor(
    private val watchlistRepository: WatchlistRepository,
    private val stockRepository: StockRepository
) : BaseViewModel() {

    companion object {
        private const val TAG = "WatchlistDetailViewModel"

        private val KNOWN_VALID_STOCKS = setOf(
            // Technology
            "AAPL", "MSFT", "GOOGL", "AMZN", "TSLA", "NVDA", "META", "NFLX",
            "AMD", "INTC", "CRM", "ORCL", "IBM", "ADBE", "PYPL", "SQ", "SHOP",
            "UBER", "LYFT", "SNAP", "PINS", "ROKU", "ZM", "DOCU", "OKTA",

            // Finance
            "JPM", "BAC", "WFC", "GS", "MS", "C", "AXP", "V", "MA", "BRK.B",

            // Healthcare
            "JNJ", "PFE", "UNH", "CVS", "ABBV", "MRK", "TMO", "ABT", "MDT",

            // Consumer
            "PG", "KO", "PEP", "WMT", "HD", "NKE", "MCD", "SBUX", "TGT", "COST",

            // Industrial
            "BA", "GE", "CAT", "UPS", "FDX", "RTX", "LMT", "MMM", "HON",

            // Energy
            "XOM", "CVX", "COP", "SLB", "OXY", "VLO", "PSX", "MPC",

            // ETFs
            "SPY", "QQQ", "DIA", "IWM", "VTI", "VOO", "VEA", "VWO", "GLD", "SLV",

            // Automotive
            "F", "GM", "RIVN", "LCID", "NIO", "XPEV", "LI",

            // Entertainment & Media
            "DIS", "CMCSA", "T", "VZ", "TMUS", "NFLX", "ROKU", "SPOT",

            // Retail
            "AMZN", "WMT", "TGT", "COST", "HD", "LOW", "TJX", "ROST", "BBY",

            // Meme stocks
            "GME", "AMC", "BB", "NOK", "PLTR", "CLOV", "WISH", "SOFI"
        )
    }

    private val _uiState = MutableStateFlow(WatchlistDetailUiState())
    val uiState: StateFlow<WatchlistDetailUiState> = _uiState.asStateFlow()

    fun loadWatchlistDetails(watchlistId: Long) {
        viewModelScope.launch {
            setLoading(true)
            try {
                watchlistRepository.getStocksInWatchlist(watchlistId).collect { stocks ->
                    _uiState.value = _uiState.value.copy(stocks = stocks)
                    setLoading(false)
                }
            } catch (e: Exception) {
                setError("Failed to load watchlist details: ${e.message}")
                setLoading(false)
            }
        }
    }

    fun addStockToWatchlist(watchlistId: Long, symbol: String) {
        viewModelScope.launch {
            try {
                setLoading(true)
                android.util.Log.d(TAG, "Adding stock $symbol to watchlist $watchlistId")

                if (!isValidSymbolFormat(symbol)) {
                    setError("Invalid symbol format. Please enter a valid ticker symbol.")
                    setLoading(false)
                    return@launch
                }

                if (isInvalidSymbol(symbol)) {
                    setError("Cannot add invalid stock symbol '$symbol' to watchlist")
                    setLoading(false)
                    return@launch
                }

                val currentStocks = _uiState.value.stocks
                if (currentStocks.any { it.symbol.equals(symbol, ignoreCase = true) }) {
                    setError("$symbol is already in this watchlist")
                    setLoading(false)
                    return@launch
                }

                val isValidStock = validateStockSymbol(symbol)

                if (!isValidStock) {
                    setError("Stock symbol '$symbol' not found. Please check and try again.")
                    setLoading(false)
                    return@launch
                }

                val success = watchlistRepository.addStockToWatchlist(watchlistId, symbol)
                if (success) {
                    stockRepository.updateWatchlistStatus(symbol, true)
                    setSuccess("Added $symbol to watchlist")
                } else {
                    setError("Failed to add $symbol to watchlist")
                }
                setLoading(false)
            } catch (e: Exception) {
                setError("Failed to add stock: ${e.message}")
                setLoading(false)
            }
        }
    }

    fun removeStockFromWatchlist(watchlistId: Long, symbol: String) {
        viewModelScope.launch {
            try {
                val success = watchlistRepository.removeStockFromWatchlist(watchlistId, symbol)
                if (success) {
                    val isInOtherWatchlist = watchlistRepository.isStockInAnyWatchlist(symbol)
                    if (!isInOtherWatchlist) {
                        stockRepository.updateWatchlistStatus(symbol, false)
                    }
                    setSuccess("Removed $symbol from watchlist")
                } else {
                    setError("Failed to remove $symbol from watchlist")
                }
            } catch (e: Exception) {
                setError("Failed to remove stock: ${e.message}")
            }
        }
    }

    private fun isValidSymbolFormat(symbol: String): Boolean {
        val symbolRegex = "^[A-Za-z0-9.\\-+]{1,10}$".toRegex()
        return symbol.matches(symbolRegex)
    }

    private fun isInvalidSymbol(symbol: String): Boolean {
        val invalidSymbols = setOf(
            "DARSHI", "INVALID", "TEST", "MOCK", "DUMMY", "SAMPLE", "EXAMPLE",
            "FAKE", "NULL", "UNDEFINED", "NONE", "ERROR", "TEMP"
        )
        return invalidSymbols.contains(symbol.uppercase())
    }

    private suspend fun validateStockSymbol(symbol: String): Boolean {
        val upperSymbol = symbol.uppercase()

        if (upperSymbol in KNOWN_VALID_STOCKS) {
            android.util.Log.d(TAG, "Stock $upperSymbol found in known stocks database")
            return true
        }

        return try {
            android.util.Log.d(TAG, "Validating stock $upperSymbol with API")

            val result = withTimeoutOrNull(5000) {
                stockRepository.getTopGainersAndLosers(forceRefresh = false).first()
            }

            when (result) {
                is NetworkResult.Success -> {
                    val allStocks = result.data.topGainers + result.data.topLosers + result.data.mostActivelyTraded
                    val found = allStocks.any { it.ticker.equals(upperSymbol, ignoreCase = true) }

                    if (found) {
                        android.util.Log.d(TAG, "Stock $upperSymbol found in API data")
                        return true
                    }
                }
                is NetworkResult.Error -> {
                    android.util.Log.w(TAG, "API Error during validation: ${result.message}")
                }
                else -> {
                    android.util.Log.w(TAG, "API call failed or timed out for validation")
                }
            }

            val cachedStock = stockRepository.getStock(upperSymbol).first()
            if (cachedStock != null) {
                android.util.Log.d(TAG, "Stock $upperSymbol found in cache")
                return true
            }

            if (upperSymbol in KNOWN_VALID_STOCKS) {
                android.util.Log.d(TAG, "Allowing known stock $upperSymbol despite API failure")
                return true
            }

            android.util.Log.d(TAG, "Stock $upperSymbol not found in any source")
            false
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error validating symbol $symbol", e)
            upperSymbol in KNOWN_VALID_STOCKS
        }
    }

    fun deleteWatchlist(watchlistId: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                setLoading(true)
                val watchlist = watchlistRepository.getWatchlist(watchlistId)
                if (watchlist != null) {
                    watchlistRepository.deleteWatchlist(watchlist)
                    setSuccess("Watchlist '${watchlist.name}' deleted successfully")
                    onSuccess()
                } else {
                    setError("Watchlist not found")
                }
                setLoading(false)
            } catch (e: Exception) {
                setError("Failed to delete watchlist: ${e.message}")
                setLoading(false)
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

data class WatchlistDetailUiState(
    val stocks: List<Stock> = emptyList()
) 