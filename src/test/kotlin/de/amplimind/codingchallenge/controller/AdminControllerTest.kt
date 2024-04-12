package de.amplimind.codingchallenge.controller

import com.fasterxml.jackson.databind.ObjectMapper
import de.amplimind.codingchallenge.dto.request.ChangeUserRoleRequestDTO
import de.amplimind.codingchallenge.dto.request.CreateProjectRequestDTO
import de.amplimind.codingchallenge.model.UserRole
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * Test class for [AdminController].
 */
@SpringBootTest
@AutoConfigureMockMvc
internal class AdminControllerTest
    @Autowired
    constructor(
        val mockMvc: MockMvc,
        val objectMapper: ObjectMapper,
    ) {
        /**
         * Test that a project can be added successfully.
         */
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

        /**
         * Makes sure a 200 OK will be returned when a role change succeeds
         */
        @Test
        @WithMockUser(username = "admin", roles = ["ADMIN"])
        fun test_successful_role_change() {
            val successfulRequest =
                ChangeUserRoleRequestDTO(
                    email = "user@web.de",
                    newRole = UserRole.ADMIN,
                )

            this.mockMvc.put("/v1/admin/change/role") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(successfulRequest)
            }.andExpect {
                status { isOk() }
            }
        }

        /**
         * Makes sure a 404 not found will be returned when trying to change the role of a user that does not exist.
         */
        @Test
        @WithMockUser(username = "admin", roles = ["ADMIN"])
        fun test_failure_role_change() {
            val failureRequest =
                ChangeUserRoleRequestDTO(
                    email = "unknown@web.de",
                    newRole = UserRole.ADMIN,
                )

            this.mockMvc.put("/v1/admin/change/role") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(failureRequest)
            }.andExpect {
                status { isNotFound() }
            }
        }

        /**
         * Test that all projects are fetched correctly.
         */
        @Test
        @WithMockUser(username = "admin", roles = ["ADMIN"])
        fun test_successful_project_fetch() {
            // TODO check here for the response data as soon as H2 is used
            this.mockMvc.get("/v1/admin/project/fetch/all").andExpect {
                status { isOk() }
            }
        }
    }
