package com.example.readhub.config

import com.example.readhub.model.Article
import com.example.readhub.model.Magazine
import com.example.readhub.repository.ArticleRepository
import com.example.readhub.repository.MagazineRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class DataLoader(
    private val articleRepository: ArticleRepository,
    private val magazineRepository: MagazineRepository
) : CommandLineRunner {

    override fun run(args: Array<String>) {
        println("🔄 DataLoader started...")

        // Clear existing data
        articleRepository.deleteAll()
        magazineRepository.deleteAll()
        println("✅ Data cleared")

        // Sample articles
        val articles = listOf(
            Article(
                title = "Getting Started with Spring Boot",
                content = "Spring Boot makes it easy to create stand-alone Spring applications...",
                author = "Jane Developer",
                description = "Learn Spring Boot basics",
                source = "Spring Blog",
                url = "https://example.com/spring-boot",
                urlToImage = "https://picsum.photos/400/200?random=1",
                publishedAt = LocalDateTime.now().minusDays(1),
                category = "Technology",
                tags = listOf("spring", "java", "tutorial")
            ),
            Article(
                title = "The Future of AI in Development",
                content = "Artificial intelligence is transforming how we write code...",
                author = "AI Expert",
                description = "Exploring AI tools for developers",
                source = "Tech Insights",
                url = "https://example.com/ai-future",
                urlToImage = "https://picsum.photos/400/200?random=2",
                publishedAt = LocalDateTime.now().minusHours(5),
                category = "Artificial Intelligence",
                tags = listOf("ai", "development", "future")
            )
        )

        val savedArticles = articleRepository.saveAll(articles)
        println("✅ Loaded ${savedArticles.size} articles")

        // Sample magazines
        val magazines = listOf(
            Magazine(
                title = "Developer Weekly",
                publisher = "Code Publications",
                description = "Weekly magazine for software developers",
                coverImage = "https://picsum.photos/300/400?random=3",
                issueNumber = 15,
                publicationDate = LocalDateTime.now().minusDays(10),
                articles = savedArticles.map { it.id!! },
                category = "Programming"
            )
        )

        magazineRepository.saveAll(magazines)
        println("✅ Loaded ${magazines.size} magazines")

        println("🎉 DataLoader completed successfully!")
    }
}