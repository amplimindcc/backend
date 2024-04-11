package de.amplimind.codingchallenge.config

import de.amplimind.codingchallenge.model.Submission
import de.amplimind.codingchallenge.model.SubmissionStates
import de.amplimind.codingchallenge.model.User
import de.amplimind.codingchallenge.model.UserRole
import de.amplimind.codingchallenge.repository.SubmissionRepository
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
import java.sql.Timestamp

@Configuration
class AppConfig(
    private val userRepository: UserRepository,
    private val submissionRepository: SubmissionRepository,
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

            val initUser =
                User(
                    email = "init@web.de",
                    password = null,
                    role = UserRole.INIT,
                )

            val implementingUser =
                User(
                    email = "impl@web.de",
                    password = passwordEncoder().encode("impl"),
                    role = UserRole.USER,
                )

            val submittedUser =
                User(
                    email = "submitted@web.de",
                    password = passwordEncoder().encode("submitted"),
                    role = UserRole.USER,
                )

            this.userRepository.saveAll(listOf(admin, user, initUser, implementingUser, submittedUser))

            val userSubmission =
                Submission(
                    userEmail = user.email,
                    status = SubmissionStates.INIT,
                    turnInDate = Timestamp(System.currentTimeMillis()),
                    projectID = 1L,
                    expirationDate = Timestamp(System.currentTimeMillis()),
                )

            val initUserSubmission =
                Submission(
                    userEmail = initUser.email,
                    status = SubmissionStates.INIT,
                    turnInDate = Timestamp(System.currentTimeMillis()),
                    projectID = 1L,
                    expirationDate = Timestamp(System.currentTimeMillis()),
                )

            val inImplementationSubmission =
                Submission(
                    userEmail = implementingUser.email,
                    status = SubmissionStates.IN_IMPLEMENTATION,
                    turnInDate = Timestamp(System.currentTimeMillis()),
                    projectID = 1L,
                    expirationDate = Timestamp(System.currentTimeMillis()),
                )

            val submittedSubmission =
                Submission(
                    userEmail = submittedUser.email,
                    status = SubmissionStates.SUBMITTED,
                    turnInDate = Timestamp(System.currentTimeMillis()),
                    projectID = 1L,
                    expirationDate = Timestamp(System.currentTimeMillis()),
                )

            this.submissionRepository.saveAll(listOf(inImplementationSubmission, submittedSubmission, initUserSubmission, userSubmission))
        }
    }
}
