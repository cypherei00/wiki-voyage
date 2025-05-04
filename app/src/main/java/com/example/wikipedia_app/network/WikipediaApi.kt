package com.example.wikipedia_app.network

import com.example.wikipedia_app.model.*
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface WikipediaApi {
    // ... existing endpoints ...

    @GET("api/rest_v1/feed/featured/{year}/{month}/{day}")
    fun getFeaturedContent(
        @Path("year") year: String,
        @Path("month") month: String,
        @Path("day") day: String
    ): Call<FeaturedArticleResponse>

    @GET("api/rest_v1/metrics/pageviews/top-per-country/{country}/all-access/{year}/{month}/{day}")
    fun getTrendingArticles(
        @Path("country") country: String = "US",
        @Path("year") year: String,
        @Path("month") month: String,
        @Path("day") day: String
    ): Call<TrendingArticleResponse>
} 