package com.example.wikipedia_app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks ORDER BY timestamp DESC")
    fun getAllBookmarks(): Flow<List<Bookmark>>

    @Insert
    suspend fun insertBookmark(bookmark: Bookmark)

    @Delete
    suspend fun deleteBookmark(bookmark: Bookmark)

    @Query("SELECT * FROM bookmarks WHERE title = :title")
    suspend fun getBookmarkByTitle(title: String): Bookmark?
} 