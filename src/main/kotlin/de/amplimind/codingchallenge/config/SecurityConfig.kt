package de.amplimind.codingchallenge.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

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
            .build()
    }

    @Bean
    fun cors(): WebMvcConfigurer {
        return object : WebMvcConfigurer {
            override fun addCorsMappings(registry: CorsRegistry) {
                registry
                    .addMapping("/**")
                    .allowedOriginPatterns("http://localhost:*") // todo: change to frontend url
                    .allowedMethods("*")
            }
        }
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
}
