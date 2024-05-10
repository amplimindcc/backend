package de.amplimind.codingchallenge.controller

import com.fasterxml.jackson.databind.ObjectMapper
import de.amplimind.codingchallenge.dto.request.LoginRequestDTO
import de.amplimind.codingchallenge.repository.ProjectRepository
import de.amplimind.codingchallenge.repository.SubmissionRepository
import de.amplimind.codingchallenge.repository.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import utils.TestDataInitializer

/**
 * Test class for [ProjectController].
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
internal class ProjectControllerTest

@Autowired
constructor(
    val mockMvc: MockMvc,
    val objectMapper: ObjectMapper,
    private val userRepository: UserRepository,
    private val submissionRepository: SubmissionRepository,
    private val projectRepository: ProjectRepository,
    private val jdbcTemplate: JdbcTemplate
) {

    @BeforeEach
    fun setUp() {
        TestDataInitializer(
            userRepository,submissionRepository,projectRepository, jdbcTemplate = jdbcTemplate
        ).initTestData()
    }


    @Test
    @WithMockUser(username = "user@web.de", roles = ["USER"])
    fun test_login_invalid_password() {

        this.mockMvc.get("/v1/project/fetch")
            .andExpect {
            status { isOk() }
        }
    }

}