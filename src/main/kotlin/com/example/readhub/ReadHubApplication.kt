package com.example.readhub

import com.example.readhub.config.ApiConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties

import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(ApiConfig::class)
class ReadHubApplication

fun main(args: Array<String>) {
    runApplication<ReadHubApplication>(*args)
}
