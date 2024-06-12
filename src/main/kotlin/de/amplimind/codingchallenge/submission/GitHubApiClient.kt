package de.amplimind.codingchallenge.submission

import kotlinx.serialization.Serializable
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface GitHubApiClient {
    @PUT("repos/amplimindcc/{repoName}/contents/{filePath}")
    suspend fun pushFileCall(
        @Path("repoName") repoName: String,
        @Path("filePath") filePath: String,
        @Body submissionFile: SubmissionFile,
    ): Response<PushFileResponse>

    @POST("orgs/{org}/repos")
    suspend fun createSubmissionRepository(
        @Path("org") org: String,
        @Body submissionRepository: SubmissionGitHubRepository,
    ): Response<CreateRepoResponse>

    @POST("repos/amplimindcc/{repoName}/actions/workflows/{workflowName}/dispatches")
    suspend fun triggerWorkflow(
        @Path("repoName") repoName: String,
        @Path("workflowName") workflowName: String,
        @Body workflowDispatch: WorkflowDispatch,
    ): Response<Void>

    @GET("repos/amplimindcc/{repoName}")
    fun getSubmissionRepository(
        @Path("repoName") repoName: String,
    ): Call<Result<String>>

    @DELETE("repos/amplimindcc/{repoName}")
    suspend fun deleteRepository(
        @Path("repoName") repoName: String,
    ): Response<Void>

    @GET("repos/amplimindcc/{repoName}/actions/artifacts")
    suspend fun getArtifacts(
        @Path("repoName") repoName: String,
    ): ArtifactsResponse

    @GET("repos/amplimindcc/{repoName}/actions/artifacts/{artifactId}/zip")
    suspend fun downloadArtifact(
        @Path("repoName") repoName: String,
        @Path("artifactId") artifactId: Int,
    ): Response<ResponseBody>

    @POST("/repos/amplimindcc/{repo}/git/blobs")
    suspend fun createBlob(
        @Path("repo") repo: String,
        @Body blob: Blob,
    ): Response<CreateBlobResponse>

    @POST("/repos/amplimindcc/{repo}/git/trees")
    suspend fun createTree(
        @Path("repo") repo: String,
        @Body tree: CreateTreeRequest,
    ): Response<CreateTreeResponse>

    @POST("/repos/amplimindcc/{repo}/git/commits")
    suspend fun createCommit(
        @Path("repo") repo: String,
        @Body commit: CreateCommitRequest,
    ): Response<CreateCommitResponse>

    @POST("/repos/amplimindcc/{repo}/git/refs/heads/{branch}")
    suspend fun updateBranchReference(
        @Path("repo") repo: String,
        @Path("branch") branch: String,
        @Body reference: UpdateBranchReferenceRequest,
    ): Response<UpdateBranchReferenceResponse>

    @GET("/repos/amplimindcc/{repo}/git/trees/{branch}")
    suspend fun getGitTree(
        @Path("repo") repo: String,
        @Path("branch") branch: String,
    ): Response<GetGitTreeResponse>
}

@Serializable
data class Blob(
    val content: String,
)

@Serializable
data class CreateBlobResponse(
    val sha: String,
    val url: String,
)

@Serializable
data class TreeItem(
    val path: String,
    val mode: String,
    val type: String,
    val sha: String? = null,
)

@Serializable
data class CreateTreeRequest(
    val tree: List<TreeItem>,
    val base_tree: String,
)

@Serializable
data class GetGitTreeResponse(
    val sha: String,
)

@Serializable
data class CreateTreeResponse(
    val sha: String,
)

@Serializable
data class CreateCommitResponse(
    val sha: String,
)

@Serializable
data class CreateCommitRequest(
    val message: String,
    val tree: String,
    val author: Committer,
)

@Serializable
data class UpdateBranchReferenceRequest(
    val sha: String,
    val force: Boolean = true,
)

@Serializable
data class UpdateBranchReferenceResponse(
    val ref: String,
    val url: String,
)

@Serializable
data class Artifact(
    val id: Int,
    val name: String,
    val url: String,
    val archive_download_url: String,
)

@Serializable
data class ArtifactsResponse(
    val total_count: Int,
    val artifacts: List<Artifact>,
)

@Serializable
data class SubmissionFile(
    val message: String,
    val content: String,
    val committer: Committer,
)

@Serializable
data class Committer(
    val name: String,
    val email: String,
)

@Serializable
data class SubmissionGitHubRepository(
    val name: String,
    val description: String,
)

@Serializable
data class WorkflowDispatch(
    val ref: String,
)

@Serializable
data class PushFileResponse(
    val name: String,
)

@Serializable
data class CreateRepoResponse(
    val id: Int,
    val name: String,
)
