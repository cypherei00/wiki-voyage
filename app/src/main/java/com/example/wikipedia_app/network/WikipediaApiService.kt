package com.example.wikipedia_app.network

import com.example.wikipedia_app.model.ArticleResponse
import com.example.wikipedia_app.model.WikipediaResponse
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Call

interface WikipediaApiService {
    @GET("w/api.php?action=query&list=search&format=json")
    fun searchArticles(
        @Query("srsearch") query: String,
        @Query("srlimit") limit: Int = 10,
        @Query("sroffset") offset: Int = 0,
        @Query("srwhat") what: String = "text",
        @Query("srinfo") info: String = "totalhits|suggestion"
    ): Call<WikipediaResponse>

    @GET("w/api.php?action=parse&format=json")
    fun getArticleContent(
        @Query("page") title: String,
        @Query("section") section: Int? = null,
        @Query("redirects") redirects: Boolean = true,
        @Query("prop") props: String = "text|sections|displaytitle",
        @Query("formatversion") formatVersion: Int = 2,
        @Query("mobileformat") mobileFormat: Boolean = true,
        @Query("disableeditsection") disableEditSection: Boolean = true,
        @Query("disabletoc") disableToc: Boolean = false
    ): Call<ArticleResponse>
}