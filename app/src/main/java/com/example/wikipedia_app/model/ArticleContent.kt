package com.example.wikipedia_app.model

data class ArticleContent(
    val title: String,
    val sections: List<ArticleSection>
)

data class ArticleSection(
    val title: String,
    val level: Int,
    var content: String,
    val sectionIndex: Int,
    val subsections: List<ArticleSection> = emptyList()
) 