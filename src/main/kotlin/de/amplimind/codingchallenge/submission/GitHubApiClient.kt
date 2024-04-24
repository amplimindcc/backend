package de.amplimind.codingchallenge.submission

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface GitHubApiClient {
    @PUT("repos/amplimindcc/{repoName}/contents/{filePath}")
    suspend fun pushFileCall(
        @Path("repoName") repoName: String,
        @Path("filePath") filePath: String,
        @Body submissionFile: SubmissionFile
    ): ResponseBody

    @POST("orgs/{org}/repos")
    suspend fun createSubmissionRepository(
        @Path("org") org: String,
        @Body submissionRepository: SubmissionGitHubRepository
    ): ResponseBody

    @POST("repos/amplimindcc/{repoName}/actions/workflows/{workflowName}/dispatches")
    suspend fun triggerWorkflow(
        @Path("repoName") repoName: String,
        @Path("workflowName") workflowName: String,
        @Body workflowDispatch: WorkflowDispatch
    ): Response<String>

    @GET("repos/amplimindcc/{repoName}")
    fun getSubmissionRepository(
        @Path("repoName") repoName: String
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
        .addConverterFactory(Json { ignoreUnknownKeys = true }.asConverterFactory(contentType))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .client(httpClient)
        .build()
    return retrofit.create(GitHubApiClient::class.java)
}
