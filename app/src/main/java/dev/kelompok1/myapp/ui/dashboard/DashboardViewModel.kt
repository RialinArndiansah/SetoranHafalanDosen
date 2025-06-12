package dev.kelompok1.myapp.ui.dashboard

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.auth0.jwt.JWT
import dev.kelompok1.myapp.data.model.DosenResponse
import dev.kelompok1.myapp.data.network.RetrofitClient
import dev.kelompok1.myapp.data.TokenManager
import dev.kelompok1.myapp.data.model.SetoranMahasiswaResponse
import dev.kelompok1.myapp.data.model.SetoranRequest
import dev.kelompok1.myapp.data.model.SetoranItem
import dev.kelompok1.myapp.data.model.RecentSetoran
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.delay
import dev.kelompok1.myapp.ui.components.formatDateToWIB
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

// Define a sealed class for recent setoran state
sealed class RecentSetoranState {
    object Loading : RecentSetoranState()
    data class Success(val data: Map<String, List<RecentSetoran>>) : RecentSetoranState()
    data class Error(val message: String) : RecentSetoranState()
}

class DashboardViewModel(private val tokenManager: TokenManager) : ViewModel() {

    private val _dashboardState = MutableStateFlow<DashboardState>(DashboardState.Idle)
    val dashboardState: StateFlow<DashboardState> = _dashboardState
    private val _setoranState = MutableStateFlow<SetoranState>(SetoranState.Idle)
    val setoranState: StateFlow<SetoranState> = _setoranState
    private val _userName = MutableStateFlow<String?>(null)
    val userName: StateFlow<String?> = _userName
    private val _profilePhotoUri = MutableStateFlow<Uri?>(null)
    val profilePhotoUri: StateFlow<Uri?> = _profilePhotoUri
    private val TAG = "DashboardViewModel"
    
    // Recent setoran tracking
    private val _recentSetoranByAngkatan = MutableStateFlow<RecentSetoranState>(RecentSetoranState.Loading)
    val recentSetoranByAngkatan: StateFlow<RecentSetoranState> = _recentSetoranByAngkatan

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

    fun updateProfilePhoto(uri: Uri?) {
        _profilePhotoUri.value = uri
    }

    fun fetchDosenInfo() {
        viewModelScope.launch {
            _dashboardState.value = DashboardState.Loading
            try {
                val token = tokenManager.getAccessToken()
                if (token != null) {
                    Log.d(TAG, "Mengambil data dosen, token: $token")
                    Log.d(TAG, "URL: https://api.tif.uin-suska.ac.id/setoran-dev/v1/dosen/pa-saya")
                    
                    fetchDosenInfoWithRetry(token, maxRetries = 3)
                } else {
                    Log.e(TAG, "Access token tidak ditemukan")
                    _dashboardState.value = DashboardState.Error("Token tidak ditemukan")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Pengecualian saat mengambil data dosen: ${e.message}", e)
                val errorMessage = when (e) {
                    is java.net.SocketTimeoutException -> "Timeout koneksi, silakan coba lagi"
                    is java.net.UnknownHostException -> "Tidak dapat terhubung ke server, periksa koneksi internet Anda"
                    is retrofit2.HttpException -> "Kesalahan HTTP: ${e.message()} (Kode: ${e.code()})"
                    is java.io.IOException -> "Kesalahan jaringan: ${e.message}"
                    else -> "Kesalahan tidak diketahui: ${e.message}"
                }
                _dashboardState.value = DashboardState.Error(errorMessage)
            }
        }
    }
    
    private suspend fun fetchDosenInfoWithRetry(token: String, maxRetries: Int) {
        var currentTry = 0
        var lastException: Exception? = null
        
        while (currentTry < maxRetries) {
            try {
                // If it's a retry, add delay with exponential backoff
                if (currentTry > 0) {
                    val backoffDelay = (1000L * (currentTry * currentTry)).coerceAtMost(5000L)
                    delay(backoffDelay)
                    _dashboardState.value = DashboardState.Loading
                }
                
                val response = RetrofitClient.apiService.getDosenInfo(
                    token = "Bearer $token"
                )
                
                if (response.isSuccessful) {
                    response.body()?.let { dosen ->
                        Log.d(TAG, "Data dosen berhasil diambil: ${dosen.message}")
                        _dashboardState.value = DashboardState.Success(dosen)
                        
                        // Process recent setoran data from student logs if available
                        initializeRecentSetoranData(dosen)
                        
                        return // Success, exit the retry loop
                    } ?: run {
                        Log.e(TAG, "Respons kosong dari server")
                        _dashboardState.value = DashboardState.Error("Respons kosong dari server")
                        return // Empty response, no need to retry
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "Gagal mengambil data dosen, kode: ${response.code()}, pesan: ${response.message()}, body: $errorBody")
                    
                    // For 4xx errors except 401, don't retry
                    if (response.code() in 400..499 && response.code() != 401) {
                        handleErrorResponse(response.code(), errorBody, response.message(), isDosen = true)
                        return
                    }
                    
                    // For 401, try token refresh once
                    if (response.code() == 401) {
                        val refreshSuccessful = refreshTokenAndRetry(isDosen = true)
                        if (refreshSuccessful) return // Token refresh was successful, fetchDosenInfo will be called again
                    }
                    
                    // For all other errors, increment retry counter
                    currentTry++
                }
            } catch (e: Exception) {
                lastException = e
                Log.e(TAG, "Pengecualian saat mencoba mengambil data dosen (coba ke-${currentTry+1}): ${e.message}", e)
                currentTry++
            }
        }
        
        // If we reach here, all retries failed
        val errorMessage = when (lastException) {
            is java.net.SocketTimeoutException -> "Timeout koneksi setelah beberapa percobaan"
            is java.net.UnknownHostException -> "Tidak dapat terhubung ke server setelah beberapa percobaan"
            is retrofit2.HttpException -> "Kesalahan HTTP: ${(lastException as retrofit2.HttpException).message()}"
            is java.io.IOException -> "Kesalahan jaringan: ${lastException.message}"
            else -> lastException?.message ?: "Kesalahan tidak diketahui setelah beberapa percobaan"
        }
        _dashboardState.value = DashboardState.Error(errorMessage)
    }
    
    private suspend fun refreshTokenAndRetry(isDosen: Boolean): Boolean {
        Log.w(TAG, "Token tidak valid, mencoba refresh token")
        val refreshToken = tokenManager.getRefreshToken()
        if (refreshToken != null) {
            try {
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
                        return true
                    }
                }
                // Refresh token failed
                if (isDosen) {
                    _dashboardState.value = DashboardState.Error("Sesi berakhir, silakan login kembali")
                } else {
                    _setoranState.value = SetoranState.Error("Sesi berakhir, silakan login kembali")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error saat refresh token: ${e.message}", e)
                if (isDosen) {
                    _dashboardState.value = DashboardState.Error("Gagal memperbarui sesi: ${e.message}")
                } else {
                    _setoranState.value = SetoranState.Error("Gagal memperbarui sesi: ${e.message}")
                }
            }
        } else {
            if (isDosen) {
                _dashboardState.value = DashboardState.Error("Sesi tidak valid, silakan login kembali")
            } else {
                _setoranState.value = SetoranState.Error("Sesi tidak valid, silakan login kembali")
            }
        }
        return false
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

    fun postSetoranMahasiswa(nim: String, idKomponenSetoran: String, namaKomponenSetoran: String, tanggal: String = "") {
        viewModelScope.launch {
            _setoranState.value = SetoranState.Loading
            try {
                val token = tokenManager.getAccessToken()
                if (token != null) {
                    Log.d(TAG, "Menambahkan setoran mahasiswa, NIM: $nim, id_komponen_setoran: $idKomponenSetoran, nama_komponen_setoran: $namaKomponenSetoran, tanggal: $tanggal")
                    Log.d(TAG, "URL: https://api.tif.uin-suska.ac.id/setoran-dev/v1/mahasiswa/setoran/$nim")
                    val request = SetoranRequest(
                        dataSetoran = listOf(
                            SetoranItem(
                                idKomponenSetoran = idKomponenSetoran,
                                namaKomponenSetoran = namaKomponenSetoran
                            )
                        ),
                        tglSetoran = if (tanggal.isNotBlank()) tanggal else null
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
                            
                            // Find student info to add to recent setoran
                            when (val state = _dashboardState.value) {
                                is DashboardState.Success -> {
                                    val mahasiswa = state.data.data.info_mahasiswa_pa.daftar_mahasiswa.find { it.nim == nim }
                                    mahasiswa?.let {
                                        addToRecentSetoran(
                                            nim = nim,
                                            nama = it.nama,
                                            angkatan = it.angkatan,
                                            komponenSetoran = namaKomponenSetoran,
                                            tanggalSetoran = tanggal.ifBlank { 
                                                // Use current date if no date provided
                                                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) 
                                            },
                                            formattedDate = formatDateToWIB(tanggal.ifBlank { 
                                                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) 
                                            }),
                                            id = setoran.data?.info_setoran?.id ?: ""
                                        )
                                    }
                                }
                                else -> {}
                            }
                            
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
            } catch (e: Exception) {
                Log.e(TAG, "Pengecualian saat menambahkan setoran: ${e.message}", e)
                _setoranState.value = SetoranState.Error("Kesalahan jaringan: ${e.message}")
            }
        }
    }

    // Function to add new setoran to recent list, maintaining only 5 most recent per angkatan
    private fun addToRecentSetoran(
        nim: String,
        nama: String,
        angkatan: String,
        komponenSetoran: String,
        tanggalSetoran: String,
        formattedDate: String,
        id: String = ""
    ) {
        val newSetoran = RecentSetoran(
            id = id,
            nim = nim,
            nama = nama,
            angkatan = angkatan,
            komponenSetoran = komponenSetoran,
            tanggalSetoran = tanggalSetoran,
            formattedDate = formattedDate
        )
        
        // Get current map of recent setoran by angkatan
        val currentState = _recentSetoranByAngkatan.value
        val currentMap = when (currentState) {
            is RecentSetoranState.Success -> currentState.data.toMutableMap()
            else -> mutableMapOf()
        }
        
        // Get or create list for this angkatan
        val angkatanList = currentMap[angkatan]?.toMutableList() ?: mutableListOf()
        
        // Add new setoran to list
        angkatanList.add(0, newSetoran) // Add at beginning (most recent)
        
        // Keep only the 5 most recent
        val trimmedList = if (angkatanList.size > 5) angkatanList.take(5) else angkatanList
        
        // Update map
        currentMap[angkatan] = trimmedList
        
        // Update state
        _recentSetoranByAngkatan.value = RecentSetoranState.Success(currentMap)
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
                            
                            // Update the recent setoran list to remove this setoran if it exists
                            updateRecentSetoranAfterDelete(nim, idSetoran)
                            
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

    private fun initializeRecentSetoranData(dosen: DosenResponse) {
        viewModelScope.launch {
            try {
                _recentSetoranByAngkatan.value = RecentSetoranState.Loading
                
                val recentSetoran = mutableMapOf<String, MutableList<RecentSetoran>>()
                val studentsWithSubmissions = dosen.data.info_mahasiswa_pa.daftar_mahasiswa
                    .filter { it.info_setoran.total_sudah_setor > 0 }
                
                // Process in parallel for faster loading
                val deferredResults = studentsWithSubmissions.map { mahasiswa ->
                    async {
                        fetchAndProcessStudentSetoran(mahasiswa.nim, mahasiswa.nama, mahasiswa.angkatan)
                    }
                }
                
                // Wait for all to complete
                val results = deferredResults.awaitAll()
                
                // Combine all results
                results.forEach { result ->
                    result.forEach { (angkatan, setoranList) ->
                        val existingList = recentSetoran.getOrPut(angkatan) { mutableListOf() }
                        existingList.addAll(setoranList)
                    }
                }
                
                // Sort each angkatan's list by date (newest first) and limit to 5 entries
                val finalMap = recentSetoran.mapValues { (_, list) ->
                    list.sortedByDescending { it.tanggalSetoran }.take(5)
                }
                
                // Update state
                _recentSetoranByAngkatan.value = RecentSetoranState.Success(finalMap)
                
                Log.d(TAG, "Recent setoran data initialized with ${finalMap.size} angkatan entries")
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing recent setoran data: ${e.message}", e)
                _recentSetoranByAngkatan.value = RecentSetoranState.Error("Gagal memuat data setoran: ${e.message}")
            }
        }
    }
    
    private suspend fun fetchAndProcessStudentSetoran(nim: String, nama: String, angkatan: String): Map<String, List<RecentSetoran>> {
        val result = mutableMapOf<String, MutableList<RecentSetoran>>()
        try {
            val token = tokenManager.getAccessToken() ?: return emptyMap()
            
            val response = RetrofitClient.apiService.getSetoranMahasiswa(
                token = "Bearer $token",
                nim = nim
            )
            
            if (response.isSuccessful) {
                response.body()?.let { setoranResponse ->
                    val setoranDetails = setoranResponse.data.setoran.detail
                    
                    // Process all completed setoran entries
                    setoranDetails.filter { it.sudah_setor && it.info_setoran != null }
                        .forEach { detail ->
                            detail.info_setoran?.let { info ->
                                // Format date for display
                                val formattedDate = try {
                                    formatDateToWIB(info.tgl_setoran)
                                } catch (e: Exception) {
                                    info.tgl_setoran // Fallback to original format if formatting fails
                                }
                                
                                // Create RecentSetoran object
                                val setoranEntry = RecentSetoran(
                                    id = info.id,
                                    nim = nim,
                                    nama = nama,
                                    angkatan = angkatan,
                                    komponenSetoran = detail.nama,
                                    tanggalSetoran = info.tgl_setoran,
                                    formattedDate = formattedDate
                                )
                                
                                // Add to map by angkatan
                                val angkatanList = result.getOrPut(angkatan) { mutableListOf() }
                                angkatanList.add(setoranEntry)
                            }
                        }
                }
            } else {
                Log.e(TAG, "Failed to fetch setoran details for student $nim, code: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching setoran details for student $nim: ${e.message}", e)
        }
        return result
    }

    private fun updateRecentSetoranAfterDelete(nim: String, idSetoran: String) {
        viewModelScope.launch {
            try {
                val currentState = _recentSetoranByAngkatan.value
                if (currentState is RecentSetoranState.Success) {
                    val currentMap = currentState.data.toMutableMap()
                    
                    // Find the setoran to remove
                    val setoranToRemove = currentMap.values.flatten().find { it.id == idSetoran }
                    
                    if (setoranToRemove != null) {
                        // Get the angkatan of the setoran to remove
                        val angkatan = setoranToRemove.angkatan
                        
                        // Get the list of recent setoran for this angkatan
                        val angkatanList = currentMap[angkatan]?.toMutableList() ?: mutableListOf()
                        
                        // Remove the setoran from the list
                        angkatanList.remove(setoranToRemove)
                        
                        // Update the map
                        currentMap[angkatan] = angkatanList
                        
                        // Update state
                        _recentSetoranByAngkatan.value = RecentSetoranState.Success(currentMap)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating recent setoran list after delete: ${e.message}", e)
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