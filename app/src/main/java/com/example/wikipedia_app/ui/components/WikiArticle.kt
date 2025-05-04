package com.example.wikipedia_app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.*
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.wikipedia_app.model.Article
import com.example.wikipedia_app.model.WikiLink

@Composable
fun WikiArticle(
    article: Article,
    onLinkClick: (WikiLink) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            Text(
                text = article.title,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        
        item {
            val annotatedString = buildAnnotatedString {
                var currentText = article.content
                var currentIndex = 0
                
                while (currentIndex < currentText.length) {
                    val nextLink = article.links.minByOrNull { link ->
                        val index = currentText.indexOf(link.text, currentIndex)
                        if (index >= 0) index else Int.MAX_VALUE
                    }
                    
                    if (nextLink != null) {
                        val linkIndex = currentText.indexOf(nextLink.text, currentIndex)
                        if (linkIndex >= 0) {
                            // Add text before the link
                            append(currentText.substring(currentIndex, linkIndex))
                            
                            // Add the link with annotation
                            pushStringAnnotation(
                                tag = "link",
                                annotation = nextLink.target
                            )
                            withStyle(
                                style = SpanStyle(
                                    color = MaterialTheme.colorScheme.primary,
                                    textDecoration = TextDecoration.Underline
                                )
                            ) {
                                append(nextLink.text)
                            }
                            pop()
                            
                            currentIndex = linkIndex + nextLink.text.length
                        } else {
                            append(currentText.substring(currentIndex))
                            currentIndex = currentText.length
                        }
                    } else {
                        append(currentText.substring(currentIndex))
                        currentIndex = currentText.length
                    }
                }
            }
            
            ClickableText(
                text = annotatedString,
                style = MaterialTheme.typography.bodyLarge,
                onClick = { offset ->
                    annotatedString.getStringAnnotations(
                        tag = "link",
                        start = offset,
                        end = offset
                    ).firstOrNull()?.let { annotation ->
                        val link = article.links.find { it.target == annotation.item }
                        link?.let { onLinkClick(it) }
                    }
                }
            )
        }
    }
} 