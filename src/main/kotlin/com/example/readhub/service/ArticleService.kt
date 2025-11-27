package com.example.readhub.service

import com.example.readhub.config.ApiConfig
import com.example.readhub.model.Article
import com.example.readhub.repository.ArticleRepository
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import java.time.LocalDateTime

@Service
class ArticleService(
    private val articleRepository: ArticleRepository,
    private val webClient: WebClient,
    private val apiConfig: ApiConfig
) {

    // Temporary in-memory storage (replace with database later)
    private val articlesCache = mutableListOf<Article>()
    private val favoriteArticles = mutableSetOf<String>()

    init {
        // Initialize with demo articles
        articlesCache.addAll(getDemoArticles())
    }

    // Add these missing methods that WebController needs:
    fun getAllArticles(): List<Article> {
        return articlesCache.sortedByDescending { it.publishedAt }
    }

    fun getFavoriteArticles(): List<Article> {
        return articlesCache.filter { favoriteArticles.contains(it.id ?: "") }
    }

    fun toggleFavorite(id: String) {
        if (favoriteArticles.contains(id)) {
            favoriteArticles.remove(id)
            println("❌ Removed article $id from favorites")
        } else {
            favoriteArticles.add(id)
            println("⭐ Added article $id to favorites")
        }
    }

    fun searchArticles(query: String): List<Article> {
        val lowercaseQuery = query.lowercase()
        return articlesCache.filter { article ->
            article.title.lowercase().contains(lowercaseQuery) ||
                    article.content.lowercase().contains(lowercaseQuery) ||
                    article.description.lowercase().contains(lowercaseQuery) ||
                    article.author.lowercase().contains(lowercaseQuery) ||
                    article.category.lowercase().contains(lowercaseQuery) ||
                    article.tags.any { it.lowercase().contains(lowercaseQuery) }
        }
    }

    suspend fun fetchArticlesFromNewsAPI(): List<Article> {
        // Check if API key is set
        if (apiConfig.news.key.isBlank() || apiConfig.news.key == "demo_key") {
            println("⚠️  NewsAPI key not set. Using demo data instead.")
            return getDemoArticles()
        }

        try {
            println("🔑 Using API Key: ${apiConfig.news.key.take(5)}...") // Log first 5 chars only

            val newsApiResponse = webClient.get()
                .uri("https://newsapi.org/v2/top-headlines?country=us&apiKey=${apiConfig.news.key}")
                .retrieve()
                .bodyToMono<NewsApiResponse>()
                .awaitSingle()

            println("✅ Successfully fetched ${newsApiResponse.articles.size} articles from NewsAPI")

            // In your fetchArticlesFromNewsAPI() method:
            val newArticles = newsApiResponse.articles.map { newsArticle ->
                Article(
                    title = newsArticle.title ?: "No Title",
                    content = newsArticle.content ?: "No content available",
                    author = newsArticle.author ?: "Unknown Author",
                    description = newsArticle.description ?: "No description",
                    source = newsArticle.source.name,
                    url = newsArticle.url, // ← This is the REAL article URL
                    urlToImage = newsArticle.urlToImage,
                    publishedAt = parsePublishedAt(newsArticle.publishedAt),
                    category = "general",
                    tags = emptyList()
                )
            }

            // Add to cache (you might want to replace or merge)
            articlesCache.addAll(newArticles)
            return newArticles

        } catch (e: Exception) {
            println("❌ Error fetching from NewsAPI: ${e.message}")
            return emptyList()
        }
    }

    private fun parsePublishedAt(publishedAt: String?): LocalDateTime {
        return try {
            publishedAt?.let {
                LocalDateTime.parse(it.replace("Z", ""))
            } ?: LocalDateTime.now()
        } catch (e: Exception) {
            LocalDateTime.now()
        }
    }

    suspend fun searchRealNews(query: String): List<Article> {
        // Check if API key is set
        if (apiConfig.news.key.isBlank() || apiConfig.news.key == "demo_key") {
            println("⚠️  NewsAPI key not set. Using demo search only.")
            return searchArticles(query) // Fallback to local demo search
        }

        try {
            println("🔍 Searching NewsAPI for: $query")

            val newsApiResponse = webClient.get()
                .uri("https://newsapi.org/v2/everything?q=${query}&sortBy=publishedAt&language=en&apiKey=${apiConfig.news.key}")
                .retrieve()
                .bodyToMono<NewsApiResponse>()
                .awaitSingle()

            println("✅ Found ${newsApiResponse.articles.size} real articles for: $query")

            return newsApiResponse.articles.map { newsArticle ->
                Article(
                    title = newsArticle.title ?: "No Title",
                    content = newsArticle.content ?: "No content available",
                    author = newsArticle.author ?: "Unknown Author",
                    description = newsArticle.description ?: "No description",
                    source = newsArticle.source.name,
                    url = newsArticle.url ?: "https://newsapi.org/",
                    urlToImage = newsArticle.urlToImage,
                    publishedAt = parsePublishedAt(newsArticle.publishedAt),
                    category = "search",
                    tags = listOf(query)
                )
            }
        } catch (e: Exception) {
            println("❌ Error searching real news: ${e.message}")
            return searchArticles(query) // Fallback to local search
        }
    }

    private fun getDemoArticles(): List<Article> {
        println("📄 Using demo articles")
        return listOf(
            Article(
                title = "Spring Boot 3.0 Released with New Features",
                content = "Spring Boot 3.0 brings exciting new features and improvements to the popular Java framework. The new release includes enhanced performance, better GraalVM support, and improved developer experience. Developers can now enjoy faster startup times and reduced memory footprint...",
                author = "Spring Team",
                description = "Latest release of Spring Boot framework with enhanced performance",
                source = "Spring Blog",
                url = "https://spring.io/blog/2023/11/24/spring-boot-3-0-goes-ga", // REAL URL
                urlToImage = "https://picsum.photos/400/200?random=10",
                publishedAt = LocalDateTime.now().minusDays(1),
                category = "Technology",
                tags = listOf("spring", "java", "framework", "boot")
            ),
            Article(
                title = "Kotlin 1.9 Introduces Powerful New Features",
                content = "The latest Kotlin version introduces several exciting features that enhance developer productivity. New language features include improved coroutines, better type inference, and enhanced Java interoperability. These updates make Kotlin even more powerful for modern application development...",
                author = "Kotlin Team",
                description = "What's new in Kotlin 1.9 and how it improves your code",
                source = "Kotlin Blog",
                url = "https://kotlinlang.org/docs/whatsnew19.html", // REAL URL
                urlToImage = "https://picsum.photos/400/200?random=11",
                publishedAt = LocalDateTime.now().minusHours(6),
                category = "Programming",
                tags = listOf("kotlin", "programming", "update", "java")
            ),
            Article(
                title = "MongoDB 6.0 Enhances Query Performance",
                content = "MongoDB's latest release focuses on improving query performance and developer experience. Version 6.0 introduces new aggregation pipeline stages, better indexing strategies, and enhanced security features. These improvements help developers build faster and more scalable applications...",
                author = "MongoDB Team",
                description = "Performance improvements in MongoDB 6.0",
                source = "MongoDB Blog",
                url = "https://www.mongodb.com/blog/post/mongodb-6-0-release", // REAL URL
                urlToImage = "https://picsum.photos/400/200?random=12",
                publishedAt = LocalDateTime.now().minusHours(2),
                category = "Database",
                tags = listOf("mongodb", "database", "performance", "nosql")
            )
        )
    }

// Data classes for API responses (add at the bottom of the same file)
data class NewsApiResponse(
    val status: String,
    val totalResults: Int,
    val articles: List<NewsArticle>
)

data class NewsArticle(
    val source: Source,
    val author: String?,
    val title: String?,
    val description: String?,
    val url: String,
    val urlToImage: String?,
    val publishedAt: String?,
    val content: String?
)

data class Source(val name: String)}