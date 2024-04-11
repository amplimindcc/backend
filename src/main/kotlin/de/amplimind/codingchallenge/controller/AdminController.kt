package de.amplimind.codingchallenge.controller

import de.amplimind.codingchallenge.config.SecurityConfig
import de.amplimind.codingchallenge.dto.request.CreateProjectRequestDTO
import de.amplimind.codingchallenge.service.ProjectService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for admin related tasks.
 */
@RestController
@RequestMapping(SecurityConfig.ADMIN_PATH)
class AdminController(
    private val projectService: ProjectService,
) {
    @Operation(summary = "Endpoint for adding a new project.")
    @ApiResponse(responseCode = "200", description = "Project was added successfully.")
    @PostMapping("/project/add")
    fun addProject(
        @RequestBody createProjectRequest: CreateProjectRequestDTO,
    ) = this.projectService.addProject(createProjectRequest)
}
