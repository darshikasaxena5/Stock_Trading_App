package com.stocktrading.app.ui.product

import androidx.lifecycle.viewModelScope
import com.stocktrading.app.data.models.ChartPoint
import com.stocktrading.app.data.models.NetworkResult
import com.stocktrading.app.data.models.Stock
import com.stocktrading.app.data.models.Watchlist
import com.stocktrading.app.data.repository.StockRepository
import com.stocktrading.app.data.repository.WatchlistRepository
import com.stocktrading.app.ui.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val stockRepository: StockRepository,
    private val watchlistRepository: WatchlistRepository
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(ProductUiState())
    val uiState: StateFlow<ProductUiState> = _uiState.asStateFlow()

    private var currentSymbol: String = ""

    private fun getStaticFallbackStock(symbol: String): Stock? {
        return when (symbol.uppercase()) {
            "AAPL" -> Stock("AAPL", "Apple Inc.", "175.25", "+2.34", "+1.35%", "45.2M")
            "MSFT" -> Stock("MSFT", "Microsoft Corp.", "305.18", "+5.67", "+1.89%", "32.1M")
            "GOOGL" -> Stock("GOOGL", "Alphabet Inc.", "2750.80", "+45.20", "+1.67%", "28.5M")
            "TSLA" -> Stock("TSLA", "Tesla Inc.", "245.45", "+6.15", "+2.57%", "55.8M")
            "NVDA" -> Stock("NVDA", "NVIDIA Corp.", "420.75", "+8.90", "+2.16%", "41.2M")
            "META" -> Stock("META", "Meta Platforms", "385.45", "+7.25", "+1.91%", "38.7M")
            "NFLX" -> Stock("NFLX", "Netflix Inc.", "540.30", "+12.70", "+2.41%", "22.4M")
            "AMD" -> Stock("AMD", "Advanced Micro Devices", "136.21", "+2.85", "+2.13%", "67.3M")

            "INTC" -> Stock("INTC", "Intel Corp.", "52.30", "-1.20", "-2.24%", "78.9M")
            "IBM" -> Stock("IBM", "IBM Corp.", "140.25", "-3.45", "-2.40%", "12.5M")
            "ORCL" -> Stock("ORCL", "Oracle Corp.", "88.90", "-2.10", "-2.31%", "18.7M")
            "F" -> Stock("F", "Ford Motor Co.", "12.45", "-0.35", "-2.73%", "95.2M")
            "GE" -> Stock("GE", "General Electric", "108.75", "-2.85", "-2.55%", "25.1M")
            "XOM" -> Stock("XOM", "Exxon Mobil", "110.33", "-2.37", "-2.10%", "15.8M")
            "BAC" -> Stock("BAC", "Bank of America", "35.99", "-0.64", "-1.75%", "45.3M")
            "CVX" -> Stock("CVX", "Chevron Corp.", "155.20", "-2.95", "-1.87%", "18.4M")

            "SPY" -> Stock("SPY", "SPDR S&P 500", "420.50", "+1.25", "+0.30%", "125.6M")
            "QQQ" -> Stock("QQQ", "Invesco QQQ", "350.75", "+2.10", "+0.60%", "89.4M")
            "GME" -> Stock("GME", "GameStop Corp.", "18.87", "+0.48", "+2.62%", "89.3M")
            "AMC" -> Stock("AMC", "AMC Entertainment", "5.48", "+0.13", "+2.43%", "87.9M")
            "PLTR" -> Stock("PLTR", "Palantir Tech", "17.86", "+0.37", "+2.11%", "75.2M")
            "BB" -> Stock("BB", "BlackBerry Ltd.", "5.65", "+0.15", "+2.73%", "68.6M")
            "RIVN" -> Stock("RIVN", "Rivian Automotive", "15.24", "+0.28", "+1.87%", "62.4M")
            "LCID" -> Stock("LCID", "Lucid Group Inc.", "10.86", "+0.26", "+2.45%", "58.1M")

            else -> null
        }
    }

    fun loadStockData(symbol: String) {
        if (currentSymbol != symbol) {
            currentSymbol = symbol
            _uiState.value = ProductUiState()
        }

        setLoading(true)
        clearError()

        viewModelScope.launch {
            var dataLoaded = false

            val fallbackStock = getStaticFallbackStock(symbol)
            if (fallbackStock != null) {
                android.util.Log.d("ProductViewModel", "Using fallback data for $symbol")
                _uiState.value = _uiState.value.copy(
                    stock = fallbackStock,
                    isInWatchlist = watchlistRepository.isStockInAnyWatchlist(symbol)
                )
                dataLoaded = true
                _uiState.value = _uiState.value.copy(
                    chartData = generateMockChartData(fallbackStock)
                )
            } else {
                android.util.Log.d("ProductViewModel", "🚀 Using efficient approach for $symbol")
                
                loadStockDataFromMultipleSources(symbol)
                dataLoaded = _uiState.value.stock != null
            }

            if (!dataLoaded) {
                setError("Stock '$symbol' not found. Please check the symbol and try again.")
            } else {
                loadAdditionalData(symbol)
            }

            setLoading(false)
        }
    }

    private suspend fun loadStockDataFromMultipleSources(symbol: String) {
        try {
            val topGainersLosersResult = stockRepository.getTopGainersAndLosers().first()
            if (topGainersLosersResult is NetworkResult.Success) {
                val data = topGainersLosersResult.data
                val allStocks = data.topGainers + data.topLosers + data.mostActivelyTraded
                val foundQuote = allStocks.find { it.ticker.equals(symbol, ignoreCase = true) }

                if (foundQuote != null) {
                    android.util.Log.d("ProductViewModel", "Found $symbol in top gainers/losers")
                    _uiState.value = _uiState.value.copy(
                        stock = foundQuote.toStock(),
                        isInWatchlist = watchlistRepository.isStockInAnyWatchlist(symbol)
                    )
                    return
                }
            }

            val cachedStock = stockRepository.getStock(symbol).first()
            if (cachedStock != null) {
                android.util.Log.d("ProductViewModel", "Found $symbol in cache")
                _uiState.value = _uiState.value.copy(
                    stock = cachedStock,
                    isInWatchlist = cachedStock.isInWatchlist
                )
                return
            }

            android.util.Log.d("ProductViewModel", "🎯 Using fallback data for $symbol")
            val smartStock = Stock(
                symbol = symbol.uppercase(),
                name = "$symbol Inc.",
                price = "0.00",
                change = "0.00",
                changePercent = "0.00%",
                volume = "0",
                marketCap = "N/A",
                sector = "Technology",
                description = "Stock information for $symbol"
            )
            _uiState.value = _uiState.value.copy(
                stock = smartStock,
                isInWatchlist = watchlistRepository.isStockInAnyWatchlist(symbol)
            )
            
            android.util.Log.d("ProductViewModel", "Using static fallback data for $symbol")
            val fallbackStock = getStaticFallbackStock(symbol)
            if (fallbackStock != null) {
                _uiState.value = _uiState.value.copy(
                    stock = fallbackStock,
                    isInWatchlist = watchlistRepository.isStockInAnyWatchlist(symbol)
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("ProductViewModel", "Error loading stock data for $symbol", e)
        }
    }

    private fun loadAdditionalData(symbol: String) {
        viewModelScope.launch {
            try {
                _uiState.value.stock?.let { stock ->
                    _uiState.value = _uiState.value.copy(
                        chartData = generateMockChartData(stock)
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("ProductViewModel", "Error loading chart data", e)
            }

            val isInWatchlist = watchlistRepository.isStockInAnyWatchlist(symbol)
            _uiState.value = _uiState.value.copy(isInWatchlist = isInWatchlist)
        }
    }

    fun toggleWatchlist() {
        viewModelScope.launch {
            val stock = _uiState.value.stock ?: return@launch
            val currentlyInWatchlist = _uiState.value.isInWatchlist

            try {
                if (currentlyInWatchlist) {
                    showWatchlistSelectionDialog()
                } else {
                    showWatchlistSelectionDialog()
                }
            } catch (e: Exception) {
                android.util.Log.e("ProductViewModel", "Error toggling watchlist", e)
                setError("Failed to update watchlist: ${e.message}")
            }
        }
    }

    private suspend fun showWatchlistSelectionDialog() {
        try {
            val stock = _uiState.value.stock ?: return
            val watchlists = watchlistRepository.getAllWatchlists().first()
            val currentWatchlistIds = watchlistRepository.getWatchlistsContainingStock(stock.symbol).map { it.id }
            
            _uiState.value = _uiState.value.copy(
                showWatchlistDialog = true,
                availableWatchlists = watchlists,
                currentWatchlistIds = currentWatchlistIds
            )
        } catch (e: Exception) {
            android.util.Log.e("ProductViewModel", "Error loading watchlists", e)
            setError("Failed to load watchlists: ${e.message}")
        }
    }

    fun hideWatchlistDialog() {
        _uiState.value = _uiState.value.copy(showWatchlistDialog = false)
    }

    fun confirmWatchlistSelection(selectedWatchlistIds: List<Long>) {
        viewModelScope.launch {
            val stock = _uiState.value.stock ?: return@launch
            
            try {
                if (_uiState.value.availableWatchlists.isEmpty() && selectedWatchlistIds.isEmpty()) {
                    val defaultWatchlistId = watchlistRepository.createWatchlist("Default Watchlist")
                    watchlistRepository.addStockToWatchlist(defaultWatchlistId, stock.symbol)
                    stockRepository.updateWatchlistStatus(stock.symbol, true)
                    _uiState.value = _uiState.value.copy(
                        isInWatchlist = true,
                        showWatchlistDialog = false
                    )
                    setSuccess("${stock.symbol} added to Default Watchlist")
                    return@launch
                }

                val currentWatchlistIds = watchlistRepository.getWatchlistsContainingStock(stock.symbol).map { it.id }
                
                val toRemove = currentWatchlistIds - selectedWatchlistIds.toSet()
                toRemove.forEach { watchlistId ->
                    watchlistRepository.removeStockFromWatchlist(watchlistId, stock.symbol)
                }
                
                val toAdd = selectedWatchlistIds - currentWatchlistIds.toSet()
                toAdd.forEach { watchlistId ->
                    watchlistRepository.addStockToWatchlist(watchlistId, stock.symbol)
                }
                
                val isInAnyWatchlist = selectedWatchlistIds.isNotEmpty()
                stockRepository.updateWatchlistStatus(stock.symbol, isInAnyWatchlist)
                
                _uiState.value = _uiState.value.copy(
                    isInWatchlist = isInAnyWatchlist,
                    showWatchlistDialog = false
                )
                
                if (selectedWatchlistIds.isEmpty()) {
                    setSuccess("${stock.symbol} removed from all watchlists")
                } else {
                    setSuccess("${stock.symbol} updated in ${selectedWatchlistIds.size} watchlist(s)")
                }
                
            } catch (e: Exception) {
                android.util.Log.e("ProductViewModel", "Error updating watchlist selection", e)
                setError("Failed to update watchlist: ${e.message}")
            }
        }
    }



    fun createWatchlistAndAddStock(watchlistName: String) {
        viewModelScope.launch {
            val stock = _uiState.value.stock ?: return@launch
            
            try {
                val watchlistId = watchlistRepository.createWatchlist(watchlistName)
                if (watchlistId > 0) {
                    // Add to this new watchlist and maintain existing selections
                    val currentWatchlistIds = _uiState.value.currentWatchlistIds + watchlistId
                    confirmWatchlistSelection(currentWatchlistIds)
                    
                    // Refresh the dialog with updated watchlists
                    showWatchlistSelectionDialog()
                } else {
                    setError("Failed to create watchlist")
                }
            } catch (e: Exception) {
                android.util.Log.e("ProductViewModel", "Error creating watchlist", e)
                setError("Failed to create watchlist: ${e.message}")
            }
        }
    }

    private fun generateMockChartData(stock: Stock): List<ChartPoint> {
        val currentPrice = stock.price.toFloatOrNull() ?: 100f
        val changePercent = stock.changePercent
            .replace("%", "")
            .replace("+", "")
            .toFloatOrNull() ?: 0f

        val chartPoints = mutableListOf<ChartPoint>()
        val calendar = java.util.Calendar.getInstance()
        calendar.add(java.util.Calendar.DAY_OF_MONTH, -30)

        for (i in 0 until 30) {
            val dayProgress = i.toFloat() / 29f
            val trendComponent = (changePercent / 100f) * currentPrice * dayProgress
            val randomVariation = (kotlin.random.Random.nextFloat() - 0.5f) * currentPrice * 0.02f

            val price = currentPrice - (changePercent / 100f * currentPrice) + trendComponent + randomVariation

            chartPoints.add(ChartPoint(
                date = "${calendar.get(java.util.Calendar.YEAR)}-${String.format("%02d", calendar.get(java.util.Calendar.MONTH) + 1)}-${String.format("%02d", calendar.get(java.util.Calendar.DAY_OF_MONTH))}",
                price = price.coerceAtLeast(0.01f),
                timestamp = calendar.timeInMillis
            ))

            calendar.add(java.util.Calendar.DAY_OF_MONTH, 1)
        }

        return chartPoints
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

data class ProductUiState(
    val stock: Stock? = null,
    val chartData: List<ChartPoint> = emptyList(),
    val isInWatchlist: Boolean = false,
    val showWatchlistDialog: Boolean = false,
    val availableWatchlists: List<Watchlist> = emptyList(),
    val currentWatchlistIds: List<Long> = emptyList()
)