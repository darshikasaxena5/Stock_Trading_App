package com.stocktrading.app.data.repository

import com.stocktrading.app.data.database.StockDao
import com.stocktrading.app.data.database.WatchlistDao
import com.stocktrading.app.data.models.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WatchlistRepository @Inject constructor(
    private val watchlistDao: WatchlistDao,
    private val stockDao: StockDao
) {

    fun getAllWatchlists(): Flow<List<Watchlist>> {
        return watchlistDao.getAllWatchlists()
    }


    fun getAllWatchlistsWithStocks(): Flow<List<WatchlistWithStocks>> {
        return watchlistDao.getAllWatchlistsWithStocks().map { results ->
            results.map { it.toWatchlistWithStocks() }
        }
    }


    fun getStocksInWatchlist(watchlistId: Long): Flow<List<Stock>> {
        return watchlistDao.getStocksInWatchlist(watchlistId)
    }


    suspend fun createWatchlist(name: String): Long {
        val watchlist = Watchlist(name = name)
        return watchlistDao.insertWatchlist(watchlist)
    }


    suspend fun deleteWatchlist(watchlist: Watchlist) {
        watchlistDao.deleteWatchlist(watchlist)
    }

    suspend fun addStockToWatchlist(watchlistId: Long, stockSymbol: String): Boolean {
        return try {
            val existingStock = stockDao.getStock(stockSymbol)
            if (existingStock == null) {
                val basicStock = Stock(
                    symbol = stockSymbol,
                    name = stockSymbol
                )
                stockDao.insertStock(basicStock)
            }

            val watchlistStock = WatchlistStock(
                watchlistId = watchlistId,
                stockSymbol = stockSymbol
            )
            watchlistDao.insertWatchlistStock(watchlistStock)

            stockDao.updateWatchlistStatus(stockSymbol, true)

            watchlistDao.updateWatchlistStockCount(watchlistId)

            true
        } catch (e: Exception) {
            false
        }
    }


    suspend fun removeStockFromWatchlist(watchlistId: Long, stockSymbol: String): Boolean {
        return try {
            watchlistDao.removeStockFromWatchlist(watchlistId, stockSymbol)

            val isInOtherWatchlist = watchlistDao.isStockInAnyWatchlist(stockSymbol)
            if (!isInOtherWatchlist) {
                stockDao.updateWatchlistStatus(stockSymbol, false)
            }

            watchlistDao.updateWatchlistStockCount(watchlistId)

            true
        } catch (e: Exception) {
            false
        }
    }


    suspend fun isStockInAnyWatchlist(stockSymbol: String): Boolean {
        return watchlistDao.isStockInAnyWatchlist(stockSymbol)
    }


    suspend fun getWatchlistsContainingStock(stockSymbol: String): List<Watchlist> {
        return watchlistDao.getWatchlistsContainingStock(stockSymbol)
    }


    suspend fun getWatchlist(id: Long): Watchlist? {
        return watchlistDao.getWatchlist(id)
    }


}