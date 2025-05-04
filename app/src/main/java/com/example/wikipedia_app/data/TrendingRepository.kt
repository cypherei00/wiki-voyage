package com.example.wikipedia_app.data

import android.util.Log
import com.example.wikipedia_app.model.*
import com.example.wikipedia_app.network.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.text.SimpleDateFormat
import java.util.*

class TrendingRepository {
    private val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
    private val calendar = Calendar.getInstance()

    fun getTodayFeaturedContent(): Flow<FeaturedArticleResponse> = flow {
        val today = calendar.time
        val formattedDate = dateFormat.format(today)
        val (year, month, day) = formattedDate.split("/")

        Log.d("TRENDING_REPO", "Fetching featured article for $year-$month-$day")

        val response = RetrofitInstance.api.getFeaturedContent(year, month, day).execute()
        if (response.isSuccessful) {
            val body = response.body()
            Log.d("TRENDING_REPO", "Featured article response: $body")
            emit(body ?: throw Exception("Empty response"))
        } else {
            Log.e("TRENDING_REPO", "Error response: ${response.code()} - ${response.errorBody()?.string()}")
            throw Exception("Failed to fetch featured content: ${response.code()}")
        }
    }.flowOn(Dispatchers.IO)

    fun getTrendingArticles(): Flow<List<TrendingArticleItem>> = flow {
        // Use current date instead of calendar date to avoid future dates
        val today = Date()
        val formattedDate = dateFormat.format(today)
        val (year, month, day) = formattedDate.split("/")

        Log.d("TRENDING_REPO", "Fetching trending articles for $year-$month-$day")

        val response = RetrofitInstance.api.getTrendingArticles(year, month, day).execute()
        if (response.isSuccessful) {
            val items = response.body()?.items ?: emptyList()
            Log.d("TRENDING_REPO", "Trending articles count: ${items.size}")
            emit(items)
        } else {
            if (response.code() == 404) {
                // If 404, try with yesterday's date
                val yesterday = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_MONTH, -1)
                }.time
                val yesterdayFormatted = dateFormat.format(yesterday)
                val (yYear, yMonth, yDay) = yesterdayFormatted.split("/")
                
                Log.d("TRENDING_REPO", "Trying yesterday's date: $yYear-$yMonth-$yDay")
                
                val yesterdayResponse = RetrofitInstance.api.getTrendingArticles(yYear, yMonth, yDay).execute()
                if (yesterdayResponse.isSuccessful) {
                    val items = yesterdayResponse.body()?.items ?: emptyList()
                    Log.d("TRENDING_REPO", "Trending articles count (yesterday): ${items.size}")
                    emit(items)
                } else {
                    Log.e("TRENDING_REPO", "Error response for yesterday: ${yesterdayResponse.code()} - ${yesterdayResponse.errorBody()?.string()}")
                    emit(emptyList()) // Return empty list instead of throwing exception
                }
            } else {
                Log.e("TRENDING_REPO", "Error response: ${response.code()} - ${response.errorBody()?.string()}")
                emit(emptyList()) // Return empty list instead of throwing exception
            }
        }
    }.flowOn(Dispatchers.IO)
}