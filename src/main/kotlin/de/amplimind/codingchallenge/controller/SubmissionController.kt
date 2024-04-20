package de.amplimind.codingchallenge.controller

import de.amplimind.codingchallenge.dto.LintResultDTO
import de.amplimind.codingchallenge.dto.SubmissionInfoDTO
import de.amplimind.codingchallenge.dto.request.SubmitSolutionRequestDTO
import de.amplimind.codingchallenge.service.GitHubService
import de.amplimind.codingchallenge.service.SubmissionService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/submission")
class SubmissionController (
    private val submissionService: SubmissionService,
    private val gitHubService: GitHubService,
) {
    @Operation(summary = "Endpoint for submitting a solution.")
    @ApiResponse(responseCode = "200", description = "Solution was submitted successfully.")
    @PostMapping("/submit")
    fun submit(
        @ModelAttribute submitSolutionRequestDTO: SubmitSolutionRequestDTO
    ): ResponseEntity<SubmissionInfoDTO> {
        val user: Any? = SecurityContextHolder.getContext().authentication.name;
        return ResponseEntity.ok(this.submissionService.submitCode(submitSolutionRequestDTO, user.toString()))
    }

    @Operation(summary = "Endpoint getting the linting result for a specific user.")
    @ApiResponse(responseCode = "200", description = "Solution was submitted successfully.")
    @GetMapping("/lint/{user}")
    fun submit(
        @PathVariable email: String,
    ) : ResponseEntity<LintResultDTO> {
        return ResponseEntity.ok(this.gitHubService.getLintingResult(email))
    }
}