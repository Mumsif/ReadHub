package com.example.readhub.service

import com.example.readhub.config.ApiConfig
import com.example.readhub.model.Article
import com.example.readhub.repository.ArticleRepository
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import java.time.Duration
import java.time.LocalDateTime

@Service
class ArticleService(
    private val articleRepository: ArticleRepository,
    private val webClient: WebClient,
    private val apiConfig: ApiConfig
) {
    private val articlesCache = mutableListOf<Article>()
    private val favoriteArticles = mutableSetOf<String>()

    init {
        articlesCache.addAll(getDemoArticles())
    }

    fun getAllArticles(): List<Article> = articlesCache

    fun getFavoriteArticles(): List<Article> = articlesCache.filter { it.id in favoriteArticles }

    fun toggleFavorite(id: String) {
        if (favoriteArticles.contains(id)) {
            favoriteArticles.remove(id)
            println("❌ Removed article $id from favorites")
        } else {
            favoriteArticles.add(id)
            println("⭐ Added article $id to favorites")
        }
    }

    // ✅ ADD THIS METHOD
    fun isApiKeyConfigured(): Boolean {
        return apiConfig.news.key.isNotBlank() && apiConfig.news.key != "demo_key"
    }

    fun searchArticles(query: String): List<Article> {
        return runBlocking { searchRealNews(query) }
    }

    suspend fun fetchArticlesFromNewsAPI(): List<Article> {
        if (!isApiKeyConfigured()) {
            println("⚠️ NewsAPI key not set. Using demo data instead.")
            return getDemoArticles()
        }

        return try {
            println("🔑 Using API Key: ${apiConfig.news.key.take(5)}...")

            val newsApiResponse = webClient.get()
                .uri("https://newsapi.org/v2/top-headlines?country=us&apiKey=${apiConfig.news.key}")
                .retrieve()
                .onStatus({ status -> status.is4xxClientError || status.is5xxServerError }) {
                    Mono.error(RuntimeException("NewsAPI error: ${it.statusCode()}"))
                }
                .bodyToMono<NewsApiResponse>()
                .timeout(Duration.ofSeconds(10))
                .awaitSingle()

            println("✅ Successfully fetched ${newsApiResponse.articles.size} articles from NewsAPI")

            val newArticles = newsApiResponse.articles.map { newsArticle ->
                Article(
                    title = newsArticle.title ?: "No Title",
                    content = newsArticle.content ?: "No content available",
                    author = newsArticle.author ?: "Unknown Author",
                    description = newsArticle.description ?: "No description",
                    source = newsArticle.source.name,
                    url = newsArticle.url,
                    urlToImage = newsArticle.urlToImage,
                    publishedAt = parsePublishedAt(newsArticle.publishedAt),
                    category = "general",
                    tags = emptyList(),
                    isFavorite = false
                )
            }

            articlesCache.addAll(newArticles)
            newArticles

        } catch (e: Exception) {
            println("❌ Error fetching from NewsAPI: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun searchRealNews(query: String): List<Article> {
        if (!isApiKeyConfigured()) {
            println("⚠️ NewsAPI key not set. Using demo search for: $query")
            return articlesCache.filter { article ->
                article.title.contains(query, ignoreCase = true) ||
                        article.content.contains(query, ignoreCase = true)
            }
        }

        return try {
            println("🔍 Searching NewsAPI for: $query with key: ${apiConfig.news.key.take(5)}...")

            val newsApiResponse = webClient.get()
                .uri("https://newsapi.org/v2/everything") {
                    it.queryParam("q", query)
                        .queryParam("sortBy", "publishedAt")
                        .queryParam("language", "en")
                        .queryParam("apiKey", apiConfig.news.key)
                        .build()
                }
                .retrieve()
                .onStatus({ status -> status.is4xxClientError || status.is5xxServerError }) {
                    Mono.error(RuntimeException("NewsAPI error: ${it.statusCode()}"))
                }
                .bodyToMono<NewsApiResponse>()
                .timeout(Duration.ofSeconds(10))
                .awaitSingle()

            if (newsApiResponse.status != "ok") {
                println("❌ NewsAPI returned status: ${newsApiResponse.status}")
                return emptyList()
            }

            println("✅ Found ${newsApiResponse.articles.size} real articles for: $query")

            newsApiResponse.articles.map { newsArticle ->
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
                    tags = listOf(query),
                    isFavorite = false
                )
            }

        } catch (e: Exception) {
            println("❌ Error searching real news: ${e.message}")
            e.printStackTrace()
            articlesCache.filter {
                it.title.contains(query, ignoreCase = true)
            }
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

    private fun getDemoArticles(): List<Article> {
        return listOf(
            Article(
                title = "Spring Boot 3.0 Released with New Features",
                content = "Spring Boot 3.0 brings exciting new features and improvements...",
                author = "Spring Team",
                description = "Latest release of Spring Boot framework",
                source = "Spring Blog",
                url = "https://spring.io/blog/2023/11/24/spring-boot-3-0-goes-ga",
                urlToImage = "https://picsum.photos/400/200?random=10",
                publishedAt = LocalDateTime.now().minusDays(1),
                category = "Technology",
                tags = listOf("spring", "java", "tutorial"),
                isFavorite = false
            ),
            Article(
                title = "Kotlin 1.9 Introduces Powerful New Features",
                content = "The latest Kotlin version introduces several exciting features...",
                author = "Kotlin Team",
                description = "What's new in Kotlin 1.9",
                source = "Kotlin Blog",
                url = "https://kotlinlang.org/docs/whatsnew19.html",
                urlToImage = "https://picsum.photos/400/200?random=11",
                publishedAt = LocalDateTime.now().minusHours(6),
                category = "Programming",
                tags = listOf("kotlin", "programming", "update"),
                isFavorite = false
            ),
            Article(
                title = "MongoDB 6.0 Enhances Query Performance",
                content = "MongoDB's latest release focuses on improving query performance...",
                author = "MongoDB Team",
                description = "Performance improvements in MongoDB 6.0",
                source = "MongoDB Blog",
                url = "https://www.mongodb.com/blog/post/mongodb-6-0-release",
                urlToImage = "https://picsum.photos/400/200?random=12",
                publishedAt = LocalDateTime.now().minusHours(2),
                category = "Database",
                tags = listOf("mongodb", "database", "performance"),
                isFavorite = false
            )
        )
    }

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

    data class Source(val name: String)
}