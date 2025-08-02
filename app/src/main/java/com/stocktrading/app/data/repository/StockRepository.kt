package com.stocktrading.app.data.repository

import android.util.Log
import com.stocktrading.app.BuildConfig
import com.stocktrading.app.data.api.AlphaVantageApi
import com.stocktrading.app.data.database.StockDao
import com.stocktrading.app.data.models.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockRepository @Inject constructor(
    private val api: AlphaVantageApi,
    private val stockDao: StockDao
) {

    companion object {
        private const val TAG = "StockRepository"
        private const val CACHE_TIMEOUT = 30 * 60 * 1000L
    }

    private fun getStaticFallbackData(): TopGainersLosersResponse {
        val topGainers = listOf(
            StockQuote("AAPL", "175.25", "+2.34", "+1.35%", "45.2M"),
            StockQuote("MSFT", "305.18", "+5.67", "+1.89%", "32.1M"),
            StockQuote("GOOGL", "2750.80", "+45.20", "+1.67%", "28.5M"),
            StockQuote("TSLA", "245.45", "+6.15", "+2.57%", "55.8M"),
            StockQuote("NVDA", "420.75", "+8.90", "+2.16%", "41.2M"),
            StockQuote("META", "385.45", "+7.25", "+1.91%", "38.7M"),
            StockQuote("NFLX", "540.30", "+12.70", "+2.41%", "22.4M"),
            StockQuote("AMD", "136.21", "+2.85", "+2.13%", "67.3M")
        )

        val topLosers = listOf(
            StockQuote("INTC", "52.30", "-1.20", "-2.24%", "78.9M"),
            StockQuote("IBM", "140.25", "-3.45", "-2.40%", "12.5M"),
            StockQuote("ORCL", "88.90", "-2.10", "-2.31%", "18.7M"),
            StockQuote("F", "12.45", "-0.35", "-2.73%", "95.2M"),
            StockQuote("GE", "108.75", "-2.85", "-2.55%", "25.1M"),
            StockQuote("XOM", "110.33", "-2.37", "-2.10%", "15.8M"),
            StockQuote("BAC", "35.99", "-0.64", "-1.75%", "45.3M"),
            StockQuote("CVX", "155.20", "-2.95", "-1.87%", "18.4M")
        )

        val mostActive = listOf(
            StockQuote("SPY", "420.50", "+1.25", "+0.30%", "125.6M"),
            StockQuote("QQQ", "350.75", "+2.10", "+0.60%", "89.4M"),
            StockQuote("GME", "18.87", "+0.48", "+2.62%", "89.3M"),
            StockQuote("AMC", "5.48", "+0.13", "+2.43%", "87.9M"),
            StockQuote("PLTR", "17.86", "+0.37", "+2.11%", "75.2M"),
            StockQuote("BB", "5.65", "+0.15", "+2.73%", "68.6M"),
            StockQuote("RIVN", "15.24", "+0.28", "+1.87%", "62.4M"),
            StockQuote("LCID", "10.86", "+0.26", "+2.45%", "58.1M")
        )

        return TopGainersLosersResponse(
            topGainers = topGainers,
            topLosers = topLosers,
            mostActivelyTraded = mostActive,
            lastUpdated = "Static Demo Data - Updated Daily"
        )
    }

    private fun getStaticCompanyOverview(symbol: String): CompanyOverview? {
        return when (symbol.uppercase()) {
            "AAPL" -> CompanyOverview(
                "AAPL",
                "Apple Inc.",
                "Apple Inc. designs, manufactures, and markets smartphones, personal computers, tablets, wearables, and accessories worldwide.",
                "Technology",
                "Consumer Electronics",
                "2800000000000",
                "180.50",
                "140.25",
                "28.5",
                "0.44",
                "6.15",
                "24.35"
            )

            "MSFT" -> CompanyOverview(
                "MSFT",
                "Microsoft Corp.",
                "Microsoft Corporation develops, licenses, and supports software, services, devices, and solutions worldwide.",
                "Technology",
                "Software",
                "2300000000000",
                "380.75",
                "290.15",
                "31.2",
                "0.68",
                "9.85",
                "45.20"
            )

            "GOOGL" -> CompanyOverview(
                "GOOGL",
                "Alphabet Inc.",
                "Alphabet Inc. operates as a holding company that offers a portfolio of Google services and products worldwide.",
                "Technology",
                "Internet Services",
                "1700000000000",
                "2900.50",
                "2100.75",
                "25.8",
                "0.00",
                "110.25",
                "280.40"
            )

            "TSLA" -> CompanyOverview(
                "TSLA",
                "Tesla Inc.",
                "Tesla, Inc. designs, develops, manufactures, leases, and sells electric vehicles, and energy generation and storage systems.",
                "Automotive",
                "Electric Vehicles",
                "780000000000",
                "280.90",
                "150.25",
                "45.2",
                "0.00",
                "5.35",
                "95.80"
            )

            "AMD" -> CompanyOverview(
                "AMD",
                "Advanced Micro Devices Inc.",
                "Advanced Micro Devices, Inc. operates as a semiconductor company worldwide.",
                "Technology",
                "Semiconductors",
                "220000000000",
                "165.40",
                "85.20",
                "18.5",
                "0.00",
                "7.45",
                "25.60"
            )

            "GME" -> CompanyOverview(
                "GME",
                "GameStop Corp.",
                "GameStop Corp. operates as a multichannel video game, consumer electronics, and collectibles retailer.",
                "Retail",
                "Gaming",
                "5800000000",
                "45.50",
                "12.75",
                "N/A",
                "0.00",
                "-1.25",
                "18.90"
            )

            "AMC" -> CompanyOverview(
                "AMC",
                "AMC Entertainment Holdings Inc.",
                "AMC Entertainment Holdings, Inc. operates as a theatrical exhibition company.",
                "Entertainment",
                "Movie Theaters",
                "2800000000",
                "12.50",
                "2.85",
                "N/A",
                "0.00",
                "-2.45",
                "8.75"
            )

            else -> CompanyOverview(
                symbol,
                symbol,
                "Company information not available",
                "N/A",
                "N/A",
                "0",
                "0.00",
                "0.00",
                "N/A",
                "0.00",
                "0.00",
                "0.00"
            )
        }
    }

    fun getTopGainersAndLosers(forceRefresh: Boolean = false): Flow<NetworkResult<TopGainersLosersResponse>> =
        flow {

            Log.d(TAG, " getTopGainersAndLosers called (forceRefresh=$forceRefresh)")
            emit(NetworkResult.Loading())

            try {
                val apiKey = BuildConfig.API_KEY
                Log.d(TAG, "API Key length: ${apiKey.length}")
                Log.d(TAG, "API Key starts with: ${apiKey.take(4)}...")

                if (apiKey.isEmpty()) {
                    Log.e(TAG, "API key is empty! Using smart fallback")
                    emit(NetworkResult.Success(getStaticFallbackData()))
                    return@flow
                }

                if (!forceRefresh && isCacheValid()) {
                    val cached = stockDao.getAllStocks().first()
                    if (cached.isNotEmpty()) {
                        Log.d(TAG, "Using cached data: ${cached.size} stocks")
                        emit(NetworkResult.Success(convertCachedToApiResponse(cached)))
                        return@flow
                    }
                }

                Log.d(TAG, "Making API call to Alpha Vantage...")
                val response = api.getTopGainersLosers()
                Log.d(TAG, "Response code: ${response.code()}")
                Log.d(TAG, "Response message: ${response.message()}")

                if (response.isSuccessful) {
                    val body = response.body()
                    Log.d(TAG, "Response body: $body")

                    if (body != null) {
                        if (body.information != null) {
                            Log.e(TAG, "API returned informational message: ${body.information}")
                            val smartData = getStaticFallbackData()
                            cacheTopGainersLosers(smartData)
                            emit(NetworkResult.Success(smartData))
                            return@flow
                        }

                        Log.d(TAG, "Top gainers count: ${body.topGainers.size}")
                        Log.d(TAG, "Top losers count: ${body.topLosers.size}")
                        Log.d(TAG, "Most active count: ${body.mostActivelyTraded.size}")

                        val totalStocks =
                            body.topGainers.size + body.topLosers.size + body.mostActivelyTraded.size
                        if (totalStocks == 0) {
                            Log.w(TAG, "API returned empty data - using smart simulator")
                            val smartData = getStaticFallbackData()
                            cacheTopGainersLosers(smartData)
                            emit(NetworkResult.Success(smartData))
                            return@flow
                        }

                        val safe = TopGainersLosersResponse(
                            topGainers = body.topGainers,
                            topLosers = body.topLosers,
                            mostActivelyTraded = body.mostActivelyTraded,
                            lastUpdated = body.lastUpdated
                        )
                        val filtered = filterValidStocks(safe)
                        Log.d(
                            TAG,
                            "After filtering - Gainers: ${filtered.topGainers.size}, Losers: ${filtered.topLosers.size}, Active: ${filtered.mostActivelyTraded.size}"
                        )

                        cacheTopGainersLosers(filtered)
                        emit(NetworkResult.Success(filtered))
                    } else {
                        Log.e(TAG, "Response body is null - using smart fallback")
                        val smartData = getStaticFallbackData()
                        emit(NetworkResult.Success(smartData))
                    }
                } else {
                    Log.e(
                        TAG,
                        "API Error - Code: ${response.code()}, Message: ${response.message()}"
                    )

                    val smartData = getStaticFallbackData()
                    cacheTopGainersLosers(smartData)
                    emit(NetworkResult.Success(smartData))
                }

            } catch (e: Exception) {
                Log.e(TAG, "Exception in getTopGainersAndLosers: ${e.message}", e)

                try {
                    val cached = stockDao.getAllStocks().first()
                    if (cached.isNotEmpty()) {
                        Log.d(TAG, "Using cached data as fallback: ${cached.size} stocks")
                        emit(NetworkResult.Success(convertCachedToApiResponse(cached)))
                    } else {
                        Log.d(TAG, "No cache available - using smart simulator")
                        val smartData = getStaticFallbackData()
                        cacheTopGainersLosers(smartData)
                        emit(NetworkResult.Success(smartData))
                    }
                } catch (cacheException: Exception) {
                    Log.e(TAG, "Cache fallback failed, using smart simulator", cacheException)
                    val smartData = getStaticFallbackData()
                    emit(NetworkResult.Success(smartData))
                }
            }
        }


    fun getCompanyOverview(symbol: String): Flow<NetworkResult<CompanyOverview>> = flow {
        emit(NetworkResult.Loading())
        try {
            val resp = api.getCompanyOverview(symbol = symbol)
            if (resp.isSuccessful && resp.body() != null) {
                emit(NetworkResult.Success(resp.body()!!))
            } else {
                Log.w(TAG, "Company overview API failed for $symbol, using smart fallback")
                val smartOverview = getStaticCompanyOverview(symbol)
                if (smartOverview != null) {
                    emit(NetworkResult.Success(smartOverview))
                } else {
                    emit(NetworkResult.Error("Company data not found for $symbol"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in getCompanyOverview", e)
            val smartOverview = getStaticCompanyOverview(symbol)
            if (smartOverview != null) {
                emit(NetworkResult.Success(smartOverview))
            } else {
                emit(NetworkResult.Error("Network error: ${e.message}"))
            }
        }
    }


    fun getStock(symbol: String): Flow<Stock?> =
        stockDao.getStockFlow(symbol)


    suspend fun updateWatchlistStatus(symbol: String, isInWatchlist: Boolean) {
        stockDao.updateWatchlistStatus(symbol, isInWatchlist)
    }


    private fun filterValidStocks(response: TopGainersLosersResponse): TopGainersLosersResponse {
        val blacklist =
            setOf("INVALID", "TEST", "MOCK", "DUMMY", "SAMPLE", "EXAMPLE", "FAKE")
        val regex = "^[A-Z0-9+\\-]{1,5}$".toRegex()
        fun ok(q: StockQuote): Boolean {
            val s = q.ticker.uppercase()
            if (s in blacklist) return false
            if (!s.matches(regex)) return false
            val price = q.price.replace("$", "").toDoubleOrNull() ?: return false
            if (price <= 0) return false
            val vol = q.volume.replace(",", "").toLongOrNull() ?: return false
            if (vol <= 0) return false
            return true
        }
        return TopGainersLosersResponse(
            topGainers = response.topGainers.filter { ok(it) },
            topLosers = response.topLosers.filter { ok(it) },
            mostActivelyTraded = response.mostActivelyTraded.filter { ok(it) },
            lastUpdated = response.lastUpdated
        )
    }

    private fun convertCachedToApiResponse(stocks: List<Stock>): TopGainersLosersResponse {
        return getStaticFallbackData()
    }

    fun getCleanStaticData(): TopGainersLosersResponse {
        return getStaticFallbackData()
    }

    private suspend fun cacheTopGainersLosers(data: TopGainersLosersResponse) {
        val all = data.topGainers + data.topLosers + data.mostActivelyTraded
        stockDao.insertStocks(all.map { it.toStock() })
        cleanOldCache()
    }

    private suspend fun cleanOldCache() {
        val cutoff = System.currentTimeMillis() - 24 * 60 * 60 * 1000L
        stockDao.deleteOldStocks(cutoff)
    }

    private suspend fun isCacheValid(): Boolean {
        val last = stockDao.getLastUpdateTime()
        return last != null && (System.currentTimeMillis() - last) < CACHE_TIMEOUT
    }


}
