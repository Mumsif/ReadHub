package com.example.readhub.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "articles")
data class Article(
    @Id
    val id: String? = null,
    val title: String,
    val content: String,
    val author: String,
    val description: String,
    val source: String,
    val url: String,
    val urlToImage: String?,
    val publishedAt: LocalDateTime,
    val category: String,
    val isFavorite: Boolean = false,
    val tags: List<String> = emptyList()
){
    // Ensure content is not null or empty
    val displayContent: String
        get() = if (content.isBlank() || content == "No content available") {
            description // Fallback to description
        } else {
            content
        }
}