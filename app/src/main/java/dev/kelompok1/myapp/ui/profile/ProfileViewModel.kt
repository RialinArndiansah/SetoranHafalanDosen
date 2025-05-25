package dev.kelompok1.myapp.ui.profile

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.kelompok1.myapp.data.network.RetrofitClient
import dev.kelompok1.myapp.ui.dashboard.DashboardState
import dev.kelompok1.myapp.data.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

class ProfileViewModel(private val tokenManager: TokenManager) : ViewModel() {
    
    private val _profileState = MutableStateFlow<DashboardState>(DashboardState.Idle)
    val profileState: StateFlow<DashboardState> = _profileState
    
    private val _userName = MutableStateFlow<String?>(null)
    val userName: StateFlow<String?> = _userName
    
    private val TAG = "ProfileViewModel"
    
    init {
        extractUserNameFromToken()
    }
    
    private fun extractUserNameFromToken() {
        val idToken = tokenManager.getIdToken()
        if (idToken != null) {
            try {
                val decodedJwt = com.auth0.jwt.JWT.decode(idToken)
                val name = decodedJwt.getClaim("name").asString() ?: 
                           decodedJwt.getClaim("preferred_username").asString()
                _userName.value = name
                Log.d(TAG, "Nama pengguna dari id_token: $name")
            } catch (e: Exception) {
                Log.e(TAG, "Gagal menguraikan id_token: ${e.message}")
            }
        }
    }
    
    fun fetchDosenInfo() {
        viewModelScope.launch {
            _profileState.value = DashboardState.Loading
            try {
                val token = tokenManager.getAccessToken()
                if (token != null) {
                    Log.d(TAG, "Mengambil data dosen, token: $token")
                    val response = RetrofitClient.apiService.getDosenInfo(
                        token = "Bearer $token"
                    )
                    if (response.isSuccessful) {
                        response.body()?.let { dosen ->
                            Log.d(TAG, "Data dosen berhasil diambil: ${dosen.message}")
                            _profileState.value = DashboardState.Success(dosen)
                        } ?: run {
                            Log.e(TAG, "Respons kosong dari server")
                            _profileState.value = DashboardState.Error("Respons kosong dari server")
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e(TAG, "Gagal mengambil data dosen, kode: ${response.code()}, pesan: ${response.message()}, body: $errorBody")
                        handleErrorResponse(response.code(), errorBody, response.message())
                    }
                } else {
                    Log.e(TAG, "Access token tidak ditemukan")
                    _profileState.value = DashboardState.Error("Token tidak ditemukan")
                }
            } catch (e: HttpException) {
                Log.e(TAG, "Pengecualian HTTP (dosen): ${e.code()}, pesan: ${e.message()}")
                _profileState.value = DashboardState.Error("Kesalahan HTTP: ${e.message()} (Kode: ${e.code()})")
            } catch (e: Exception) {
                Log.e(TAG, "Pengecualian saat mengambil data dosen: ${e.message}", e)
                _profileState.value = DashboardState.Error("Kesalahan jaringan: ${e.message}")
            }
        }
    }
    
    private fun handleErrorResponse(code: Int, errorBody: String?, message: String) {
        when (code) {
            400 -> {
                Log.e(TAG, "Bad request: $errorBody")
                _profileState.value = DashboardState.Error("Gagal mengambil data dosen: $errorBody (Kode: 400)")
            }
            401 -> {
                Log.w(TAG, "Token tidak valid, mencoba refresh token")
                viewModelScope.launch {
                    val refreshToken = tokenManager.getRefreshToken()
                    if (refreshToken != null) {
                        val refreshResponse = RetrofitClient.kcApiService.refreshToken(
                            clientId = "setoran-mobile-dev",
                            clientSecret = "aqJp3xnXKudgC7RMOshEQP7ZoVKWzoSl",
                            grantType = "refresh_token",
                            refreshToken = refreshToken
                        )
                        if (refreshResponse.isSuccessful) {
                            refreshResponse.body()?.let { auth ->
                                Log.d(TAG, "Token berhasil diperbarui")
                                tokenManager.saveTokens(auth.access_token, auth.refresh_token, auth.id_token)
                                fetchDosenInfo()
                            }
                        } else {
                            Log.e(TAG, "Gagal refresh token: ${refreshResponse.code()}")
                            _profileState.value = DashboardState.Error("Sesi berakhir, silakan login kembali")
                        }
                    } else {
                        Log.e(TAG, "Refresh token tidak ditemukan")
                        _profileState.value = DashboardState.Error("Sesi berakhir, silakan login kembali")
                    }
                }
            }
            else -> {
                Log.e(TAG, "Error lainnya: $message")
                _profileState.value = DashboardState.Error("Gagal mengambil data dosen: $message (Kode: $code)")
            }
        }
    }
    
    companion object {
        fun getFactory(context: Context): Factory {
            return Factory(TokenManager(context))
        }
    }

    class Factory(private val tokenManager: TokenManager) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProfileViewModel(tokenManager) as T
        }
    }
} 