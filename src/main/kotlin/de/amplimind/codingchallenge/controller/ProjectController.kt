package de.amplimind.codingchallenge.controller

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.PathVariable

@RestController
@RequestMapping("/v1/project")
class ProjectController(
    private val projectService: ProjectService,
) {
    @Operation(summary = "Endpoint for deleting a project.")
    @ApiResponse(responseCode = "200", description = "Project was deleted successfully")
    @DeleteMapping("/project/{projectId}")
    fun deleteProject (
        @PathVariable projectId: Long
    ) {
        projectService.deleteProject(projectId)
    }
}
