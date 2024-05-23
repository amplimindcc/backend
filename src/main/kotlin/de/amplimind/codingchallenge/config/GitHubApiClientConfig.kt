package de.amplimind.codingchallenge.config

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import de.amplimind.codingchallenge.submission.GitHubApiClient
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

@Configuration
class GitHubApiClientConfig {
    val accessToken = ""

    @Bean
    fun githubApiClient(): GitHubApiClient {
        val authToken = "Bearer $accessToken"
        val httpClient =
            OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val original = chain.request()
                    val builder =
                        original.newBuilder()
                            .header("Accept", "application/vnd.github.v3+json")
                            .header("Authorization", authToken)
                            .header("X-GitHub-Api-Version", "2022-11-28")
                    val request = builder.build()
                    chain.proceed(request)
                }
                .build()

        val contentType = "application/json".toMediaType()
        val retrofit =
            Retrofit.Builder()
                .baseUrl("https://api.github.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .addConverterFactory(Json { ignoreUnknownKeys = true }.asConverterFactory(contentType))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(httpClient)
                .build()
        return retrofit.create(GitHubApiClient::class.java)
    }
}
