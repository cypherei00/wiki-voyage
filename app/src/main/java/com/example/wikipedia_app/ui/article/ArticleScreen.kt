package com.example.wikipedia_app.ui.article

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.wikipedia_app.model.ArticleContent
import com.example.wikipedia_app.model.ArticleResponse
import com.example.wikipedia_app.model.ArticleSection
import com.example.wikipedia_app.model.Section
import com.example.wikipedia_app.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.util.Log
import android.text.Html
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import com.example.wikipedia_app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleScreen(title: String, navController: NavController) {
    var articleContent by remember { mutableStateOf<ArticleContent?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(title) {
        isLoading = true
        RetrofitInstance.api.getArticleContent(title).enqueue(object : Callback<ArticleResponse> {
            override fun onResponse(call: Call<ArticleResponse>, response: Response<ArticleResponse>) {
                isLoading = false
                if (response.isSuccessful) {
                    val articleResponse = response.body()
                    
                    when {
                        articleResponse?.error != null -> {
                            Toast.makeText(context, articleResponse.error.info, Toast.LENGTH_SHORT).show()
                            Log.e("ARTICLE_ERROR", "Error: ${articleResponse.error.code} - ${articleResponse.error.info}")
                        }
                        articleResponse?.parse != null -> {
                            val sections = articleResponse.parse.sections ?: emptyList()
                            
                            if (sections.isNotEmpty()) {
                                // Process the sections into a structured format
                                val processedContent = processArticleContent(
                                    title = articleResponse.parse.displaytitle ?: title,
                                    sections = sections
                                )
                                articleContent = processedContent
                                
                                // Fetch content for each section
                                fetchSectionContents(processedContent.sections, title)
                            } else {
                                Toast.makeText(context, "No sections available", Toast.LENGTH_SHORT).show()
                                Log.e("ARTICLE_ERROR", "No sections in response")
                            }
                        }
                        else -> {
                            Toast.makeText(context, "No content available", Toast.LENGTH_SHORT).show()
                            Log.e("ARTICLE_ERROR", "No content in response")
                        }
                    }
                } else {
                    Toast.makeText(context, "Failed to load article: ${response.code()}", Toast.LENGTH_SHORT).show()
                    Log.e("ARTICLE_ERROR", "Error fetching article. Code: ${response.code()}, ErrorBody: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ArticleResponse>, t: Throwable) {
                isLoading = false
                Toast.makeText(context, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("ARTICLE_ERROR", "Network Failure: ${t.message}")
            }
        })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(articleContent?.title ?: title) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                articleContent?.sections?.let { sections ->
                    items(sections) { section ->
                        ArticleSection(section = section)
                    }
                }
            }
        }
    }
}

@Composable
fun ArticleSection(section: ArticleSection) {
    // Use rememberSaveable to persist the expanded state
    var isExpanded by rememberSaveable(section.title) { 
        mutableStateOf(section.level <= 2) 
    }
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Section title with different styling based on level
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { 
                    isExpanded = !isExpanded 
                }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = section.title,
                style = when (section.level) {
                    1 -> MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    2 -> MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                    3 -> MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                    else -> MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Medium
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .background(
                        if (section.level == 1) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                        else Color.Transparent
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Expand/Collapse icon
            if (section.subsections.isNotEmpty() || section.content.isNotEmpty()) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse section" else "Expand section",
                    tint = primaryColor,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
        }

        // Animated visibility for smooth transitions
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column {
                // Section content with improved formatting
                if (section.content.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Text(
                            text = buildAnnotatedString {
                                val paragraphs = section.content.split("\n\n")
                                paragraphs.forEachIndexed { index, paragraph ->
                                    if (index > 0) append("\n\n")
                                    withStyle(
                                        style = ParagraphStyle(
                                            lineHeight = 24.sp,
                                            textIndent = TextIndent(firstLine = 16.sp)
                                        )
                                    ) {
                                        withStyle(
                                            style = SpanStyle(
                                                fontSize = 16.sp
                                            )
                                        ) {
                                            // Handle markdown-style formatting
                                            var currentText = paragraph
                                            
                                            // Handle bullet points
                                            if (currentText.startsWith("• ")) {
                                                withStyle(SpanStyle(fontSize = 20.sp)) {
                                                    append("• ")
                                                }
                                                currentText = currentText.substring(2)
                                            }

                                            // Handle bold text
                                            while (currentText.contains("**")) {
                                                val start = currentText.indexOf("**")
                                                val end = currentText.indexOf("**", start + 2)
                                                if (end != -1) {
                                                    append(currentText.substring(0, start))
                                                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                                        append(currentText.substring(start + 2, end))
                                                    }
                                                    currentText = currentText.substring(end + 2)
                                                } else {
                                                    break
                                                }
                                            }

                                            // Handle italic text (including scientific names)
                                            while (currentText.contains("*")) {
                                                val start = currentText.indexOf("*")
                                                val end = currentText.indexOf("*", start + 1)
                                                if (end != -1) {
                                                    append(currentText.substring(0, start))
                                                    val italicText = currentText.substring(start + 1, end)
                                                    withStyle(SpanStyle(
                                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                                        color = if (italicText.matches(Regex("[A-Z][a-z]+ [a-z]+|[A-Z][a-z]+"))) 
                                                            primaryColor
                                                        else 
                                                            onSurfaceColor
                                                    )) {
                                                        append(italicText)
                                                    }
                                                    currentText = currentText.substring(end + 1)
                                                } else {
                                                    break
                                                }
                                            }
                                            append(currentText)
                                        }
                                    }
                                }
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                // Subsections with proper indentation
                section.subsections.forEach { subsection ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .padding(start = (subsection.level - 1) * 16.dp)
                    ) {
                        ArticleSection(section = subsection)
                    }
                }
            }
        }
    }
}

private fun fetchSectionContents(sections: List<ArticleSection>, title: String) {
    sections.forEach { section ->
        // Fetch content for this section using the section index
        RetrofitInstance.api.getArticleContent(
            title = title,
            section = section.sectionIndex
        ).enqueue(object : Callback<ArticleResponse> {
            override fun onResponse(call: Call<ArticleResponse>, response: Response<ArticleResponse>) {
                if (response.isSuccessful) {
                    val articleResponse = response.body()
                    if (articleResponse?.parse?.text != null) {
                        // Update the section content
                        section.content = processHtmlContent(articleResponse.parse.text)
                    }
                }
            }

            override fun onFailure(call: Call<ArticleResponse>, t: Throwable) {
                Log.e("SECTION_ERROR", "Failed to fetch section content: ${t.message}")
            }
        })

        // Recursively fetch content for subsections
        if (section.subsections.isNotEmpty()) {
            fetchSectionContents(section.subsections, title)
        }
    }
}

private fun processArticleContent(
    title: String,
    sections: List<Section>
): ArticleContent {
    val processedSections = mutableListOf<ArticleSection>()
    
    // Process each section
    sections.forEach { section ->
        val subsection = ArticleSection(
            title = cleanHtmlTags(section.line),
            level = section.toclevel,
            content = "", // Content will be fetched separately
            sectionIndex = section.index.toIntOrNull() ?: 0 // Store the section index
        )
        processedSections.add(subsection)
    }

    return ArticleContent(
        title = cleanHtmlTags(title),
        sections = processedSections
    )
}

private fun cleanHtmlTags(text: String): String {
    return text
        .replace(Regex("<[^>]+>"), "")
        .replace(Regex("&[^;]+;"), "")
        .trim()
}

private fun processHtmlContent(html: String): String {
    // Remove unwanted elements
    var processed = html
        .replace(Regex("<style[^>]*>.*?</style>"), "") // Remove style tags
        .replace(Regex("<script[^>]*>.*?</script>"), "") // Remove script tags
        .replace(Regex("<link[^>]*>"), "") // Remove link tags
        .replace(Regex("<div[^>]*class=\"[^\"]*hatnote[^\"]*\"[^>]*>.*?</div>"), "") // Remove hatnotes
        .replace(Regex("<table[^>]*>.*?</table>"), "") // Remove tables
        .replace(Regex("<img[^>]*>"), "") // Remove images
        .replace(Regex("<span[^>]*>.*?</span>"), "") // Remove spans
        .replace(Regex("<sup[^>]*>.*?</sup>"), "") // Remove superscripts
        .replace(Regex("<i>|</i>"), "*") // Convert italics to markdown
        .replace(Regex("<b>|</b>"), "**") // Convert bold to markdown
        .replace(Regex("<a[^>]*>|</a>"), "") // Remove links but keep their text
        .replace(Regex("<p[^>]*>|</p>"), "\n\n") // Convert paragraphs to newlines
        .replace(Regex("<br[^>]*>"), "\n") // Convert line breaks
        .replace(Regex("<ul>|</ul>"), "\n") // Convert unordered lists
        .replace(Regex("<ol>|</ol>"), "\n") // Convert ordered lists
        .replace(Regex("<li>"), "• ") // Convert list items to bullet points
        .replace(Regex("</li>"), "\n") // End list items with newline
        .replace(Regex("\\[\\d+\\]"), "") // Remove citation numbers
        .replace(Regex("\\s+"), " ") // Normalize whitespace
        .trim()

    // Clean up any remaining HTML tags
    processed = processed.replace(Regex("<[^>]+>"), "")

    // Handle scientific names and terms
    processed = processed
        .replace(Regex("\\*([A-Z][a-z]+ [a-z]+)\\*"), "*$1*") // Format scientific names
        .replace(Regex("\\*([A-Z][a-z]+)\\*"), "*$1*") // Format genus names

    return processed
}

private fun decodeHtml(htmlText: String): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(htmlText, Html.FROM_HTML_MODE_LEGACY).toString()
    } else {
        Html.fromHtml(htmlText).toString()
    }
}