package de.amplimind.codingchallenge.controller

import com.fasterxml.jackson.databind.ObjectMapper
import de.amplimind.codingchallenge.dto.request.ChangePasswordRequestDTO
import de.amplimind.codingchallenge.repository.ProjectRepository
import de.amplimind.codingchallenge.repository.SubmissionRepository
import de.amplimind.codingchallenge.repository.UserRepository
import de.amplimind.codingchallenge.service.EmailService
import de.amplimind.codingchallenge.utils.JWTUtils
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import utils.TestDataInitializer
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date

/**
 *   Test class for [AccountController]
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
internal class AccountControllerTest
    @Autowired
    constructor(
        val mackMvc: MockMvc,
        val objectMapper: ObjectMapper,
        private val userRepository: UserRepository,
        private val submissionRepository: SubmissionRepository,
        private val projectRepository: ProjectRepository,
    ) {
        fun gen_token(email: String): String {
            return JWTUtils.createToken(
                mapOf(JWTUtils.MAIL_KEY to email),
                Date.from(
                    Instant.now().plus(
                        JWTUtils.RESET_PASSWORD_EXPIRATION_MIN,
                        ChronoUnit.MINUTES,
                    ),
                ),
            )
        }

        @MockK
        private lateinit var emailService: EmailService

        @BeforeEach
        fun setUp() {
            MockKAnnotations.init(this)
            TestDataInitializer(
                userRepository,
                submissionRepository,
                projectRepository,
            ).initTestData()
        }

        /**
         *  Test reset-email is sent when requesting a password change
         */

        @Test
        @WithMockUser(username = "admin", roles = ["ADMIN"])
        fun test_password_reset_email_sent_successful() {
            every { emailService.sendEmail(any(), any(), any()) } just Runs

            val emailToResetPassword = "admin@web.de"
            this.mackMvc.post("/v1/account/request-password-change/$emailToResetPassword") {
            }.andExpect {
                status { isOk() }
            }
        }

        /**
         *  Test error 422 is thrown if email has the wrong format
         */

        @Test
        @WithMockUser(username = "admin", roles = ["ADMIN"])
        fun test_password_reset_email_sent_invalid_email() {
            val emailToResetPassword = "invalid@mail.de."
            this.mackMvc.post("/v1/account/request-password-change/$emailToResetPassword") {
            }.andExpect {
                status { isUnprocessableEntity() }
            }
        }

        /**
         *  Test error 404 is thrown if user with email does not exist
         */

        @Test
        @WithMockUser(username = "admin", roles = ["ADMIN"])
        fun test_password_reset_email_sent_user_not_found() {
            val emailToResetPassword = "userNot@exists.de"
            this.mackMvc.post("/v1/account/request-password-change/$emailToResetPassword") {
            }.andExpect {
                status { isNotFound() }
            }
        }

        /**
         *  Test that a password is reset if a correct token is provided
         */

        @Test
        @WithMockUser(username = "admin", roles = ["ADMIN"])
        fun test_password_reset_successful() {
            val emailForPasswordReset = "admin@web.de"

            val token = gen_token(emailForPasswordReset)

            val request =
                ChangePasswordRequestDTO(
                    token = token,
                    newPassword = "Th1sIsAVerySafeP4ssw0rd!",
                )

            this.mackMvc.post("/v1/account/change-password") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isOk() }
            }
        }

        /**
         *  Test that an 409 is thrown if a token was already used
         */

        @Test
        @WithMockUser(username = "admin", roles = ["ADMIN"])
        fun test_password_reset_conflict() {
            val emailForPasswordReset = "admin@web.de"

            val token = gen_token(emailForPasswordReset)

            val request =
                ChangePasswordRequestDTO(
                    token = token,
                    newPassword = "Th1sIsAVerySafeP4ssw0rd!",
                )

            // send token the first time
            this.mackMvc.post("/v1/account/change-password") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isOk() }
            }

            // use the same token the second time
            this.mackMvc.post("/v1/account/change-password") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isConflict() }
            }
        }

        /**
         *  Test that an 422 is thrown if a token is invalid
         */

        @Test
        @WithMockUser(username = "admin", roles = ["ADMIN"])
        fun test_password_reset_invalid_token() {
            val request =
                ChangePasswordRequestDTO(
                    token = "1234",
                    newPassword = "Th1sIsAVerySafeP4ssw0rd!",
                )

            this.mackMvc.post("/v1/account/change-password") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isBadRequest() }
            }
        }

        /**
         *  Test that an 412 is thrown if the password does not fulfill the requirements
         */

        @Test
        @WithMockUser(username = "admin", roles = ["ADMIN"])
        fun test_password_reset_invalid_password() {
            val request =
                ChangePasswordRequestDTO(
                    token = gen_token("admin@web.de"),
                    newPassword = "1234",
                )

            this.mackMvc.post("/v1/account/change-password") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isPreconditionFailed() }
            }
        }
    }
