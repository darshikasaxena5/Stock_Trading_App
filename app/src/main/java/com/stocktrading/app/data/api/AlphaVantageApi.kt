package com.stocktrading.app.data.api
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

import com.stocktrading.app.data.models.TopGainersLosersResponse
import com.stocktrading.app.data.models.CompanyOverview
import com.stocktrading.app.data.models.TimeSeriesResponse

interface AlphaVantageApi {

    @GET("query")
    suspend fun getTopGainersLosers(
        @Query("function") function: String = "TOP_GAINERS_LOSERS"
    ): Response<TopGainersLosersResponse>


    @GET("query")
    suspend fun getCompanyOverview(
        @Query("function") function: String = "OVERVIEW",
        @Query("symbol") symbol: String
    ): Response<CompanyOverview>

}