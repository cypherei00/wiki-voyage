package com.example.wikipedia_app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wikipedia_app.data.TrendingRepository
import com.example.wikipedia_app.model.FeaturedArticleResponse
import com.example.wikipedia_app.model.TrendingArticleItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TrendingViewModel(private val repository: TrendingRepository) : ViewModel() {
    private val _featuredContent = MutableStateFlow<FeaturedArticleResponse?>(null)
    val featuredContent: StateFlow<FeaturedArticleResponse?> = _featuredContent.asStateFlow()

    private val _trendingArticles = MutableStateFlow<List<TrendingArticleItem>>(emptyList())
    val trendingArticles: StateFlow<List<TrendingArticleItem>> = _trendingArticles.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadTrendingContent()
    }

    private fun loadTrendingContent() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                // Load featured content
                repository.getTodayFeaturedContent().collect { response ->
                    _featuredContent.value = response
                }

                // Load trending articles
                repository.getTrendingArticles().collect { articles ->
                    _trendingArticles.value = articles
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "An error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refresh() {
        loadTrendingContent()
    }
} 