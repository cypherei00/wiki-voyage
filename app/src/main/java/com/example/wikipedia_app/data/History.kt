package com.example.wikipedia_app.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "history")
data class History(
    @PrimaryKey
    val title: String,
    val url: String,
    val timestamp: Date = Date()
) 