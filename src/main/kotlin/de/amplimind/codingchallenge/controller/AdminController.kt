package de.amplimind.codingchallenge.controller

import de.amplimind.codingchallenge.config.SecurityConfig
import de.amplimind.codingchallenge.dto.DeletedUserInfoDTO
import de.amplimind.codingchallenge.dto.FullUserInfoDTO
import de.amplimind.codingchallenge.dto.SubmissionInfoDTO
import de.amplimind.codingchallenge.dto.UserInfoDTO
import de.amplimind.codingchallenge.dto.UserProjectDTO
import de.amplimind.codingchallenge.dto.request.ChangeProjectActiveStatusRequestDTO
import de.amplimind.codingchallenge.dto.request.ChangeProjectTitleRequestDTO
import de.amplimind.codingchallenge.dto.request.CreateProjectRequestDTO
import de.amplimind.codingchallenge.dto.request.InviteRequestDTO
import de.amplimind.codingchallenge.service.InviteTokenExpirationService
import de.amplimind.codingchallenge.service.ProjectService
import de.amplimind.codingchallenge.service.SubmissionService
import de.amplimind.codingchallenge.service.UserService
import de.amplimind.codingchallenge.utils.ValidationUtils
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
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
    private val userService: UserService,
    private val submissionService: SubmissionService,
    private val inviteTokenExpirationService: InviteTokenExpirationService,
) {
    @Operation(summary = "Endpoint for adding a new project.")
    @ApiResponse(responseCode = "200", description = "Project was added successfully.")
    @PostMapping("/project/add")
    fun addProject(
        @RequestBody createProjectRequest: CreateProjectRequestDTO,
    ) = this.projectService.addProject(createProjectRequest)

    @Operation(summary = "Endpoint for fetching all projects.")
    @ApiResponse(responseCode = "200", description = "All projects were fetched successfully.")
    @GetMapping("/project/fetch/all")
    fun fetchAllProjects() = ResponseEntity.ok(this.projectService.fetchAllProjects())

    @Operation(summary = "Endpoint for fetching all infos for the users")
    @ApiResponse(responseCode = "200", description = "All user infos were fetched successfully.")
    @GetMapping("fetch/users/all")
    fun fetchAllUsers(): ResponseEntity<List<FullUserInfoDTO>> {
        return ResponseEntity.ok(this.userService.fetchAllUserInfos())
    }

    @Operation(summary = "Endpoint for fetching the info for a user by email")
    @ApiResponse(responseCode = "200", description = "User info was fetched successfully.")
    @ApiResponse(responseCode = "404", description = "User with email was not found.")
    @ApiResponse(responseCode = "422", description = "Email supplied is not an email.")
    @GetMapping("fetch/projects/{email}")
    fun fetchUserInfosForEmail(
        @PathVariable email: String,
    ): ResponseEntity<UserInfoDTO> {
        ValidationUtils.validateEmail(email)
        val userInfo = this.userService.fetchUserInfosForEmail(email)
        return ResponseEntity.ok(userInfo)
    }

    @Operation(summary = "Endpoint for deleting a user by email")
    @ApiResponse(responseCode = "200", description = "User was deleted successfully.")
    @ApiResponse(responseCode = "404", description = "User with email was not found.")
    @ApiResponse(responseCode = "422", description = "Email supplied is not an email.")
    @DeleteMapping("user/{email}")
    fun deleteUserByEmail(
        @PathVariable email: String,
    ): ResponseEntity<DeletedUserInfoDTO> {
        ValidationUtils.validateEmail(email)
        val userInfo = this.userService.deleteUserByEmail(email)
        return ResponseEntity.ok(userInfo)
    }

    @Operation(summary = "Endpoint for creating User and emailing him the invite")
    @ApiResponse(responseCode = "200", description = "User info was fetched successfully.")
    @ApiResponse(responseCode = "409", description = "User already exists")
    @ApiResponse(responseCode = "422", description = "Email supplied is not an email.")
    @PostMapping("invite")
    fun createInvite(
        @RequestBody inviteRequest: InviteRequestDTO,
    ): ResponseEntity<UserInfoDTO> {
        ValidationUtils.validateEmail(inviteRequest.email)
        return ResponseEntity.ok(this.userService.handleInvite(inviteRequest))
    }

    @Operation(summary = "Endpoint for changing the state of a submission to reviewed")
    @ApiResponse(responseCode = "200", description = "Submission state was changed successfully.")
    @ApiResponse(responseCode = "400", description = "If the submission is not in state from which it can be set to reviewed")
    @ApiResponse(responseCode = "404", description = "If the submission for the provided email was not found.")
    @ApiResponse(responseCode = "422", description = "Email supplied is not an email.")
    @PutMapping("change/submissionstate/reviewed/{email}")
    fun changeSubmissionStateReviewed(
        @PathVariable email: String,
    ): ResponseEntity<SubmissionInfoDTO> {
        ValidationUtils.validateEmail(email)
        return ResponseEntity.ok(this.submissionService.changeSubmissionStateReviewed(email))
    }

    @Operation(summary = "Endpoint for changing the state of a submission to submitted")
    @ApiResponse(responseCode = "200", description = "Submission state was changed successfully.")
    @ApiResponse(responseCode = "404", description = "If the project for the provided id not found.")
    @PutMapping("/change/project/active")
    fun changeProjectActive(
        @RequestBody changeProjectActiveStatusRequestDTO: ChangeProjectActiveStatusRequestDTO,
    ) = ResponseEntity.ok(this.projectService.changeProjectActive(changeProjectActiveStatusRequestDTO))

    @Operation(summary = "Endpoint for changing the title of a project")
    @ApiResponse(responseCode = "200", description = "Project title was changed successfully.")
    @ApiResponse(responseCode = "404", description = "If the project for the provided id not found.")
    @PutMapping("/change/project/title")
    fun changeProjectTitle(
        @RequestBody changeProjectTitleRequestDTO: ChangeProjectTitleRequestDTO,
    ) = ResponseEntity.ok(this.projectService.changeProjectTitle(changeProjectTitleRequestDTO))

    @Operation(summary = "Get project of user")
    @ApiResponse(responseCode = "200", description = "Project fetched successfully")
    @ApiResponse(responseCode = "404", description = "User does not have a project assigned")
    @ApiResponse(responseCode = "422", description = "Supplied email is not an email")
    @GetMapping("/fetch/project/{email}")
    fun getUserProject(
        @PathVariable email: String,
    ): ResponseEntity<UserProjectDTO> {
        ValidationUtils.validateEmail(email)
        return ResponseEntity.ok(
            this.projectService.fetchProjectById(
                this.submissionService.getProjectIdOfUser(email),
            ),
        )
    }

    @Operation(summary = "Download project of user")
    @ApiResponse(responseCode = "200", description = "Project downloaded successfully")
    @ApiResponse(responseCode = "404", description = "User project not found")
    @ApiResponse(responseCode = "422", description = "User not found")
    @GetMapping("/download/project/{email}")
    fun downloadUserProject(
        @PathVariable email: String,
    ) {
        // TODO Wait until upload is done
    }

    @Operation(summary = "Endpoint for deleting a project.")
    @ApiResponse(responseCode = "200", description = "Project was deleted successfully")
    @ApiResponse(responseCode = "409", description = "Project won't be deleted as it is still in use")
    @DeleteMapping("/project/{projectId}")
    fun deleteProject(
        @PathVariable projectId: Long,
    ) {
        projectService.deleteProject(projectId)
    }

    @Operation(summary = "Endpoint to send user another invite")
    @ApiResponse(responseCode = "200", description = "Repeated invite successfully.")
    @ApiResponse(responseCode = "404", description = "User does not exist")
    @ApiResponse(responseCode = "409", description = "User is already registered and does not need a reinvite")
    @ApiResponse(responseCode = "422", description = "Email supplied was not an actual email")
    @PostMapping("/resend/invite")
    fun resendInvite(
        @RequestBody inviteRequest: InviteRequestDTO,
    ): ResponseEntity<UserInfoDTO> {
        ValidationUtils.validateEmail(inviteRequest.email)
        return ResponseEntity.ok(userService.handleResendInvite(inviteRequest))
    }

    @Operation(summary = "Fetches the expiration time (dd.mm.yyyy hh:mm) for a invite link for the provided email of a user")
    @ApiResponse(responseCode = "200", description = "Expiration fetched successfully")
    @ApiResponse(responseCode = "404", description = "There is no expiration date for the provided email")
    @GetMapping("invite/expiration/{email}")
    fun fetchExpirationForInvite(
        @PathVariable email: String,
    ): ResponseEntity<String> {
        val expirationDate = inviteTokenExpirationService.fetchExpirationDateForUser(email)
        return ResponseEntity.ok(expirationDate)
    }
}
