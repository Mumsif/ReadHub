package com.example.readhub.repository

import com.example.readhub.model.Magazine
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface MagazineRepository : MongoRepository<Magazine, String> {
    fun findByTitleContainingIgnoreCase(title: String): List<Magazine>
    fun findByPublisherContainingIgnoreCase(publisher: String): List<Magazine>
    fun findByCategory(category: String): List<Magazine>
    fun findByIsFavoriteTrue(): List<Magazine>

    @Query("{ \$or: [ " +
            "{ 'title': { \$regex: ?0, \$options: 'i' } }, " +
            "{ 'description': { \$regex: ?0, \$options: 'i' } }, " +
            "{ 'publisher': { \$regex: ?0, \$options: 'i' } } " +
            "] }")
    fun searchMagazines(query: String): List<Magazine>
}