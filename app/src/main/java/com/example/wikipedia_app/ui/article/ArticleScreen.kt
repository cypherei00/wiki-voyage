package com.example.wikipedia_app.ui.article

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.wikipedia_app.data.Bookmark
import com.example.wikipedia_app.model.ArticleContent
import com.example.wikipedia_app.model.ArticleResponse
import com.example.wikipedia_app.model.ArticleSection
import com.example.wikipedia_app.model.Section
import com.example.wikipedia_app.network.RetrofitInstance
import com.example.wikipedia_app.network.ApiConfig
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
import androidx.compose.foundation.text.ClickableText
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import coil.compose.AsyncImage
import com.example.wikipedia_app.R
import com.example.wikipedia_app.model.ArticleDescriptionResponse
import com.example.wikipedia_app.navigation.Screen
import com.example.wikipedia_app.ui.theme.BackgroundBeige
import com.example.wikipedia_app.ui.theme.CreamOffWhite
import com.example.wikipedia_app.ui.theme.DarkBrown
import com.example.wikipedia_app.ui.theme.TealCyan
import com.example.wikipedia_app.ui.viewmodels.BookmarkViewModel
import com.example.wikipedia_app.ui.viewmodels.HistoryViewModel
import com.example.wikipedia_app.ui.viewmodels.TTSViewModel
import androidx.compose.ui.res.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleScreen(
    navController: NavController,
    title: String,
    viewModel: BookmarkViewModel,
    historyViewModel: HistoryViewModel,
    ttsViewModel: TTSViewModel
) {
    var articleContent by remember { mutableStateOf<ArticleContent?>(null) }
    var articleDescription by remember { mutableStateOf<String?>(null) }
    var articleImages by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var sectionsLoaded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val isBookmarked by viewModel.isBookmarked.collectAsState()
    val isSpeaking by ttsViewModel.isSpeaking.collectAsState()

    LaunchedEffect(title) {
        viewModel.checkBookmarkStatus(title)
        historyViewModel.addToHistory(title, "${ApiConfig.WIKIPEDIA_BASE_URL}wiki/$title")
        isLoading = true
        sectionsLoaded = false
        Log.d("ARTICLE_LOADING", "Starting to load article: $title")
        
        RetrofitInstance.api.getArticleDescription(title).enqueue(object : Callback<ArticleDescriptionResponse> {
            override fun onResponse(call: Call<ArticleDescriptionResponse>, response: Response<ArticleDescriptionResponse>) {
                Log.d("ARTICLE_DESC", "Description response received: ${response.isSuccessful}")
                articleDescription = response.body()?.query?.pages?.firstOrNull()?.extract
                Log.d("ARTICLE_DESC", "Description content: $articleDescription")
            }
            override fun onFailure(call: Call<ArticleDescriptionResponse>, t: Throwable) {
                Log.e("ARTICLE_DESC", "Failed to get description: ${t.message}")
            }
        })

        RetrofitInstance.api.getArticleContent(title).enqueue(object : Callback<ArticleResponse> {
            override fun onResponse(call: Call<ArticleResponse>, response: Response<ArticleResponse>) {
                if (response.isSuccessful) {
                    response.body()?.parse?.let { parse ->
                        Log.d("ARTICLE_CONTENT", "Parse object received: ${parse.sections?.size} sections")
                        
                        articleImages = parse.images
                            ?.filter { it.endsWith(".jpg", true) || it.endsWith(".png", true) }
                            ?.map { "${ApiConfig.WIKIPEDIA_BASE_URL}wiki/Special:FilePath/${it.removePrefix("File:")}" }
                            ?.distinct()
                            ?.take(3)
                            ?: emptyList()

                        Log.d("ARTICLE_IMAGES", "Found ${articleImages.size} images")

                        articleContent = processArticleContent(
                            title = parse.displaytitle ?: title,
                            sections = parse.sections ?: emptyList()
                        )
                        Log.d("ARTICLE_SECTIONS", "Processed ${articleContent?.sections?.size} sections")
                        
                        // Fetch section contents and update loading state
                        fetchSectionContents(articleContent!!.sections, title) {
                            sectionsLoaded = true
                            isLoading = false
                        }
                    }
                } else {
                    Log.e("ARTICLE_CONTENT", "Failed to load article: ${response.code()}")
                    error = "Failed to load article"
                    isLoading = false
                }
            }
            override fun onFailure(call: Call<ArticleResponse>, t: Throwable) {
                Log.e("ARTICLE_CONTENT", "Network Error: ${t.message}")
                error = "Network Error: ${t.message}"
                isLoading = false
            }
        })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "WIKI-VOYAGE",
                        color = CreamOffWhite,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = CreamOffWhite)
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (isSpeaking) {
                                ttsViewModel.stop()
                            } else {
                                val textToSpeak = buildString {
                                    articleDescription?.let { append(it + "\n\n") }
                                    articleContent?.sections?.forEach { section ->
                                        append(section.title + "\n")
                                        append(section.content + "\n\n")
                                    }
                                }
                                Log.d("TTS", "Text to speak length: ${textToSpeak.length}")
                                Log.d("TTS", "First 100 chars: ${textToSpeak.take(100)}")
                                ttsViewModel.speak(textToSpeak)
                            }
                        }
                    ) {
                        Icon(
                            if (isSpeaking) Icons.Default.Stop else Icons.Default.VolumeUp,
                            contentDescription = if (isSpeaking) 
                                stringResource(R.string.accessibility_tts_stop)
                            else 
                                stringResource(R.string.accessibility_tts_play),
                            tint = if (isSpeaking) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(
                        onClick = {
                            viewModel.toggleBookmark(
                                Bookmark(
                                    title = title,
                                    url = "${ApiConfig.WIKIPEDIA_BASE_URL}wiki/$title"
                                )
                            )
                        }
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = if (isBookmarked) 
                                stringResource(R.string.accessibility_bookmark_remove)
                            else 
                                stringResource(R.string.accessibility_bookmark_add),
                            tint = if (isBookmarked) CreamOffWhite else MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TealCyan,
                    titleContentColor = CreamOffWhite
                )
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (error != null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundBeige)
                    .padding(padding)
                    .padding(horizontal = 16.dp)
            ) {
                item {
                    Text(
                        text = articleContent?.title ?: title,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = DarkBrown
                        ),
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }

                items(articleImages) { imageUrl ->
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .heightIn(min = 200.dp),
                        contentScale = ContentScale.Crop
                    )
                }

                articleDescription?.let {
                    item {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                lineHeight = 28.sp,
                                fontSize = 18.sp,
                                color = DarkBrown,
                                textAlign = TextAlign.Start
                            ),
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    }
                    
                    item {
                        Divider(
                            color = DarkBrown.copy(alpha = 0.4f),
                            thickness = 2.dp,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }
                }

                if (sectionsLoaded) {
                    articleContent?.sections?.let { sections ->
                        items(sections) { section ->
                            if (section.content.isNotEmpty()) {
                                Column(modifier = Modifier.padding(vertical = 16.dp)) {
                                    Text(
                                        text = section.title,
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = TealCyan,
                                            fontSize = 24.sp
                                        ),
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )

                                    val annotatedText = buildAnnotatedString {
                                        val paragraphs = section.content
                                            .replace("• ", "\n• ")
                                            .replace(Regex("\\n{2,}"), "\n")
                                            .lines()
                                            .map { it.trimStart() }
                                            .filter { it.isNotBlank() }
                                            .map { it.trim() }

                                        paragraphs.forEachIndexed { index, paragraph ->
                                            if (index > 0) append("\n")
                                            val isBullet = paragraph.startsWith("•")
                                            val startsWithDate = paragraph.take(4).all { it.isDigit() }
                                            val useIndent = !(isBullet || startsWithDate)

                                            val paragraphStyle = ParagraphStyle(
                                                lineHeight = if (isBullet) 24.sp else 30.sp,
                                                textIndent = if (useIndent) TextIndent(firstLine = 20.sp) else TextIndent.None
                                            )

                                            withStyle(paragraphStyle) {
                                                val lines = paragraph
                                                    .split("\n")
                                                    .map { it.replace(Regex("^[ \t]+"), "") }

                                                lines.forEachIndexed { i, line ->
                                                    if (i > 0) append("\n")
                                                    appendFormattedLine(line, TealCyan)
                                                }
                                            }
                                        }
                                    }

                                    ClickableText(
                                        text = annotatedText,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontSize = 18.sp,
                                            textAlign = TextAlign.Start
                                        ),
                                        modifier = Modifier.padding(bottom = 16.dp),
                                        onClick = { offset ->
                                            annotatedText.getStringAnnotations("ARTICLE", offset, offset)
                                                .firstOrNull()?.let { annotation ->
                                                    navController.navigate(Screen.Article.createRoute(annotation.item))
                                                }
                                        }
                                    )

                                    Divider(
                                        color = DarkBrown.copy(alpha = 0.4f),
                                        thickness = 1.dp,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


fun AnnotatedString.Builder.appendFormattedLine(
    text: String,
    primaryColor: Color
) {
    // 🔬 Chemical equations with subscript and arrows
    val chemRegex = Regex("\\[CHEM:(.*?)\\]")
    val chemMatch = chemRegex.find(text)
    if (chemMatch != null) {
        val before = text.substring(0, chemMatch.range.first)
        val equation = chemMatch.groupValues[1]
        appendFormattedLine(before, primaryColor)

        val parts = equation.split(Regex("\\s+"))
        parts.forEachIndexed { index, part ->
            if (index > 0) append(" ")

            val subscriptRegex = Regex("([A-Za-z]+)(\\d+)")
            val subMatch = subscriptRegex.find(part)
            when {
                subMatch != null -> {
                    val (base, sub) = subMatch.destructured
                    append(base)
                    withStyle(SpanStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        baselineShift = BaselineShift.Subscript
                    )) {
                        append(sub)
                    }
                }
                part in listOf("→", "←", "↔") -> {
                    withStyle(SpanStyle(
                        color = primaryColor,
                        fontWeight = FontWeight.Bold
                    )) {
                        append(part)
                    }
                }
                else -> {
                    withStyle(SpanStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 16.sp
                    )) {
                        append(part)
                    }
                }
            }
        }

        val after = text.substring(chemMatch.range.last + 1)
        appendFormattedLine(after, primaryColor)
        return
    }


    // ➗ Handle math blocks
    val mathRegex = Regex("\\[MATH:(.*?)\\]")
    val mathMatch = mathRegex.find(text)
    if (mathMatch != null) {
        val before = text.substring(0, mathMatch.range.first)
        val mathContent = mathMatch.groupValues[1]
        appendFormattedLine(before, primaryColor)
        withStyle(SpanStyle(fontFamily = FontFamily.Monospace)) {
            append(mathContent)
        }
        val after = text.substring(mathMatch.range.last + 1)
        appendFormattedLine(after, primaryColor)
        return
    }

    // 🔗 Handle internal links
    val linkRegex = Regex("\\[\\[(.*?)\\|(.*?)]]")
    val matches = linkRegex.findAll(text)
    if (matches.any()) {
        var cursor = 0
        matches.forEach { match ->
            val before = text.substring(cursor, match.range.first)
            appendFormattedLine(before, primaryColor)

            val (displayText, target) = match.destructured
            pushStringAnnotation(tag = "ARTICLE", annotation = target)
            withStyle(SpanStyle(color = primaryColor, textDecoration = TextDecoration.Underline)) {
                appendFormattedLine(displayText, primaryColor)
            }
            pop()
            cursor = match.range.last + 1
        }
        if (cursor < text.length) {
            appendFormattedLine(text.substring(cursor), primaryColor)
        }
        return
    }

    // 🧠 Bold & Italic
    var remaining = text
    while (remaining.isNotEmpty()) {
        val boldStart = remaining.indexOf("**")
        val italicStart = remaining.indexOf("*")

        when {
            boldStart != -1 && (italicStart == -1 || boldStart < italicStart) -> {
                append(remaining.substring(0, boldStart))
                val end = remaining.indexOf("**", boldStart + 2)
                if (end != -1) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(remaining.substring(boldStart + 2, end))
                    }
                    remaining = remaining.substring(end + 2)
                } else {
                    append(remaining)
                    break
                }
            }

            italicStart != -1 -> {
                append(remaining.substring(0, italicStart))
                val end = remaining.indexOf("*", italicStart + 1)
                if (end != -1) {
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = primaryColor)) {
                        append(remaining.substring(italicStart + 1, end))
                    }
                    remaining = remaining.substring(end + 1)
                } else {
                    append(remaining)
                    break
                }
            }

            else -> {
                append(remaining)
                break
            }
        }
    }
}

private fun fetchSectionContents(sections: List<ArticleSection>, title: String, onComplete: () -> Unit) {
    Log.d("SECTION_FETCH", "Starting to fetch ${sections.size} sections")
    var completedSections = 0
    val totalSections = sections.size

    sections.forEach { section ->
        Log.d("SECTION_FETCH", "Fetching content for section: ${section.title} (index: ${section.sectionIndex})")

        RetrofitInstance.api.getArticleContent(
            title = title,
            section = section.sectionIndex
        ).enqueue(object : Callback<ArticleResponse> {
            override fun onResponse(call: Call<ArticleResponse>, response: Response<ArticleResponse>) {
                if (response.isSuccessful) {
                    val articleResponse = response.body()
                    val rawHtml = articleResponse?.parse?.text ?: ""
                    Log.d("SECTION_RAW", "Raw HTML length for '${section.title}': ${rawHtml.length}")

                    try {
                        val processed = processHtmlContent(rawHtml)
                        Log.d("SECTION_PROCESSED", "Processed content length for '${section.title}': ${processed.length}")

                        val cleaned = processed
                            .removePrefix(section.title)
                            .removePrefix("Edit")
                            .trim()

                        val formatted = cleaned
                            .replace("• ", "\n• ")
                            .replace(Regex("\\n{3,}"), "\n\n")
                            .trim()

                        section.content = formatted
                        Log.d("SECTION_FINAL", "Final content for '${section.title}': ${formatted.take(100)}...")
                    } catch (e: Exception) {
                        Log.e("SECTION_PROCESSING", "Error processing section ${section.title}: ${e.message}", e)
                        section.content = "Error loading content for this section."
                    }
                } else {
                    Log.e("SECTION_ERROR", "Failed to fetch section ${section.title}: ${response.code()}")
                    section.content = "Error loading content for this section."
                }

                completedSections++
                if (completedSections == totalSections) {
                    onComplete()
                }
            }

            override fun onFailure(call: Call<ArticleResponse>, t: Throwable) {
                Log.e("SECTION_ERROR", "Failed to fetch section ${section.title}: ${t.message}", t)
                section.content = "Error loading content for this section."
                
                completedSections++
                if (completedSections == totalSections) {
                    onComplete()
                }
            }
        })

        // Recursively fetch subsection content
        if (section.subsections.isNotEmpty()) {
            Log.d("SUBSECTION_FETCH", "Fetching ${section.subsections.size} subsections for ${section.title}")
            fetchSectionContents(section.subsections, title, onComplete)
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
        .replace(Regex("\\[edit\\]"), "")
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
        .replace(Regex("<a[^>]*href=\"/wiki/([^\"]+)\"[^>]*>(.*?)</a>")) {
            val linkTarget = it.groupValues[1]
            val linkText = it.groupValues[2]
            "[[$linkText|$linkTarget]]"
        }
        .replace(Regex("<a[^>]*>|</a>"), "")
        .replace(Regex("<p[^>]*>|</p>"), "\n") // Convert paragraphs to newlines
        .replace(Regex("<br[^>]*>"), "\n") // Convert line breaks
        .replace(Regex("<ul>|</ul>"), "\n") // Convert unordered lists
        .replace(Regex("<ol>|</ol>"), "\n") // Convert ordered lists
        .replace(Regex("<li>"), "• ") // Convert list items to bullet points
        .replace(Regex("</li>"), "\n") // End list items with newline
        .replace(Regex("\\[\\d+\\]"), "") // Remove citation numbers
        .replace(Regex("\\s+"), " ") // Normalize whitespace
        .replace("&gt;", ">")
        .replace("&lt;", "<")
        .replace("&amp;", "&")
//        .replace(Regex("\\{ce\\s*\\{(.*?)}}")) {
//            "[CHEM:${it.groupValues[1]}]"
//        }
//        .trim()

    // Handle chemical equations with better formatting
    processed = processed.replace(Regex("<chem>([^<]+)</chem>")) { match ->
        val equation = match.groupValues[1]
            .replace(Regex("&gt;"), "→") // Convert HTML arrow to Unicode arrow
            .replace(Regex("&lt;"), "←")
            .replace(Regex("&amp;"), "&")
            .replace(Regex("\\s*->\\s*"), " → ") // Format arrows with spaces
            .replace(Regex("\\s*<-\\s*"), " ← ")
            .replace(Regex("\\s*<->\\s*"), " ↔ ")
        "\n[CHEM:$equation]\n"
    }


    // Handle mathematical equations
    processed = processed.replace(Regex("<math>([^<]+)</math>")) { match ->
        val equation = match.groupValues[1]
        "\n[MATH:$equation]\n"
    }

    // Convert inline images
    processed = processed.replace(Regex("<noscript>\\s*<img[^>]+src=\"([^\"]+)\"[^>]*>\\s*</noscript>")) { match ->
        val rawSrc = match.groupValues[1]
        val fullUrl = if (rawSrc.startsWith("//")) "https:$rawSrc" else rawSrc
        "\n[[[IMG:$fullUrl]]]\n"
    }

    // Clean up any remaining HTML tags
    processed = processed.replace(Regex("<[^>]+>"), "")

    // Remove any line-level leading spaces
    processed = processed
        .lines()
        .joinToString("\n") { it.trimStart() }

    // Handle scientific names and terms
    processed = processed
        .replace(Regex("\\*([A-Z][a-z]+ [a-z]+)\\*"), "*$1*") // Format scientific names
        .replace(Regex("\\*([A-Z][a-z]+)\\*"), "*$1*") // Format genus names

    // Final cleanup
    processed = processed
        .replace(Regex("\\n{3,}"), "\n") // Normalize multiple newlines
        .replace(Regex("^\\s+$", RegexOption.MULTILINE), "") // Remove empty lines
        .replace(Regex("\\[edit\\]"), "") // Remove edit links
        .trim()

    return processed
}

private fun decodeHtml(htmlText: String): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(htmlText, Html.FROM_HTML_MODE_LEGACY).toString()
    } else {
        Html.fromHtml(htmlText).toString()
    }
}