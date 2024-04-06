package de.amplimind.codingchallenge.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig {
    companion object {
        val OPEN_API_PATHS =
            arrayOf(
                "/v3/api-docs",
                "/v3/api-docs/**",
                "/swagger-resources/",
                "/swagger-resources/**",
                "/configuration/ui",
                "/webjars/**",
                "/swagger-ui.html",
                "/swagger-ui/**",
                "/v3/api-docs/swagger-config",
                "/v3/api-docs/public-api",
            )
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .csrf { it.disable() } // TODO Enable CSRF
            .authorizeHttpRequests {
                it.requestMatchers("/v1/hello/**", "/login").permitAll()
                    // TODO OPEN_API paths should be made restricted (authenticated) later (only for admin)
                    .requestMatchers(*OPEN_API_PATHS).permitAll()
            }
            .cors{ corsConfigurationSource() }
            .build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource   {
        val cors = CorsConfiguration()
        cors.allowedOriginPatterns = listOf("http://localhost:*") // todo: change to frontend URL
        cors.allowedMethods = listOf("GET", "POST", "PUT", "DELETE")
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", cors)
        return source
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
}
