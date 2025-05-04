package com.example.wikipedia_app.data

import kotlinx.coroutines.flow.Flow

class HistoryRepository(private val historyDao: HistoryDao) {
    val allHistory: Flow<List<History>> = historyDao.getAllHistory()

    suspend fun addToHistory(history: History) {
        historyDao.insertHistory(history)
    }

    suspend fun deleteHistory(history: History) {
        historyDao.deleteHistory(history)
    }

    suspend fun clearHistory() {
        historyDao.clearHistory()
    }
} 