package de.amplimind.codingchallenge.controller

import de.amplimind.codingchallenge.dto.request.SubmitSolutionRequestDTO
import de.amplimind.codingchallenge.dto.response.LintResultResponseDTO
import de.amplimind.codingchallenge.dto.response.SubmissionActiveInfoDTO
import de.amplimind.codingchallenge.dto.response.SubmissionInfoResponseDTO
import de.amplimind.codingchallenge.service.SubmissionService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/submission/")
class SubmissionController(
    private val submissionService: SubmissionService,
) {
    @Operation(summary = "Endpoint for submitting a solution.")
    @ApiResponse(responseCode = "200", description = "Solution was submitted successfully.")
    @ApiResponse(
        responseCode = "400",
        description = "The submission was flagged as a zip bomb as it has a nested zip file or being too large.",
    )
    @ApiResponse(responseCode = "409", description = "Readme file was present which results in a conflict.")
    @PostMapping("submit")
    fun submit(
        @ModelAttribute submitSolutionRequestDTO: SubmitSolutionRequestDTO,
    ): ResponseEntity<SubmissionInfoResponseDTO> {
        return ResponseEntity.ok(this.submissionService.submitCode(submitSolutionRequestDTO))
    }

    @Operation(summary = "Endpoint for getting the linting result for a specific user.")
    @ApiResponse(responseCode = "200", description = "Get the linting results")
    @ApiResponse(responseCode = "404", description = "If there is no linting result for the requested user")
    @GetMapping("lint/{email}")
    fun getLinterResult(
        @PathVariable email: String,
    ): ResponseEntity<LintResultResponseDTO> {
        return ResponseEntity.ok(this.submissionService.getLinterResponse(email))
    }

    @Operation(summary = "Endpoint for fetching the submission active info.")
    @ApiResponse(responseCode = "200", description = "Get the submission active info")
    @ApiResponse(responseCode = "401", description = "If this request was made without being logged in (authenticated)")
    @ApiResponse(responseCode = "404", description = "If there is no submission for the requesting user")
    @GetMapping("active")
    fun fetchSubmissionActiveInfo(): ResponseEntity<SubmissionActiveInfoDTO> {
        return ResponseEntity.ok(this.submissionService.fetchSubmissionActiveInfo())
    }
}
