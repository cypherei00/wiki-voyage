package com.example.wikipedia_app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class Bookmark(
    @PrimaryKey
    val title: String,
    val url: String,
    val timestamp: Long = System.currentTimeMillis()
)