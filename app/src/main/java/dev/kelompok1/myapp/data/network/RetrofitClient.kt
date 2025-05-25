package dev.kelompok1.myapp.data.network

import dev.kelompok1.myapp.data.network.ApiService
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
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

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
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