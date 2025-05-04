package com.example.wikipedia_app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wikipedia_app.data.History
import com.example.wikipedia_app.data.HistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HistoryViewModel(private val repository: HistoryRepository) : ViewModel() {
    private val _history = MutableStateFlow<List<History>>(emptyList())
    val history: StateFlow<List<History>> = _history.asStateFlow()

    init {
        viewModelScope.launch {
            repository.allHistory.collect { historyList ->
                _history.value = historyList
            }
        }
    }

    fun addToHistory(title: String, url: String) {
        viewModelScope.launch {
            repository.addToHistory(History(title = title, url = url))
        }
    }

    fun deleteHistory(history: History) {
        viewModelScope.launch {
            repository.deleteHistory(history)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }
} 