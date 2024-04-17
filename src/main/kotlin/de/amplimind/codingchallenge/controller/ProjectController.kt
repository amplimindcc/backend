package de.amplimind.codingchallenge.controller

import de.amplimind.codingchallenge.dto.UserProjectDTO
import de.amplimind.codingchallenge.service.ProjectService
import de.amplimind.codingchallenge.service.SubmissionService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/project")
class ProjectController(
    private val submissionService: SubmissionService,
    private val projectService: ProjectService,
) {
    @Operation(summary = "Endpoint for deleting a project.")
    @ApiResponse(responseCode = "200", description = "Project was deleted successfully")
    @ApiResponse(responseCode = "409", description = "Project won't be deleted as it is still in use")
    @DeleteMapping("/project/{projectId}")
    fun deleteProject(
        @PathVariable projectId: Long,
    ) {
        projectService.deleteProject(projectId)
    }

    @Operation(summary = "Get project of user")
    @ApiResponse(responseCode = "200" , description = "Project fetched successfully")
    @ApiResponse(responseCode = "404", description = "User does not have a project assigned")
    @GetMapping("fetch/{email}")
    fun getUserProject(
        @PathVariable email : String,
    ): ResponseEntity<UserProjectDTO> {
        return ResponseEntity.ok(this.projectService.fetchProjectById(
            this.submissionService.getProjectIdOfUser(email)
        ))
    }
}
