package de.amplimind.codingchallenge.config

import de.amplimind.codingchallenge.model.Project
import de.amplimind.codingchallenge.model.Submission
import de.amplimind.codingchallenge.model.SubmissionStates
import de.amplimind.codingchallenge.model.User
import de.amplimind.codingchallenge.model.UserRole
import de.amplimind.codingchallenge.repository.ProjectRepository
import de.amplimind.codingchallenge.repository.SubmissionRepository
import de.amplimind.codingchallenge.repository.UserRepository
import org.springframework.beans.factory.annotation.Value
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
    private val submissionRepository: SubmissionRepository,
    private val projectRepository: ProjectRepository,
) {
    @Value("\${spring.custom.organization.name}")
    lateinit var organizationName: String

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
            // TODO just some example data for us, REMOVE LATER

            val admin =
                User(
                    email = "admin@web.de",
                    password = passwordEncoder().encode("admin"),
                    role = UserRole.ADMIN,
                )

            val user =
                User(
                    email = "user@web.de",
                    password = passwordEncoder().encode("user"),
                    role = UserRole.USER,
                )

            this.userRepository.saveAll(listOf(admin, user))

            val userSubmission =
                Submission(
                    userEmail = user.email,
                    status = SubmissionStates.INIT,
                    projectID = 1L,
                )

            this.submissionRepository.saveAll(listOf(userSubmission))

            val project1 =
                Project(
                    title = "Test Project",
                    description = "This is a test description",
                    active = true,
                    version = 0,
                )

            val project2 =
                Project(
                    title = "Test Project 2",
                    description = "This is another test description",
                    active = false,
                    version = 0,
                )

            this.projectRepository.saveAll(listOf(project1, project2))
        }
    }
}
