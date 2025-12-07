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

        try {
            if (articleRepository.count() == 0L) {
                println("📄 Database is empty, loading demo articles...")
                val demoArticles = getDemoArticles()
                articleRepository.saveAll(demoArticles)
                println("✅ Loaded ${demoArticles.size} demo articles")
            } else {
                println("✅ Database already has articles, skipping")
            }

            if (magazineRepository.count() == 0L) {
                println("📄 Loading demo magazines...")
                val demoMagazines = getDemoMagazines()
                magazineRepository.saveAll(demoMagazines)
                println("✅ Loaded ${demoMagazines.size} demo magazines")
            } else {
                println("✅ Database already has magazines, skipping")
            }

        } catch (e: Exception) {
            println("❌ Error in DataLoader: ${e.message}")
            e.printStackTrace()
        }

        println("🎉 DataLoader completed!")
    }

    private fun getDemoArticles(): List<Article> {
        return listOf(
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
                tags = listOf("spring", "java", "tutorial"),
                isFavorite = false
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
                tags = listOf("ai", "development", "future"),
                isFavorite = false
            )
        )
    }

    private fun getDemoMagazines(): List<Magazine> {
        return listOf(
            Magazine(
                title = "Developer Weekly",
                publisher = "Code Publications",
                description = "Weekly magazine for software developers",
                coverImage = "https://picsum.photos/300/400?random=3",
                issueNumber = 15,
                publicationDate = LocalDateTime.now().minusDays(10),
                category = "Programming"
            )
        )
    }
}