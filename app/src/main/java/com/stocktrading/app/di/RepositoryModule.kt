package com.stocktrading.app.di


import com.stocktrading.app.data.api.AlphaVantageApi
import com.stocktrading.app.data.database.StockDao
import com.stocktrading.app.data.database.WatchlistDao
import com.stocktrading.app.data.repository.StockRepository
import com.stocktrading.app.data.repository.WatchlistRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    

    @Provides
    @Singleton
    fun provideStockRepository(
        api: AlphaVantageApi,
        stockDao: StockDao
    ): StockRepository {
        return StockRepository(api, stockDao)
    }
    

    @Provides
    @Singleton
    fun provideWatchlistRepository(
        watchlistDao: WatchlistDao,
        stockDao: StockDao
    ): WatchlistRepository {
        return WatchlistRepository(watchlistDao, stockDao)
    }

}