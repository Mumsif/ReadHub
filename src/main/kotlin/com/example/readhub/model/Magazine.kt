package com.example.readhub.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "magazines")
data class Magazine(
    @Id
    val id: String? = null,
    val title: String,
    val publisher: String,
    val description: String,
    val coverImage: String?,
    val issueNumber: Int,
    val publicationDate: LocalDateTime,
    val articles: List<String> = emptyList(),
    val isFavorite: Boolean = false,
    val category: String
)