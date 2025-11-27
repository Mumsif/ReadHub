package com.example.readhub.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "api")
class ApiConfig {
    var news: NewsConfig = NewsConfig()
    var gnews: GnewsConfig = GnewsConfig()
    var guardian: GuardianConfig = GuardianConfig()

    data class NewsConfig(var key: String = "")
    data class GnewsConfig(var key: String = "")
    data class GuardianConfig(var key: String = "")
}