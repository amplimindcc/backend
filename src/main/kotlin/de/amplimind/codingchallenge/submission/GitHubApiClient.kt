package de.amplimind.codingchallenge.submission

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.POST

interface GitHubApiClient {
    @PUT("repos/amplimindcc/{owner}/contents/{filePath}")
    fun pushFileCall(
        @Path("owner") owner: String,
        @Path("filePath") filePath: String,
        @Body submissionFile: SubmissionFile
    ): Call<Result<String>>

    @POST("orgs/{org}/repos")
    fun createSubmissionRepository(
        @Path("org") org: String,
        @Body submissionRepository: SubmissionGitHubRepository
    ): Call<Result<String>>

    @POST("repos/amplimindcc/{owner}/actions/workflows/{workflowName}/dispatches")
    fun triggerWorkflow(
        @Path("owner") owner: String,
        @Path("workflowName") workflowName: String,
        @Body workflowDispatch: WorkflowDispatch
    ): Call<Result<String>>
}

@Serializable
data class SubmissionFile(
    val message: String,
    val content: String
)

@Serializable
data class SubmissionGitHubRepository (
    val name: String,
    val description: String
)

@Serializable
data class WorkflowDispatch (
    val ref: String
)

fun createGitHubApiClient(accessToken: String): GitHubApiClient {
    val authToken = "Bearer $accessToken"
    val httpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val original = chain.request()
            val builder = original.newBuilder()
                .header("Accept", "application/vnd.github.v3+json")
                .header("Authorization", authToken)
                .header("X-GitHub-Api-Version", "2022-11-28")
            val request = builder.build()
            chain.proceed(request)
        }
        .build()

    val contentType = "application/json".toMediaType()
    val retrofit = Retrofit.Builder()
        .baseUrl("https://api.github.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(httpClient)
        .build()
    return retrofit.create(GitHubApiClient::class.java)
}
