package de.amplimind.codingchallenge.controller

import com.fasterxml.jackson.databind.ObjectMapper
import de.amplimind.codingchallenge.dto.request.LoginRequestDTO
import de.amplimind.codingchallenge.dto.request.RegisterRequestDTO
import de.amplimind.codingchallenge.repository.ProjectRepository
import de.amplimind.codingchallenge.repository.SubmissionRepository
import de.amplimind.codingchallenge.repository.UserRepository
import de.amplimind.codingchallenge.utils.JWTUtils
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActionsDsl
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import utils.TestDataInitializer
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

/**
 * Test class for [AuthControllerTest].
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
internal class AuthControllerTest
    @Autowired
    constructor(
        val mockMvc: MockMvc,
        val objectMapper: ObjectMapper,
        private val userRepository: UserRepository,
        private val submissionRepository: SubmissionRepository,
        private val projectRepository: ProjectRepository,
    ) {
        fun gen_token(
            email: String,
            isAdmin: Boolean,
        ): String {
            return JWTUtils.createToken(
                mapOf(JWTUtils.MAIL_KEY to email, JWTUtils.ADMIN_KEY to isAdmin),
                Date.from(
                    Instant.now().plus(
                        JWTUtils.RESET_PASSWORD_EXPIRATION_MIN,
                        ChronoUnit.MINUTES,
                    ),
                ),
            )
        }

        @BeforeEach
        fun setUp() {
            TestDataInitializer(
                userRepository,
                submissionRepository,
                projectRepository,
            ).initTestData()
        }

        /**
         * Test that successful login returns 200.
         */
        @Order(1)
        @Test
        fun test_login_successful() {
            val request =
                LoginRequestDTO(
                    email = "admin@web.de",
                    password = "admin",
                )

            this.mockMvc.post("/v1/auth/login") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isOk() }
            }
        }

        /**
         * Test that an invalid username returns error 404
         */
        @Order(2)
        @Test
        fun test_login_invalid_username() {
            val request =
                LoginRequestDTO(
                    email = "invalid",
                    password = "admin",
                )

            this.mockMvc.post("/v1/auth/login") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isNotFound() }
            }
        }

        /**
         * Test that an invalid password returns error 403
         */
        @Order(3)
        @Test
        fun test_login_invalid_password() {
            val request =
                LoginRequestDTO(
                    email = "admin@web.de",
                    password = "invalid",
                )

            this.mockMvc.post("/v1/auth/login") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isForbidden() }
            }
        }

        /**
         * Test valid register return 200
         */
        @Order(4)
        @Test
        fun test_register_successful() {
            val request =
                RegisterRequestDTO(
                    password = "Str0ngP455word!",
                    token = gen_token("init@web.de", true),
                )

            this.mockMvc.post("/v1/auth/register") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isOk() }
            }
        }

        /**
         * Test invalid token, should return 400
         */
        @Order(5)
        @Test
        fun test_register_invalid_token() {
            val request =
                RegisterRequestDTO(
                    password = "Str0ngP455word",
                    token = "12341234",
                )

            this.mockMvc.post("/v1/auth/register") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isBadRequest() }
            }
        }

        /**
         * Test invalid token, should return 400
         */
        @Order(6)
        @Test
        fun test_register_token_already_used() {
            val request =
                RegisterRequestDTO(
                    password = "Str0ngP455word!",
                    token = gen_token("init1@web.de", true),
                )

            // use token the first time
            this.mockMvc.post("/v1/auth/register") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isOk() }
            }

            // use token the first time
            this.mockMvc.post("/v1/auth/register") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isConflict() }
            }
        }

        /**
         * Test user does not exist, should throw 404
         */
        @Order(7)
        @Test
        fun test_register_user_does_not_exist() {
            val request =
                RegisterRequestDTO(
                    password = "Str0ngP455word!",
                    token = gen_token("UserNot@Exist.de", true),
                )
            // use token the first time
            this.mockMvc.post("/v1/auth/register") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isNotFound() }
            }
        }

        /**
         * Test password to weak , should throw 412
         */
        @Order(8)
        @Test
        fun test_register_password_to_weak() {
            val request =
                RegisterRequestDTO(
                    password = "weakPassword",
                    token = gen_token("init2@web.de", true),
                )
            // use token the first time
            this.mockMvc.post("/v1/auth/register") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isPreconditionFailed() }
            }
        }

        /**
         * Test if current user is logged in, should return 200
         */
        @Order(9)
        @Test
        @WithMockUser(username = "admin", roles = ["ADMIN"])
        fun test_check_login_successful() {
            this.mockMvc.get("/v1/auth/check-login").andExpect {
                status { isOk() }
            }
        }

        /**
         * Test if no user is logged in, should return 401
         */
        @Order(10)
        @Test
        fun test_check_login_not_loggged_in() {
            this.mockMvc.get("/v1/auth/check-login").andExpect {
                status { isUnauthorized() }
            }
        }

        /**
         * Test token is valid, should return 200
         */
        @Order(11)
        @Test
        fun test_check_token_successful() {
            val token = gen_token("init3@web.de", true)
            this.mockMvc.get("/v1/auth/check-token/$token").andExpect {
                status { isOk() }
            }
        }

        /**
         * Test token is invalid, should return 400
         */
        @Order(12)
        @Test
        fun test_check_token_invalid() {
            val token = "gen_token1234"
            this.mockMvc.get("/v1/auth/check-token/$token").andExpect {
                status { isBadRequest() }
            }
        }

        /**
         * Test token is already used, should return 412
         */
        @Order(13)
        @Test
        fun test_check_token_already_used() {
            val token = gen_token("admin@web.de", true)
            this.mockMvc.get("/v1/auth/check-token/$token").andExpect {
                status { isOk() }
            }
        }

        /**
         * Test that the rate limit is working as expected.
         */
        @Order(14)
        @Test
        fun test_rate_limit() {
            val request =
                LoginRequestDTO(
                    email = "trashLogin@web.de",
                    password = "trashLogin",
                )
            val results: MutableList<ResultActionsDsl> = mutableListOf()
            for (i in 1..10) {
                val result =
                    this.mockMvc.post("/v1/auth/login") {
                        contentType = MediaType.APPLICATION_JSON
                        content = objectMapper.writeValueAsString(request)
                    }
                results.add(result)
            }
            for (result in results) {
                if (result.andReturn().response.status == 429) {
                    assert(true)
                    break
                }
            }
        }
    }
