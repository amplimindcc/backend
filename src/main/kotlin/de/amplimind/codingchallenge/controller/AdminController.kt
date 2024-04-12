package de.amplimind.codingchallenge.controller

import de.amplimind.codingchallenge.config.SecurityConfig
import de.amplimind.codingchallenge.dto.UserInfoDTO
import de.amplimind.codingchallenge.dto.request.ChangeUserRoleRequestDTO
import de.amplimind.codingchallenge.dto.request.CreateProjectRequestDTO
import de.amplimind.codingchallenge.service.EmailService
import de.amplimind.codingchallenge.service.ProjectService
import de.amplimind.codingchallenge.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Controller for admin related tasks.
 */
@RestController
@RequestMapping(SecurityConfig.ADMIN_PATH)
class AdminController(
    private val projectService: ProjectService,
    private val userService: UserService,
    private val emailService: EmailService,
) {
    @Operation(summary = "Endpoint for adding a new project.")
    @ApiResponse(responseCode = "200", description = "Project was added successfully.")
    @PostMapping("/project/add")
    fun addProject(
        @RequestBody createProjectRequest: CreateProjectRequestDTO,
    ) = this.projectService.addProject(createProjectRequest)

    @Operation(summary = "Endpoint for fetching all infos for the users")
    @ApiResponse(responseCode = "200", description = "All user infos were fetched successfully.")
    @GetMapping("fetch/users/all")
    fun fetchAllUsers(): ResponseEntity<List<UserInfoDTO>> {
        return ResponseEntity.ok(this.userService.fetchAllUserInfos())
    }

    @Operation(summary = "Endpoint for fetching the info for a user by email")
    @ApiResponse(responseCode = "200", description = "User info was fetched successfully.")
    @ApiResponse(responseCode = "404", description = "User with email was not found.")
    @GetMapping("fetch/projects/{email}")
    fun fetchUserInfosForEmail(
        @PathVariable email: String,
    ): ResponseEntity<UserInfoDTO> {
        val userInfo = this.userService.fetchUserInfosForEmail(email)
        return ResponseEntity.ok(userInfo)
    }

    @Operation(summary = "Endpoint for changing the role of a user")
    @ApiResponse(responseCode = "200", description = "User role was changed successfully.")
    @ApiResponse(responseCode = "400", description = "If the new role which should be set is INIT.")
    @ApiResponse(responseCode = "404", description = "User with email was not found.")
    @PutMapping("change/role")
    fun changeRole(
        @RequestBody changeUserRoleRequest: ChangeUserRoleRequestDTO,
    ): ResponseEntity<UserInfoDTO> {
        return ResponseEntity.ok(this.userService.changeUserRole(changeUserRoleRequest))
    }


    @Operation(summary = "Endpoint for fetching the info for a user by email")
    @ApiResponse(responseCode = "200", description = "User info was fetched successfully.")
    @ApiResponse(responseCode = "404", description = "User with email was not found.")
    @GetMapping("invite/{email}")
    fun createInvite(
        @PathVariable email: String,
    ) {
        this.emailService.sendEmail(email)
        this.userService.createUser(email)
    }
}
