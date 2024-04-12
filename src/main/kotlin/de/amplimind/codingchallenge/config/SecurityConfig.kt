package de.amplimind.codingchallenge.config

import de.amplimind.codingchallenge.model.UserRole
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val authenticationProvider: AuthenticationProvider,
    private val authenticationFilter: AuthenticationFilter,
) {
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

        const val ADMIN_PATH = "/v1/admin/"
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .csrf {it.disable()} // TODO Enable CSRF
            .authorizeHttpRequests {
                it.requestMatchers("/v1/hello/**", "/v1/auth/**", "/whoami").permitAll()
                    // TODO OPEN_API should be secured in the end (only admin)
                    .requestMatchers(*OPEN_API_PATHS).permitAll()

                it.requestMatchers("${ADMIN_PATH}**").hasRole(UserRole.ADMIN.name)
                it.requestMatchers("/logout").permitAll()
                it.requestMatchers("/login").permitAll().anyRequest().authenticated()
            }
            .authenticationProvider(authenticationProvider)
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            }
            .cors { corsConfigurationSource() }
            // TODO Handle logout (cookie
            .build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val cors = CorsConfiguration()
        cors.allowedOriginPatterns = listOf("http://localhost:*") // todo: change to frontend URL
        cors.allowedMethods = listOf("GET", "POST", "PUT", "DELETE")
        cors.allowedHeaders = listOf("*") // todo: change to specific headers
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", cors)
        return source
    }
}
