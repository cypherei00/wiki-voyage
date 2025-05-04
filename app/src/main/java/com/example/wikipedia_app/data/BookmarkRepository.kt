package com.example.wikipedia_app.data

import kotlinx.coroutines.flow.Flow

class BookmarkRepository(private val bookmarkDao: BookmarkDao) {
    val allBookmarks: Flow<List<Bookmark>> = bookmarkDao.getAllBookmarks()

    suspend fun addBookmark(bookmark: Bookmark) {
        bookmarkDao.insertBookmark(bookmark)
    }

    suspend fun removeBookmark(bookmark: Bookmark) {
        bookmarkDao.deleteBookmark(bookmark)
    }

    suspend fun isBookmarked(title: String): Boolean {
        return bookmarkDao.getBookmarkByTitle(title) != null
    }
} 