package de.amplimind.codingchallenge.config

import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AppConfig {
    @Bean
    fun commandLineRunner(): CommandLineRunner {
        return CommandLineRunner {
        }
    }
}
