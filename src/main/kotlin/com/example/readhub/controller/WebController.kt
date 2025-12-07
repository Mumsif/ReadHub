package com.example.readhub.controller

import com.example.readhub.model.Article
import com.example.readhub.service.ArticleService
import com.example.readhub.service.MagazineService
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/")
class WebController(
    private val articleService: ArticleService,
    private val magazineService: MagazineService
) {

    @GetMapping
    fun home(model: Model): String {
        return try {
            println("🏠 Loading home page...")
            val articles = articleService.getAllArticles().take(6)
            val magazines = magazineService.getAllMagazines().take(3)

            model.addAttribute("articles", articles)
            model.addAttribute("magazines", magazines)
            "index"
        } catch (e: Exception) {
            println("❌ Error in home: ${e.message}")
            model.addAttribute("articles", emptyList<Article>())
            model.addAttribute("magazines", emptyList<Article>())
            "index"
        }
    }

    @GetMapping("/articles")
    fun articles(model: Model): String {
        println("🔄 /articles endpoint called")
        val articles = articleService.getAllArticles()
        model.addAttribute("articles", articles)
        return "articles"
    }

    @GetMapping("/magazines")
    fun magazines(model: Model): String {
        model.addAttribute("magazines", magazineService.getAllMagazines())
        return "magazines"
    }

    @GetMapping("/search")
    fun search(@RequestParam query: String, model: Model): String {
        println("🔍 Searching for: $query")

        return try {
            val articles = runBlocking { articleService.searchRealNews(query) }
            val magazines = magazineService.searchMagazines(query)

            model.addAttribute("articles", articles)
            model.addAttribute("magazines", magazines)
            model.addAttribute("searchQuery", query)

            if (articles.isEmpty() && magazines.isEmpty()) {
                model.addAttribute("noResults", true)
            }

            "search-results"
        } catch (e: Exception) {
            println("❌ Search error: ${e.message}")
            model.addAttribute("articles", emptyList<Article>())
            model.addAttribute("magazines", emptyList<Article>())
            model.addAttribute("error", "Search failed: ${e.message}")
            "search-results"
        }
    }

    @PostMapping("/articles/{id}/favorite")
    fun toggleArticleFavorite(@PathVariable id: String): String {
        articleService.toggleFavorite(id)
        return "redirect:/articles"
    }

    @PostMapping("/magazines/{id}/favorite")
    fun toggleMagazineFavorite(@PathVariable id: String): String {
        magazineService.toggleFavorite(id)
        return "redirect:/magazines"
    }

    @PostMapping("/fetch-articles")
    fun fetchArticles(): String {
        runBlocking {
            val newArticles = articleService.fetchArticlesFromNewsAPI()
            println("📰 Fetched ${newArticles.size} new articles")
        }
        return "redirect:/articles"
    }

    @GetMapping("/favorites")
    fun favorites(model: Model): String {
        model.addAttribute("favoriteArticles", articleService.getFavoriteArticles())
        model.addAttribute("favoriteMagazines", magazineService.getFavoriteMagazines())
        return "favorites"
    }

    @GetMapping("/api/debug/articles")
    @ResponseBody
    fun debugArticles(): List<Article> {
        return articleService.getAllArticles()
    }

    @GetMapping("/api/debug/status")
    @ResponseBody
    fun debugStatus(): Map<String, Any> {
        return mapOf(
            "articles" to articleService.getAllArticles().size,
            "magazines" to magazineService.getAllMagazines().size,
            "apiKeySet" to articleService.isApiKeyConfigured()
        )
    }
}