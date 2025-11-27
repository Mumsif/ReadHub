package com.example.readhub.controller

import com.example.readhub.model.Article
import com.example.readhub.service.ArticleService  // Change this
import com.example.readhub.service.MagazineService  // Change this
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
@RequestMapping("/")
class WebController(
    private val articleService: ArticleService,
    private val magazineService: MagazineService
) {

    @GetMapping
    fun home(model: Model): String {
        try {
            println("🏠 Loading home page...")

            // Get articles and magazines
            val articles = articleService.getAllArticles().take(6)
            val magazines = magazineService.getAllMagazines().take(3)

            println("📰 Home page: ${articles.size} articles, ${magazines.size} magazines")

            // Debug: Print what we're sending to the template
            articles.forEachIndexed { index, article ->
                println("📖 Article $index: ${article.title}")
            }
            magazines.forEachIndexed { index, magazine ->
                println("📚 Magazine $index: $magazine")
            }

            model.addAttribute("articles", articles)
            model.addAttribute("magazines", magazines)

        } catch (e: Exception) {
            println("❌ Error in home controller: ${e.message}")
            e.printStackTrace()
            // Add empty lists to avoid template errors
            model.addAttribute("articles", emptyList<Article>())
            model.addAttribute("magazines", emptyList<String>())
        }
        return "index"
    }



    @GetMapping("/articles")
    fun articles(model: Model): String {
        println("🔄 /articles endpoint called")
        val articles = articleService.getAllArticles()
        println("📰 Found ${articles.size} articles")

        // Debug: Print article titles
        articles.forEach { article ->
            println("📖 Article: ${article.title} - Content length: ${article.content.length}")
        }

        model.addAttribute("articles", articles)
        return "articles"
    }

    @GetMapping("/magazines")
    fun magazines(model: Model): String {
        model.addAttribute("magazines", magazineService.getAllMagazines())
        return "magazines"
    }

    @GetMapping("/search")
    suspend fun search(@RequestParam query: String, model: Model): String {
        println("🔍 Searching for: $query")

        try {
            // Use REAL news search instead of just local search
            val articles = articleService.searchRealNews(query)
            val magazines = magazineService.searchMagazines(query)

            println("📰 Found ${articles.size} articles, ${magazines.size} magazines")

            model.addAttribute("articles", articles)
            model.addAttribute("magazines", magazines)
            model.addAttribute("searchQuery", query)

            if (articles.isEmpty() && magazines.isEmpty()) {
                model.addAttribute("noResults", true)
                println("❌ No results found for: $query")
            }

        } catch (e: Exception) {
            println("❌ Search error: ${e.message}")
            model.addAttribute("articles", emptyList<Article>())
            model.addAttribute("magazines", emptyList<String>())
            model.addAttribute("error", "Search failed: ${e.message}")
        }

        return "search-results"
    }

    @GetMapping("/debug")
    fun debug(): String {
        return "Debug: If you see this, Spring Boot is working!"
    }

    @PostMapping("/articles/{id}/favorite")
    fun toggleArticleFavorite(@PathVariable id: String, redirectAttributes: RedirectAttributes): String {
        articleService.toggleFavorite(id)
        redirectAttributes.addFlashAttribute("message", "Favorite status updated")
        return "redirect:/articles"
    }

    @PostMapping("/magazines/{id}/favorite")
    fun toggleMagazineFavorite(@PathVariable id: String, redirectAttributes: RedirectAttributes): String {
        magazineService.toggleFavorite(id)
        redirectAttributes.addFlashAttribute("message", "Favorite status updated")
        return "redirect:/magazines"
    }

    @PostMapping("/fetch-articles")
    suspend fun fetchArticles(redirectAttributes: RedirectAttributes): String {
        val newArticles = articleService.fetchArticlesFromNewsAPI()
        // Optional: Save to database
        // articleRepository.saveAll(newArticles)

        redirectAttributes.addFlashAttribute("message", "Fetched ${newArticles.size} articles from API")
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

}