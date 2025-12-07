package com.example.readhub.service

import com.example.readhub.model.Magazine
import com.example.readhub.repository.MagazineRepository
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class MagazineService(
    private val magazineRepository: MagazineRepository
) {
    private val magazinesCache = mutableListOf<Magazine>()
    private val favoriteMagazines = mutableSetOf<String>()

    init {
        magazinesCache.addAll(getDemoMagazines())
        // Load from DB on startup
        runBlocking {
            if (magazineRepository.count() == 0L) {
                magazineRepository.saveAll(getDemoMagazines())
            }
        }
    }

    fun getAllMagazines(): List<Magazine> {
        return magazineRepository.findAll()
    }

    fun getFavoriteMagazines(): List<Magazine> {
        return magazineRepository.findByIsFavoriteTrue()
    }

    fun toggleFavorite(id: String) {
        val magazine = magazineRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Magazine not found: $id") }

        magazine.isFavorite = !magazine.isFavorite
        magazineRepository.save(magazine)
        println("⭐ Toggled favorite for magazine $id: ${magazine.isFavorite}")
    }

    fun searchMagazines(query: String): List<Magazine> {
        if (query.isBlank()) return emptyList()
        return magazineRepository.searchMagazines(query)
    }

    private fun getDemoMagazines(): List<Magazine> {
        return listOf(
            Magazine(
                title = "Developer Weekly",
                publisher = "Code Publications",
                description = "Weekly magazine for software developers",
                coverImage = "https://picsum.photos/300/400?random=1",
                issueNumber = 15,
                publicationDate = LocalDateTime.now().minusDays(7),
                category = "Programming"
            ),
            Magazine(
                title = "Tech Insights",
                publisher = "Tech Media Group",
                description = "Monthly technology trends and analysis",
                coverImage = "https://picsum.photos/300/400?random=2",
                issueNumber = 42,
                publicationDate = LocalDateTime.now().minusDays(14),
                category = "Technology"
            ),
            Magazine(
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