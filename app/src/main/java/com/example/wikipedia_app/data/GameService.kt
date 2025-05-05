package com.example.wikipedia_app.data

import com.example.wikipedia_app.model.Article
import com.example.wikipedia_app.model.WikiLink
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.URLEncoder
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

class GameService {
    private val baseUrl = "https://en.wikipedia.org/wiki/"
    private val timeout = 10L // seconds
    private val maxLinks = 100 // Limit number of links to prevent overload

    suspend fun getRandomArticle(): Article = withContext(Dispatchers.IO) {
        try {
            withTimeout(TimeUnit.SECONDS.toMillis(timeout)) {
                val url = "https://en.wikipedia.org/wiki/Special:Random"
                val doc = Jsoup.connect(url)
                    .timeout(TimeUnit.SECONDS.toMillis(timeout).toInt())
                    .get()
                parseArticle(doc)
            }
        } catch (e: Exception) {
            when (e) {
                is UnknownHostException -> throw GameException("No internet connection")
                else -> throw GameException("Failed to load article: ${e.message}")
            }
        }
    }

    suspend fun getArticle(title: String): Article = withContext(Dispatchers.IO) {
        try {
            withTimeout(TimeUnit.SECONDS.toMillis(timeout)) {
                val encodedTitle = URLEncoder.encode(title, "UTF-8")
                val url = "$baseUrl$encodedTitle"
                val doc = Jsoup.connect(url)
                    .timeout(TimeUnit.SECONDS.toMillis(timeout).toInt())
                    .get()
                parseArticle(doc)
            }
        } catch (e: Exception) {
            when (e) {
                is UnknownHostException -> throw GameException("No internet connection")
                else -> throw GameException("Failed to load article: ${e.message}")
            }
        }
    }

    private fun parseArticle(doc: Document): Article {
        val title = doc.select("h1#firstHeading").text()
        val content = doc.select("div#mw-content-text").text()
        val links = extractLinks(doc)
        return Article(title, content, links)
    }

    private fun extractLinks(doc: Document): List<WikiLink> {
        return doc.select("div#mw-content-text a[href^='/wiki/']")
            .toList() // <-- This fixes the error!
            .filter { !it.attr("href").contains(":") }
            .filter { !it.attr("href").contains("#") }
            .filter { it.text().isNotBlank() }
            .filter { !it.text().contains("edit") }
            .filter { !it.text().contains("citation needed") }
            .take(maxLinks)
            .map { element ->
                WikiLink(
                    text = element.text(),
                    target = element.attr("href").substring(6)
                )
            }
            .distinct()
    }
}

class GameException(message: String) : Exception(message)