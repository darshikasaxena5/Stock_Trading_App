package com.stocktrading.app.ui.explore

import androidx.lifecycle.viewModelScope
import com.stocktrading.app.data.models.Stock
import com.stocktrading.app.data.models.NetworkResult
import com.stocktrading.app.data.repository.StockRepository
import com.stocktrading.app.ui.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val stockRepository: StockRepository
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(ExploreUiState())
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()

    init {
        loadOnlyCleanStaticData()
    }

    private fun loadOnlyCleanStaticData() {
        viewModelScope.launch {
            try {
                val cleanData = stockRepository.getCleanStaticData()
                
                val gainers = cleanData.topGainers.map { quote ->
                    Stock(
                        symbol = quote.ticker,
                        name = getStockName(quote.ticker),
                        price = quote.price,
                        change = quote.changeAmount,
                        changePercent = quote.changePercentage,
                        volume = quote.volume
                    )
                }
                
                val losers = cleanData.topLosers.map { quote ->
                    Stock(
                        symbol = quote.ticker,
                        name = getStockName(quote.ticker),
                        price = quote.price,
                        change = quote.changeAmount,
                        changePercent = quote.changePercentage,
                        volume = quote.volume
                    )
                }
                
                val active = cleanData.mostActivelyTraded.map { quote ->
                    Stock(
                        symbol = quote.ticker,
                        name = getStockName(quote.ticker),
                        price = quote.price,
                        change = quote.changeAmount,
                        changePercent = quote.changePercentage,
                        volume = quote.volume
                    )
                }
                
                android.util.Log.d("ExploreViewModel", " Loaded clean static data - Gainers: ${gainers.size}, Losers: ${losers.size}, Active: ${active.size}")
                
                _uiState.value = _uiState.value.copy(
                    topGainers = gainers,
                    topLosers = losers,
                    mostActive = active,
                    lastUpdated = " Clean Demo Data - Pull to refresh for live data",
                    dataSource = DataSource.STATIC_DEMO
                )
                
            } catch (e: Exception) {
                android.util.Log.e("ExploreViewModel", "Error loading clean static data", e)
                loadManualStaticData()
            }
        }
    }

    private fun loadManualStaticData() {
        android.util.Log.d("ExploreViewModel", " Loading manual static data as fallback")
        _uiState.value = _uiState.value.copy(
            topGainers = getManualGainers(),
            topLosers = getManualLosers(),
            mostActive = getManualActive(),
            lastUpdated = " Manual Demo Data - Pull to refresh for live data",
            dataSource = DataSource.STATIC_DEMO
        )
    }

    private fun getManualGainers(): List<Stock> {
        return listOf(
            Stock("AAPL", "Apple Inc.", "175.25", "+2.34", "+1.36%", "45.2M"),
            Stock("MSFT", "Microsoft Corp.", "305.18", "+5.67", "+1.89%", "32.1M"),
            Stock("GOOGL", "Alphabet Inc.", "2750.80", "+45.20", "+1.67%", "28.5M"),
            Stock("TSLA", "Tesla Inc.", "245.45", "+6.15", "+2.57%", "55.8M"),
            Stock("NVDA", "NVIDIA Corp.", "420.75", "+8.90", "+2.16%", "41.2M"),
            Stock("META", "Meta Platforms", "385.45", "+7.25", "+1.91%", "38.7M"),
            Stock("NFLX", "Netflix Inc.", "540.30", "+12.70", "+2.41%", "22.4M")
        )
    }

    private fun getManualLosers(): List<Stock> {
        return listOf(
            Stock("INTC", "Intel Corp.", "52.30", "-1.20", "-2.24%", "78.9M"),
            Stock("IBM", "IBM Corp.", "140.25", "-3.45", "-2.40%", "12.5M"),
            Stock("F", "Ford Motor Co.", "12.45", "-0.35", "-2.73%", "95.2M"),
            Stock("GE", "General Electric", "108.75", "-2.85", "-2.55%", "25.1M"),
            Stock("XOM", "Exxon Mobil", "110.33", "-2.37", "-2.10%", "15.8M"),
            Stock("BAC", "Bank of America", "35.99", "-0.64", "-1.75%", "45.3M"),
            Stock("CVX", "Chevron Corp.", "155.20", "-2.95", "-1.87%", "18.4M")
        )
    }

    private fun getManualActive(): List<Stock> {
        return listOf(
            Stock("SPY", "SPDR S&P 500", "420.50", "+1.25", "+0.30%", "125.6M"),
            Stock("QQQ", "Invesco QQQ", "350.75", "+2.10", "+0.60%", "89.4M"),
            Stock("AMD", "Advanced Micro Devices", "136.21", "+2.85", "+2.13%", "67.3M"),
            Stock("GME", "GameStop Corp.", "18.87", "+0.48", "+2.62%", "89.3M"),
            Stock("AMC", "AMC Entertainment", "5.48", "+0.13", "+2.43%", "87.9M"),
            Stock("BB", "BlackBerry Ltd.", "5.65", "+0.15", "+2.73%", "68.6M"),
            Stock("RIVN", "Rivian Automotive", "15.24", "+0.28", "+1.87%", "62.4M")
        )
    }


    fun onRefresh() {
        _uiState.value = _uiState.value.copy(isRefreshing = true)
        loadStockData(forceRefresh = true)
    }

    private fun loadStockData(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            setLoading(true)
            clearError()

            stockRepository.getTopGainersAndLosers(forceRefresh)
                .collect { result ->
                    when (result) {
                        is NetworkResult.Loading -> {
                            setLoading(true)
                        }

                        is NetworkResult.Success -> {
                            val data = result.data
                            android.util.Log.d("ExploreViewModel", "📊 Data received - Gainers: ${data.topGainers.size}, Losers: ${data.topLosers.size}, Active: ${data.mostActivelyTraded.size}")

                            val gainers = data.topGainers.map { quote ->
                                Stock(
                                    symbol = quote.ticker,
                                    name = getStockName(quote.ticker),
                                    price = cleanPrice(quote.price),
                                    change = cleanChange(quote.changeAmount),
                                    changePercent = cleanPercentage(quote.changePercentage),
                                    volume = quote.volume
                                )
                            }

                            val losers = data.topLosers.map { quote ->
                                Stock(
                                    symbol = quote.ticker,
                                    name = getStockName(quote.ticker),
                                    price = cleanPrice(quote.price),
                                    change = cleanChange(quote.changeAmount),
                                    changePercent = cleanPercentage(quote.changePercentage),
                                    volume = quote.volume
                                )
                            }

                            val mostActive = data.mostActivelyTraded.map { quote ->
                                Stock(
                                    symbol = quote.ticker,
                                    name = getStockName(quote.ticker),
                                    price = cleanPrice(quote.price),
                                    change = cleanChange(quote.changeAmount),
                                    changePercent = cleanPercentage(quote.changePercentage),
                                    volume = quote.volume
                                )
                            }

                            android.util.Log.d("ExploreViewModel", "✅ Processed - Gainers: ${gainers.size}, Losers: ${losers.size}, Active: ${mostActive.size}")

                            _uiState.value = _uiState.value.copy(
                                topGainers = gainers,
                                topLosers = losers,
                                mostActive = mostActive,
                                lastUpdated = data.lastUpdated,
                                isRefreshing = false,
                                dataSource = determineDataSource(data.lastUpdated)
                            )

                            setLoading(false)

                            if (gainers.isNotEmpty() || losers.isNotEmpty() || mostActive.isNotEmpty()) {
                                // loadCompanyNamesAsync(gainers + losers + mostActive) // Commented to avoid API limits
                            }
                        }

                        is NetworkResult.Error -> {
                            android.util.Log.e("ExploreViewModel", "❌ API Error: ${result.message}")
                            
                            setError("Unable to load stock data: ${result.message}")
                            setLoading(false)
                            _uiState.value = _uiState.value.copy(isRefreshing = false)

                            if (_uiState.value.topGainers.isEmpty() && _uiState.value.topLosers.isEmpty() && _uiState.value.mostActive.isEmpty()) {
                                loadStaticFallbackData()
                            }
                        }
                    }
                }
        }
    }

    private fun getStockName(symbol: String): String {
        return when (symbol.uppercase()) {
            "AAPL" -> "Apple Inc."
            "MSFT" -> "Microsoft Corp."
            "GOOGL" -> "Alphabet Inc."
            "AMZN" -> "Amazon.com Inc."
            "TSLA" -> "Tesla Inc."
            "NVDA" -> "NVIDIA Corp."
            "META" -> "Meta Platforms"
            "NFLX" -> "Netflix Inc."
            "JPM" -> "JPMorgan Chase"
            "JNJ" -> "Johnson & Johnson"
            "PG" -> "Procter & Gamble"
            "KO" -> "Coca-Cola Co."
            "WMT" -> "Walmart Inc."
            "DIS" -> "Walt Disney Co."
            "SPY" -> "SPDR S&P 500"
            "QQQ" -> "Invesco QQQ"
            "AMD" -> "Advanced Micro"
            "INTC" -> "Intel Corp."
            "GME" -> "GameStop Corp."
            "AMC" -> "AMC Entertainment"
            "PLTR" -> "Palantir Tech"
            "BB" -> "BlackBerry Ltd."
            "XOM" -> "Exxon Mobil"
            "BAC" -> "Bank of America"
            "F" -> "Ford Motor Co."
            "GE" -> "General Electric"
            else -> symbol
        }
    }

    private fun cleanPrice(price: String): String {
        return price.replace("$", "").trim()
    }

    private fun cleanChange(change: String): String {
        return change.replace("$", "").trim()
    }

    private fun cleanPercentage(percentage: String): String {
        return percentage.trim()
    }

    private fun determineDataSource(lastUpdated: String): DataSource {
        return when {
            lastUpdated.contains("Market Open", ignoreCase = true) -> DataSource.LIVE_SIMULATION
            lastUpdated.contains("After Hours", ignoreCase = true) -> DataSource.AFTER_HOURS_SIMULATION
            lastUpdated.contains("Weekend", ignoreCase = true) -> DataSource.WEEKEND_SIMULATION
            lastUpdated.contains("Cached", ignoreCase = true) -> DataSource.CACHED
            else -> DataSource.SIMULATION
        }
    }


    private fun loadStaticFallbackData() {
        android.util.Log.d("ExploreViewModel", " Loading static fallback data")
        _uiState.value = _uiState.value.copy(
            topGainers = getManualGainers(),
            topLosers = getManualLosers(),
            mostActive = getManualActive(),
            lastUpdated = " Demo Data (No internet connection)",
            isRefreshing = false,
            dataSource = DataSource.STATIC_DEMO
        )
    }


    override fun onCleared() {
        super.onCleared()
        clearError()
        clearSuccess()
    }
}

data class ExploreUiState(
    val topGainers: List<Stock> = emptyList(),
    val topLosers: List<Stock> = emptyList(),
    val mostActive: List<Stock> = emptyList(),
    val isRefreshing: Boolean = false,
    val lastUpdated: String = "",
    val dataSource: DataSource = DataSource.LOADING
)

enum class DataSource {
    LOADING,
    LIVE_SIMULATION,
    AFTER_HOURS_SIMULATION,
    WEEKEND_SIMULATION,
    CACHED,
    SIMULATION,
    STATIC_DEMO
}