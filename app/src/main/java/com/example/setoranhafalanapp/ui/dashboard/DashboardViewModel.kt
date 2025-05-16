package com.example.setoranhafalan.ui.dashboard

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.auth0.jwt.JWT
import com.example.setoranhafalan.data.model.DosenResponse
import com.example.setoranhafalan.data.network.RetrofitClient
import com.example.setoranhafalanapp.data.TokenManager
import com.example.setoranhafalanapp.data.model.SetoranMahasiswaResponse
import com.example.setoranhafalanapp.data.model.SetoranRequest
import com.example.setoranhafalanapp.data.model.SetoranItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.text.SimpleDateFormat
import java.util.*

class DashboardViewModel(private val tokenManager: TokenManager) : ViewModel() {

    private val _dashboardState = MutableStateFlow<DashboardState>(DashboardState.Idle)
    val dashboardState: StateFlow<DashboardState> = _dashboardState
    private val _setoranState = MutableStateFlow<SetoranState>(SetoranState.Idle)
    val setoranState: StateFlow<SetoranState> = _setoranState
    private val _userName = MutableStateFlow<String?>(null)
    val userName: StateFlow<String?> = _userName
    private val TAG = "DashboardViewModel"

    init {
        val idToken = tokenManager.getIdToken()
        if (idToken != null) {
            try {
                val decodedJwt = JWT.decode(idToken)
                val name = decodedJwt.getClaim("name").asString() ?: decodedJwt.getClaim("preferred_username").asString()
                _userName.value = name
                Log.d(TAG, "Nama pengguna dari id_token: $name")
            } catch (e: Exception) {
                Log.e(TAG, "Gagal menguraikan id_token: ${e.message}")
            }
        }
    }

    fun fetchDosenInfo() {
        viewModelScope.launch {
            _dashboardState.value = DashboardState.Loading
            try {
                val token = tokenManager.getAccessToken()
                if (token != null) {
                    Log.d(TAG, "Mengambil data dosen, token: $token")
                    Log.d(TAG, "URL: https://api.tif.uin-suska.ac.id/setoran-dev/v1/dosen/pa-saya")
                    val response = RetrofitClient.apiService.getDosenInfo(
                        token = "Bearer $token"
                    )
                    if (response.isSuccessful) {
                        response.body()?.let { dosen ->
                            Log.d(TAG, "Data dosen berhasil diambil: ${dosen.message}")
                            _dashboardState.value = DashboardState.Success(dosen)
                        } ?: run {
                            Log.e(TAG, "Respons kosong dari server")
                            _dashboardState.value = DashboardState.Error("Respons kosong dari server")
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e(TAG, "Gagal mengambil data dosen, kode: ${response.code()}, pesan: ${response.message()}, body: $errorBody")
                        handleErrorResponse(response.code(), errorBody, response.message(), isDosen = true)
                    }
                } else {
                    Log.e(TAG, "Access token tidak ditemukan")
                    _dashboardState.value = DashboardState.Error("Token tidak ditemukan")
                }
            } catch (e: HttpException) {
                Log.e(TAG, "Pengecualian HTTP (dosen): ${e.code()}, pesan: ${e.message()}")
                _dashboardState.value = DashboardState.Error("Kesalahan HTTP: ${e.message()} (Kode: ${e.code()})")
            } catch (e: Exception) {
                Log.e(TAG, "Pengecualian saat mengambil data dosen: ${e.message}", e)
                _dashboardState.value = DashboardState.Error("Kesalahan jaringan: ${e.message}")
            }
        }
    }

    fun fetchSetoranMahasiswa(nim: String) {
        viewModelScope.launch {
            _setoranState.value = SetoranState.Loading
            try {
                val token = tokenManager.getAccessToken()
                if (token != null) {
                    Log.d(TAG, "Mengambil setoran mahasiswa, NIM: $nim, token: $token")
                    Log.d(TAG, "URL: https://api.tif.uin-suska.ac.id/setoran-dev/v1/mahasiswa/setoran/$nim")
                    val response = RetrofitClient.apiService.getSetoranMahasiswa(
                        token = "Bearer $token",
                        nim = nim
                    )
                    if (response.isSuccessful) {
                        response.body()?.let { setoran ->
                            Log.d(TAG, "Setoran mahasiswa berhasil diambil: ${setoran.message}")
                            _setoranState.value = SetoranState.Success(setoran)
                        } ?: run {
                            Log.e(TAG, "Respons kosong dari server")
                            _setoranState.value = SetoranState.Error("Respons kosong dari server")
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e(TAG, "Gagal mengambil setoran, kode: ${response.code()}, pesan: ${response.message()}, body: $errorBody")
                        handleErrorResponse(response.code(), errorBody, response.message(), isDosen = false)
                    }
                } else {
                    Log.e(TAG, "Access token tidak ditemukan")
                    _setoranState.value = SetoranState.Error("Token tidak ditemukan")
                }
            } catch (e: HttpException) {
                Log.e(TAG, "Pengecualian HTTP (setoran): ${e.code()}, pesan: ${e.message()}")
                _setoranState.value = SetoranState.Error("Kesalahan HTTP: ${e.message()} (Kode: ${e.code()})")
            } catch (e: Exception) {
                Log.e(TAG, "Pengecualian saat mengambil setoran: ${e.message}", e)
                _setoranState.value = SetoranState.Error("Kesalahan jaringan: ${e.message}")
            }
        }
    }

    fun postSetoranMahasiswa(nim: String, idKomponenSetoran: String, namaKomponenSetoran: String) {
        viewModelScope.launch {
            _setoranState.value = SetoranState.Loading
            try {
                val token = tokenManager.getAccessToken()
                if (token != null) {
                    Log.d(TAG, "Menambahkan setoran mahasiswa, NIM: $nim, id_komponen_setoran: $idKomponenSetoran, nama_komponen_setoran: $namaKomponenSetoran")
                    Log.d(TAG, "URL: https://api.tif.uin-suska.ac.id/setoran-dev/v1/mahasiswa/setoran/$nim")
                    val request = SetoranRequest(
                        dataSetoran = listOf(
                            SetoranItem(
                                idKomponenSetoran = idKomponenSetoran,
                                namaKomponenSetoran = namaKomponenSetoran
                            )
                        )
                    )
                    Log.d(TAG, "Request body: $request")
                    val response = RetrofitClient.apiService.postSetoranMahasiswa(
                        token = "Bearer $token",
                        nim = nim,
                        request = request
                    )
                    if (response.isSuccessful) {
                        response.body()?.let { setoran ->
                            Log.d(TAG, "Setoran berhasil ditambahkan: ${setoran.message}")
                            _setoranState.value = SetoranState.Success(null)
                            fetchSetoranMahasiswa(nim)
                        } ?: run {
                            Log.e(TAG, "Respons kosong dari server")
                            _setoranState.value = SetoranState.Error("Respons kosong dari server")
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e(TAG, "Gagal menambahkan setoran, kode: ${response.code()}, pesan: ${response.message()}, body: $errorBody")
                        handleErrorResponse(response.code(), errorBody, response.message(), isDosen = false)
                    }
                } else {
                    Log.e(TAG, "Access token tidak ditemukan")
                    _setoranState.value = SetoranState.Error("Token tidak ditemukan")
                }
            } catch (e: HttpException) {
                Log.e(TAG, "Pengecualian HTTP (post setoran): ${e.code()}, pesan: ${e.message()}")
                _setoranState.value = SetoranState.Error("Kesalahan HTTP: ${e.message()} (Kode: ${e.code()})")
            } catch (e: Exception) {
                Log.e(TAG, "Pengecualian saat menambahkan setoran: ${e.message}", e)
                _setoranState.value = SetoranState.Error("Kesalahan jaringan: ${e.message}")
            }
        }
    }

    fun deleteSetoranMahasiswa(nim: String, idSetoran: String, idKomponenSetoran: String, namaKomponenSetoran: String) {
        viewModelScope.launch {
            _setoranState.value = SetoranState.Loading
            try {
                val token = tokenManager.getAccessToken()
                if (token != null) {
                    Log.d(TAG, "Menghapus setoran mahasiswa, NIM: $nim, id: $idSetoran, id_komponen_setoran: $idKomponenSetoran, nama_komponen_setoran: $namaKomponenSetoran")
                    Log.d(TAG, "URL: https://api.tif.uin-suska.ac.id/setoran-dev/v1/mahasiswa/setoran/$nim?id=$idSetoran")
                    val request = SetoranRequest(
                        dataSetoran = listOf(
                            SetoranItem(
                                id = idSetoran,
                                idKomponenSetoran = idKomponenSetoran,
                                namaKomponenSetoran = namaKomponenSetoran
                            )
                        )
                    )
                    Log.d(TAG, "Request body: $request")
                    val response = RetrofitClient.apiService.deleteSetoranMahasiswa(
                        token = "Bearer $token",
                        nim = nim,
                        id = idSetoran,
                        request = request
                    )
                    if (response.isSuccessful) {
                        response.body()?.let { setoran ->
                            Log.d(TAG, "Setoran berhasil dihapus: ${setoran.message}")
                            _setoranState.value = SetoranState.Success(null)
                            fetchSetoranMahasiswa(nim)
                        } ?: run {
                            Log.e(TAG, "Respons kosong dari server")
                            _setoranState.value = SetoranState.Error("Respons kosong dari server")
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e(TAG, "Gagal menghapus setoran, kode: ${response.code()}, pesan: ${response.message()}, body: $errorBody")
                        handleErrorResponse(response.code(), errorBody, response.message(), isDosen = false)
                    }
                } else {
                    Log.e(TAG, "Access token tidak ditemukan")
                    _setoranState.value = SetoranState.Error("Token tidak ditemukan")
                }
            } catch (e: HttpException) {
                Log.e(TAG, "Pengecualian HTTP (delete setoran): ${e.code()}, pesan: ${e.message()}")
                _setoranState.value = SetoranState.Error("Kesalahan HTTP: ${e.message()} (Kode: ${e.code()})")
            } catch (e: Exception) {
                Log.e(TAG, "Pengecualian saat menghapus setoran: ${e.message}", e)
                _setoranState.value = SetoranState.Error("Kesalahan jaringan: ${e.message}")
            }
        }
    }

    private fun handleErrorResponse(code: Int, errorBody: String?, message: String, isDosen: Boolean) {
        when (code) {
            400 -> {
                Log.e(TAG, "Bad request: $errorBody")
                if (isDosen) {
                    _dashboardState.value = DashboardState.Error("Gagal mengambil data dosen: $errorBody (Kode: 400)")
                } else {
                    _setoranState.value = SetoranState.Error("Gagal mengelola setoran: $errorBody (Kode: 400)")
                }
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
                                if (isDosen) {
                                    fetchDosenInfo()
                                } else {
                                    _setoranState.value = SetoranState.Error("Token diperbarui, coba lagi")
                                }
                            } ?: run {
                                Log.e(TAG, "Respons refresh kosong")
                                if (isDosen) {
                                    _dashboardState.value = DashboardState.Error("Gagal memperbarui token: Respons kosong")
                                } else {
                                    _setoranState.value = SetoranState.Error("Gagal memperbarui token: Respons kosong")
                                }
                            }
                        } else {
                            Log.e(TAG, "Gagal refresh token, kode: ${refreshResponse.code()}, pesan: ${refreshResponse.message()}")
                            if (isDosen) {
                                _dashboardState.value = DashboardState.Error("Gagal memperbarui token: ${refreshResponse.message()} (Kode: ${refreshResponse.code()})")
                            } else {
                                _setoranState.value = SetoranState.Error("Gagal memperbarui token: ${refreshResponse.message()} (Kode: ${refreshResponse.code()})")
                            }
                        }
                    } else {
                        Log.e(TAG, "Refresh token tidak ditemukan")
                        if (isDosen) {
                            _dashboardState.value = DashboardState.Error("Refresh token tidak ditemukan")
                        } else {
                            _setoranState.value = SetoranState.Error("Refresh token tidak ditemukan")
                        }
                    }
                }
            }
            403 -> {
                Log.e(TAG, "Akses ditolak: $errorBody")
                if (isDosen) {
                    _dashboardState.value = DashboardState.Error("Akses ditolak: Tidak diotorisasi (Kode: 403). Periksa scope/role Keycloak.")
                } else {
                    _setoranState.value = SetoranState.Error("Akses ditolak: Tidak diotorisasi (Kode: 403). Periksa scope/role Keycloak.")
                }
            }
            404 -> {
                Log.e(TAG, "Endpoint tidak ditemukan: $message")
                if (isDosen) {
                    _dashboardState.value = DashboardState.Error("Endpoint dosen tidak ditemukan (Kode: 404)")
                } else {
                    _setoranState.value = SetoranState.Error("Endpoint setoran tidak ditemukan (Kode: 404)")
                }
            }
            else -> {
                if (isDosen) {
                    _dashboardState.value = DashboardState.Error("Gagal mengambil data dosen: $message (Kode: $code, Body: $errorBody)")
                } else {
                    _setoranState.value = SetoranState.Error("Gagal mengelola setoran: $message (Kode: $code, Body: $errorBody)")
                }
            }
        }
    }

    companion object {
        fun getFactory(context: Context): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
                        return DashboardViewModel(TokenManager(context)) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }
    }
}

sealed class DashboardState {
    object Idle : DashboardState()
    object Loading : DashboardState()
    data class Success(val data: DosenResponse) : DashboardState()
    data class Error(val message: String) : DashboardState()
}

sealed class SetoranState {
    object Idle : SetoranState()
    object Loading : SetoranState()
    data class Success(val data: SetoranMahasiswaResponse?) : SetoranState()
    data class Error(val message: String) : SetoranState()
}