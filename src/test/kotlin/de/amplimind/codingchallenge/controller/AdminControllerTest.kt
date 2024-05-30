package de.amplimind.codingchallenge.controller

import com.fasterxml.jackson.databind.ObjectMapper
import de.amplimind.codingchallenge.dto.request.ChangeProjectActiveStatusRequestDTO
import de.amplimind.codingchallenge.dto.request.ChangeProjectTitleRequestDTO
import de.amplimind.codingchallenge.dto.request.CreateProjectRequestDTO
import de.amplimind.codingchallenge.dto.request.InviteRequestDTO
import de.amplimind.codingchallenge.model.SubmissionStates
import de.amplimind.codingchallenge.repository.ProjectRepository
import de.amplimind.codingchallenge.repository.SubmissionRepository
import de.amplimind.codingchallenge.repository.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import utils.TestDataInitializer

/**
 * Test class for [AdminController].
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
internal class AdminControllerTest
    @Autowired
    constructor(
        val mockMvc: MockMvc,
        val objectMapper: ObjectMapper,
        private val userRepository: UserRepository,
        private val submissionRepository: SubmissionRepository,
        private val projectRepository: ProjectRepository,
    ) {
        @Autowired
        private lateinit var jdbcTemplate: JdbcTemplate

        @BeforeEach
        fun setUp() {
            TestDataInitializer(
                userRepository,
                submissionRepository,
                projectRepository,
                jdbcTemplate = jdbcTemplate,
            ).initTestData()
        }

        @Test
        @WithMockUser(username = "admin", roles = ["ADMIN"])
        fun test_successful_project_add() {
            val request =
                CreateProjectRequestDTO(
                    title = "Test Project",
                    description = "This is a test description",
                    active = true,
                )

            this.mockMvc.post("/v1/admin/project/add") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isOk() }
            }
        }

        @Test
        @WithMockUser(username = "admin", roles = ["ADMIN"])
        fun test_successful_project_fetch() {
            this.mockMvc.get("/v1/admin/project/fetch/all").andExpect {
                status { isOk() }
            }
        }

        @Test
        @WithMockUser(username = "admin", roles = ["ADMIN"])
        fun test_change_submission_status_reviewed() {
            val emailToUse = "submitted@web.de"

            this.mockMvc.put("/v1/admin/change/submissionstate/reviewed/$emailToUse")
                .andExpect {
                    status { isOk() }
                    jsonPath("\$.userEmail") {
                        value(emailToUse)
                    }
                    jsonPath("\$.status") {
                        value(SubmissionStates.REVIEWED.toString())
                    }
                }
        }

        @Test
        @WithMockUser(username = "admin", roles = ["ADMIN"])
        fun test_change_submission_status_reviewed_failure() {
            this.mockMvc.put("/v1/admin/change/submissionstate/reviewed/unknown@web.de")
                .andExpect {
                    status { isNotFound() }
                }
        }

        @Test
        @WithMockUser(username = "admin", roles = ["ADMIN"])
        fun test_deleteUserByEmail_success() {
            val email = "user@web.de"

            this.mockMvc.delete("/v1/admin/user/$email")
                .andExpect {
                    status { isOk() }
                    jsonPath("\$.email") {
                        value(email)
                    }
                }
        }

        @Test
        @WithMockUser(username = "admin", roles = ["ADMIN"])
        fun test_deleteUserByEmail_failure() {
            val email = "unknown@web.de"

            this.mockMvc.delete("/v1/admin/user/$email")
                .andExpect {
                    status { isNotFound() }
                }
        }

        @Test
        @WithMockUser(username = "admin", roles = ["ADMIN"])
        fun test_fetch_all_users() {
            this.mockMvc.get("/v1/admin/fetch/users/all").andExpect {
                status { isOk() }
            }
        }

        @Test
        @WithMockUser(username = "admin", roles = ["ADMIN"])
        fun test_fetch_user_by_email_success() {
            val email = "user@web.de"

            this.mockMvc.get("/v1/admin/fetch/user/$email").andExpect {
                status { isOk() }
                jsonPath("\$.email") {
                    value(email)
                }
            }
        }

        @Test
        @WithMockUser(username = "admin", roles = ["ADMIN"])
        fun test_fetch_user_by_email_failure() {
            val email = "unknown@web.de"

            this.mockMvc.get("/v1/admin/fetch/user/$email").andExpect {
                status { isNotFound() }
            }
        }

        @Test
        @WithMockUser(username = "admin", roles = ["ADMIN"])
        fun test_create_invite_success() {
            val request =
                InviteRequestDTO(
                    email = "amplimindcodingchallenge@gmail.com",
                    isAdmin = true,
                )

            this.mockMvc.post("/v1/admin/invite") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isOk() }
            }
        }

        @Test
        @WithMockUser(username = "admin", roles = ["ADMIN"])
        fun test_create_invite_failure() {
            val request =
                InviteRequestDTO(
                    email = "invalid-email",
                    isAdmin = true,
                )

            this.mockMvc.post("/v1/admin/invite") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isUnprocessableEntity() }
            }
        }

        @Test
        @WithMockUser(username = "admin", roles = ["ADMIN"])
        fun test_change_project_active_status() {
            val projectId = 1L
            val changeRequest =
                ChangeProjectActiveStatusRequestDTO(
                    projectId = projectId,
                    active = false,
                )

            this.mockMvc.put("/v1/admin/change/project/active") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(changeRequest)
            }.andExpect {
                status { isOk() }
            }
        }

        @Test
        @WithMockUser(username = "admin", roles = ["ADMIN"])
        fun test_change_project_title() {
            val request =
                ChangeProjectTitleRequestDTO(
                    projectId = 1,
                    newTitle = "New Project Title",
                )

            this.mockMvc.put("/v1/admin/change/project/title") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isOk() }
            }
        }

        @Test
        @WithMockUser(username = "admin", roles = ["ADMIN"])
        fun test_fetch_all_submissions() {
            this.mockMvc.get("/v1/admin/submission/all").andExpect {
                status { isOk() }
            }
        }

        @Test
        @WithMockUser(username = "admin", roles = ["ADMIN"])
        fun test_resend_invite_success() {
            val request =
                InviteRequestDTO(
                    email = "amplimindcodingchallenge@gmail.com",
                    isAdmin = true,
                )

            this.mockMvc.post("/v1/admin/invite") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isOk() }
            }

            this.mockMvc.post("/v1/admin/resend/invite") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isOk() }
            }
        }

        @Test
        @WithMockUser(username = "admin", roles = ["ADMIN"])
        fun test_resend_invite_failure() {
            val request =
                InviteRequestDTO(
                    email = "unknown@web.de",
                    isAdmin = true,
                )

            this.mockMvc.post("/v1/admin/resend/invite") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isNotFound() }
            }
        }

        @Test
        @WithMockUser(username = "admin", roles = ["ADMIN"])
        fun test_fetch_expiration_for_invite_success() {
            val email = "amplimindcodingchallenge@gmail.com"

            val request =
                InviteRequestDTO(
                    email = "amplimindcodingchallenge@gmail.com",
                    isAdmin = true,
                )

            this.mockMvc.post("/v1/admin/invite") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isOk() }
            }

            this.mockMvc.get("/v1/admin/invite/expiration/$email").andExpect {
                status { isOk() }
            }
        }

        @Test
        @WithMockUser(username = "admin", roles = ["ADMIN"])
        fun test_fetch_expiration_for_invite_failure() {
            val email = "unknown@web.de"

            this.mockMvc.get("/v1/admin/invite/expiration/$email").andExpect {
                status { isNotFound() }
            }
        }

        @Test
        @WithMockUser(username = "admin", roles = ["ADMIN"])
        fun test_delete_project_success() {
            val projectId = 2L
            this.mockMvc.delete("/v1/admin/project/$projectId").andExpect {
                status { isOk() }
            }
        }

        @Test
        @WithMockUser(username = "admin", roles = ["ADMIN"])
        fun test_delete_project_failure_still_in_use() {
            val projectId = 1L
            this.mockMvc.delete("/v1/admin/project/$projectId").andExpect {
                status { isConflict() }
            }
        }

        @Test
        @WithMockUser(username = "admin", roles = ["ADMIN"])
        fun test_delete_project_failure_not_found() {
            val projectId = 999009L

            this.mockMvc.delete("/v1/admin/project/$projectId").andExpect {
                status { isNotFound() }
            }
        }

        @Test
        @WithMockUser(username = "admin", roles = ["ADMIN"])
        fun test_get_user_project_success() {
            val email = "user@web.de"

            this.mockMvc.get("/v1/admin/fetch/project/$email").andExpect {
                status { isOk() }
            }
        }

        @Test
        @WithMockUser(username = "admin", roles = ["ADMIN"])
        fun test_get_user_project_failure() {
            val email = "unknown@web.de"

            this.mockMvc.get("/v1/admin/fetch/project/$email").andExpect {
                status { isNotFound() }
            }
        }

        @Test
        @WithMockUser(username = "admin", roles = ["ADMIN"])
        fun test_download_user_project_success() {
            val email = "user@web.de"

            this.mockMvc.get("/v1/admin/download/project/$email").andExpect {
                status { isOk() }
            }
        }

        @Test
        @WithMockUser(username = "admin", roles = ["ADMIN"])
        fun test_download_user_project_failure_user_not_found() {
            val email = "unknown@web.de"

            this.mockMvc.get("/v1/admin/download/project/$email").andExpect {
                status { isNotFound() }
            }
        }

        @Test
        @WithMockUser(username = "admin", roles = ["ADMIN"])
        fun test_download_user_project_failure_project_not_found() {
            val email = "init@web.de"

            this.mockMvc.get("/v1/admin/download/project/$email").andExpect {
                status { isUnprocessableEntity() }
            }
        }
    }
