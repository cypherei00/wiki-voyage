package com.example.wikipedia_app.network

import android.util.Log

object ApiConfig {
    private const val TAG = "ApiConfig"

    private val languageToUrl = mapOf(
        "en" to "https://en.wikipedia.org/",
        "es" to "https://es.wikipedia.org/",
        "fr" to "https://fr.wikipedia.org/",
        "de" to "https://de.wikipedia.org/",
        "it" to "https://it.wikipedia.org/",
        "pt" to "https://pt.wikipedia.org/",
        "ru" to "https://ru.wikipedia.org/",
        "zh" to "https://zh.wikipedia.org/",
        "ja" to "https://ja.wikipedia.org/",
        "ko" to "https://ko.wikipedia.org/"
    )

    private var currentLanguage = "en"

    init {
        Log.d(TAG, "ApiConfig initialized with default language: $currentLanguage")
        Log.d(TAG, "Available languages: ${languageToUrl.keys.joinToString()}")
    }

    fun setLanguage(languageCode: String) {
        Log.d(TAG, "Attempting to set language to: $languageCode")
        if (languageToUrl.containsKey(languageCode)) {
            val oldLanguage = currentLanguage
            currentLanguage = languageCode
            Log.d(TAG, "Language changed from $oldLanguage to $currentLanguage")
            Log.d(TAG, "New base URL: ${languageToUrl[currentLanguage]}")
            
            // Recreate Retrofit instance with new base URL
            RetrofitInstance.recreateRetrofit()
            Log.d(TAG, "Retrofit instance recreated with new base URL")
        } else {
            Log.w(TAG, "Invalid language code: $languageCode. Available languages: ${languageToUrl.keys.joinToString()}")
        }
    }

    val WIKIPEDIA_BASE_URL: String
        get() {
            val url = languageToUrl[currentLanguage] ?: languageToUrl["en"]!!
            Log.d(TAG, "Getting base URL for language $currentLanguage: $url")
            return url
        }
} 