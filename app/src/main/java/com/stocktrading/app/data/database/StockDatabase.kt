package com.stocktrading.app.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.stocktrading.app.data.models.Stock
import com.stocktrading.app.data.models.Watchlist
import com.stocktrading.app.data.models.WatchlistStock

@Database(
    entities = [
        Stock::class,
        Watchlist::class,
        WatchlistStock::class
    ],
    version = 1,
    exportSchema = false
)
abstract class StockDatabase : RoomDatabase() {
    
    abstract fun stockDao(): StockDao
    abstract fun watchlistDao(): WatchlistDao
    
    companion object {
        const val DATABASE_NAME = "stock_database"
    }
}