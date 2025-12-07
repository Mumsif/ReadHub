package com.example.readhub.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "articles")
data class Article(
    @Id
    var id: String? = null,
    val title: String,
    var content: String,
    val author: String,
    val description: String,
    val source: String,
    val url: String,
    val urlToImage: String? = null,
    val publishedAt: LocalDateTime,
    val category: String,
    val tags: List<String> = emptyList(),
    var isFavorite: Boolean = false
)