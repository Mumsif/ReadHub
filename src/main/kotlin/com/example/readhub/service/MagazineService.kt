package com.example.readhub.service

import com.example.readhub.model.Magazine
import com.example.readhub.repository.MagazineRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class MagazineService {

    private val magazinesCache = mutableListOf<Magazine>()
    private val favoriteMagazines = mutableSetOf<String>()

    init {
        magazinesCache.addAll(getDemoMagazines())
    }

    fun getAllMagazines(): List<Magazine> {
        return magazinesCache
    }

    fun getFavoriteMagazines(): List<Magazine> {
        return magazinesCache.filter { favoriteMagazines.contains(it.id) }
    }

    fun toggleFavorite(id: String) {
        if (favoriteMagazines.contains(id)) {
            favoriteMagazines.remove(id)
            println("❌ Removed magazine $id from favorites")
        } else {
            favoriteMagazines.add(id)
            println("⭐ Added magazine $id to favorites")
        }
    }

    fun searchMagazines(query: String): List<Magazine> {
        val lowercaseQuery = query.lowercase()
        return magazinesCache.filter { magazine ->
            magazine.title.lowercase().contains(lowercaseQuery) ||
                    magazine.publisher.lowercase().contains(lowercaseQuery) ||
                    magazine.description.lowercase().contains(lowercaseQuery) ||
                    magazine.category.lowercase().contains(lowercaseQuery)
        }
    }

    private fun getDemoMagazines(): List<Magazine> {
        return listOf(
            Magazine(
                id = "1",
                title = "Developer Weekly",
                publisher = "Code Publications",
                description = "Weekly magazine for software developers",
                coverImage = "https://picsum.photos/300/400?random=1",
                issueNumber = 15,
                publicationDate = LocalDateTime.now().minusDays(7),
                category = "Programming"
            ),
            Magazine(
                id = "2",
                title = "Tech Insights",
                publisher = "Tech Media Group",
                description = "Monthly technology trends and analysis",
                coverImage = "https://picsum.photos/300/400?random=2",
                issueNumber = 42,
                publicationDate = LocalDateTime.now().minusDays(14),
                category = "Technology"
            ),
            Magazine(
                id = "3",
                title = "AI Today",
                publisher = "Future Publications",
                description = "Cutting-edge artificial intelligence research",
                coverImage = "https://picsum.photos/300/400?random=3",
                issueNumber = 8,
                publicationDate = LocalDateTime.now().minusDays(21),
                category = "Artificial Intelligence"
            )
        )
    }
}