package de.amplimind.codingchallenge.controller

import de.amplimind.codingchallenge.dto.request.SubmitSolutionRequestDTO
import de.amplimind.codingchallenge.service.SubmissionService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/v1/submission")
class SubmissionController (
    private val submissionService: SubmissionService,
) {
    @Operation(summary = "Endpoint for submitting a solution.")
    @ApiResponse(responseCode = "200", description = "Solution was submitted successfully.")
    @PostMapping("/submit")
    suspend fun submit(
        @RequestBody submitSolutionRequestDTO: SubmitSolutionRequestDTO
    ) {
        val user: Any? = SecurityContextHolder.getContext().authentication.name;
        this.submissionService.submitCode(submitSolutionRequestDTO, user.toString())
    }
}