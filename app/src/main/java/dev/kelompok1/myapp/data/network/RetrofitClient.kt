package dev.kelompok1.myapp.data.network

import dev.kelompok1.myapp.data.TokenManager
import dev.kelompok1.myapp.data.network.ApiService
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = "https://api.tif.uin-suska.ac.id/setoran-dev/v1/"
    private const val KC_URL = "https://id.tif.uin-suska.ac.id"
    
    // Increased timeouts for better reliability
    private const val CONNECT_TIMEOUT_SECONDS = 15L
    private const val READ_TIMEOUT_SECONDS = 15L
    private const val WRITE_TIMEOUT_SECONDS = 15L
    
    // Maximum number of retries for failed requests
    private const val MAX_RETRIES = 3

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    // Custom interceptor to handle timeouts and other network errors
    private class NetworkErrorInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            var tryCount = 0
            var response: Response? = null
            var exception: IOException? = null
            
            while (tryCount < MAX_RETRIES && (response == null || !response.isSuccessful)) {
                try {
                    if (response != null) {
                        response.close() // Close previous response
                    }
                    
                    // Add increasing backoff delay for retries
                    if (tryCount > 0) {
                        val backoffDelay = (1000L * (tryCount * tryCount)).coerceAtMost(5000L)
                        Thread.sleep(backoffDelay)
                    }
                    
                    response = chain.proceed(request)
                    
                    // If we get server errors (5xx), retry
                    if (response.code in 500..599) {
                        response.close()
                        tryCount++
                        continue
                    }
                    
                    return response
                } catch (e: SocketTimeoutException) {
                    exception = e
                    tryCount++
                } catch (e: IOException) {
                    exception = e
                    tryCount++
                }
            }
            
            // If all retries failed, throw the last exception or return last response
            return response ?: throw exception ?: IOException("Unknown network error")
        }
    }

    private var tokenManager: TokenManager? = null
    
    fun initialize(tokenManager: TokenManager) {
        this.tokenManager = tokenManager
    }
    
    private val tokenAuthenticator = object : Authenticator {
        override fun authenticate(route: Route?, response: Response): Request? {
            val localTokenManager = tokenManager ?: return null
            
            // Check if we should attempt to refresh
            if (localTokenManager.isRefreshTokenExpired() || localTokenManager.isUserInactive()) {
                return null // Let the UserActivityTracker handle session expiration
            }
            
            // Get the refresh token
            val refreshToken = localTokenManager.getRefreshToken() ?: return null
            
            // Try to get a new access token
            return runBlocking {
                try {
                    val tokenResponse = kcApiService.refreshToken(
                        clientId = "setoran-mobile-dev",
                        clientSecret = "aqJp3xnXKudgC7RMOshEQP7ZoVKWzoSl",
                        grantType = "refresh_token",
                        refreshToken = refreshToken
                    )
                    
                    if (tokenResponse.isSuccessful && tokenResponse.body() != null) {
                        val newAccessToken = tokenResponse.body()?.access_token
                        val newRefreshToken = tokenResponse.body()?.refresh_token
                        val newIdToken = tokenResponse.body()?.id_token
                        
                        if (newAccessToken != null && newRefreshToken != null && newIdToken != null) {
                            // Save the new tokens
                            localTokenManager.saveTokens(newAccessToken, newRefreshToken, newIdToken)
                            
                            // Update the request with the new access token
                            response.request.newBuilder()
                                .header("Authorization", "Bearer $newAccessToken")
                                .build()
                        } else {
                            null
                        }
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    null
                }
            }
        }
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .authenticator(tokenAuthenticator)
        .addInterceptor(NetworkErrorInterceptor())
        .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    val kcApiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(KC_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}