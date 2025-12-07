package com.example.readhub.repository

import com.example.readhub.model.Article
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ArticleRepository : MongoRepository<Article, String> {
    fun findByTitleContainingIgnoreCase(title: String): List<Article>
    fun findByAuthorContainingIgnoreCase(author: String): List<Article>
    fun findByCategory(category: String): List<Article>
    fun findByIsFavoriteTrue(): List<Article>

    @Query("{ \$or: [ " +
            "{ 'title': { \$regex: ?0, \$options: 'i' } }, " +
            "{ 'author': { \$regex: ?0, \$options: 'i' } }, " +
            "{ 'content': { \$regex: ?0, \$options: 'i' } } " +
            "] }")
    fun searchArticles(query: String): List<Article>
}