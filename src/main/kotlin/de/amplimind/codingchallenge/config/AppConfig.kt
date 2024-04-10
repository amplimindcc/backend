package de.amplimind.codingchallenge.config

import de.amplimind.codingchallenge.model.User
import de.amplimind.codingchallenge.model.UserRole
import de.amplimind.codingchallenge.repository.UserRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
class AppConfig(
    private val userRepository: UserRepository,
) {
    @Bean
    fun userDetailsService(): UserDetailsService {
        return UserDetailsService {
            this.userRepository.findById(it).orElseThrow()
        }
    }

    @Bean
    @Throws(Exception::class)
    fun authenticationManager(configuration: AuthenticationConfiguration): AuthenticationManager {
        return configuration.authenticationManager
    }

    @Bean
    fun authenticationProvider(): AuthenticationProvider {
        val authenticationProvider = DaoAuthenticationProvider()
        authenticationProvider.setUserDetailsService(userDetailsService())
        authenticationProvider.setPasswordEncoder(passwordEncoder())
        return authenticationProvider
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun commandLineRunner(): CommandLineRunner {
        return CommandLineRunner {
            val admin =
                User(
                    username = "admin",
                    email = "admin@web.de",
                    password = passwordEncoder().encode("admin"),
                    role = UserRole.ADMIN,
                )

            val user =
                User(
                    username = "user",
                    email = "user@web.de",
                    password = passwordEncoder().encode("user"),
                    role = UserRole.USER,
                )

            val initUser =
                User(
                    username = "init",
                    email = "init@web.de",
                    password = null,
                    role = UserRole.INIT,
                )

            val registeredUser =
                User(
                    username = "registered",
                    email = "registered@web.de",
                    password = passwordEncoder().encode("registered"),
                    role = UserRole.USER,
                )

            val implementingUser =
                User(
                    username = "implementing",
                    email = "impl@web.de",
                    password = passwordEncoder().encode("impl"),
                    role = UserRole.USER,
                )

            val submittedUser =
                User(
                    username = "submitted",
                    email = "submitted@web.de",
                    password = passwordEncoder().encode("submitted"),
                    role = UserRole.USER,
                )

            this.userRepository.saveAll(listOf(admin, user, initUser, registeredUser, implementingUser, submittedUser))
        }
    }
}
