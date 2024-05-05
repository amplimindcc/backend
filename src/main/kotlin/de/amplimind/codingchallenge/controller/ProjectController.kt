package de.amplimind.codingchallenge.controller

import de.amplimind.codingchallenge.dto.response.UserProjectResponseDTO
import de.amplimind.codingchallenge.service.ProjectService
import de.amplimind.codingchallenge.service.SubmissionService
import de.amplimind.codingchallenge.utils.UserUtils
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/project")
class ProjectController(
    private val submissionService: SubmissionService,
    private val projectService: ProjectService,
) {
    @Operation(summary = "Get project of user")
    @ApiResponse(responseCode = "200", description = "Project fetched successfully")
    @ApiResponse(responseCode = "404", description = "User does not have a project assigned")
    @ApiResponse(responseCode = "422", description = "Supplied email is not an email")
    @GetMapping("/fetch")
    fun getUserProject(): ResponseEntity<UserProjectResponseDTO> {
        val email: String = UserUtils.fetchLoggedInUser().username
        this.submissionService.setExpirationIfNotSet(email)
        return ResponseEntity.ok(
            this.projectService.fetchProjectById(
                this.submissionService.getProjectIdOfUser(email),
            ),
        )
    }
}
