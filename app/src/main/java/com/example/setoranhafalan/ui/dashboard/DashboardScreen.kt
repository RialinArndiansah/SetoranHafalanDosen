package com.example.setoranhafalan.ui.dashboard

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.setoranhafalan.data.model.DosenResponse
import com.example.setoranhafalanapp.R
import com.example.setoranhafalanapp.ui.login.LoginViewModel
import com.example.setoranhafalanapp.ui.navigation.AppBottomNavigation
import com.example.setoranhafalanapp.ui.navigation.tealPrimary
import com.example.setoranhafalanapp.ui.navigation.tealDark
import com.example.setoranhafalanapp.ui.navigation.tealLight
import com.example.setoranhafalanapp.ui.navigation.tealPastel
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val dashboardViewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.getFactory(context))
    val loginViewModel: LoginViewModel = viewModel(factory = LoginViewModel.getFactory(context))
    val dashboardState by dashboardViewModel.dashboardState.collectAsState()
    val userName by dashboardViewModel.userName.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var profilePhotoUri by remember { mutableStateOf<String?>(loadProfilePhotoPath(context)) }
    val defaultProfilePhoto = R.drawable.user
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val file = saveImageToInternalStorage(context, it)
            profilePhotoUri = file.absolutePath
            saveProfilePhotoPath(context, file.absolutePath)
        }
    }

    LaunchedEffect(Unit) {
        dashboardViewModel.fetchDosenInfo()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Dashboard Dosen",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = tealPrimary,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = {
                        loginViewModel.logout()
                        clearProfilePhoto(context)
                        onLogout()
                    }) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Logout"
                        )
                    }
                }
            )
        },
        bottomBar = {
            AppBottomNavigation(navController)
        },
        containerColor = Color(0xFFF5F7F9)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = padding.calculateBottomPadding() + 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    // New Dashboard Header
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        elevation = CardDefaults.cardElevation(2.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Dashboard",
                                    tint = tealPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Dashboard Dosen",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = tealDark
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                userName?.let {
                                    Text(
                                        text = it,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    when (val state = dashboardState) {
                        is DashboardState.Loading -> {
                            LoadingCard()
                        }
                        is DashboardState.Success -> {
                            val dosen = state.data.data
                            
                            // Define overall variables
                            val totalMahasiswaPa = dosen.info_mahasiswa_pa.daftar_mahasiswa.size
                            val belumSetoran = dosen.info_mahasiswa_pa.daftar_mahasiswa.count { 
                                it.info_setoran.total_sudah_setor == 0 
                            }
                            val sudahSetoran = totalMahasiswaPa - belumSetoran
                            val jumlahAngkatan = dosen.info_mahasiswa_pa.ringkasan.size
                            
                            // Statistics Summary Card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White
                                ),
                                elevation = CardDefaults.cardElevation(2.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = "Ringkasan Statistik",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = tealDark
                                    )
                                    
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            com.example.setoranhafalanapp.ui.components.StatisticBox(
                                                title = "Total Mahasiswa",
                                                value = totalMahasiswaPa.toString(),
                                                color = tealPastel
                                            )
                                            
                                            Spacer(modifier = Modifier.height(12.dp))
                                            
                                            com.example.setoranhafalanapp.ui.components.StatisticBox(
                                                title = "Jumlah Angkatan",
                                                value = jumlahAngkatan.toString(),
                                                color = tealPastel
                                            )
                                        }
                                        
                                        Column {
                                            com.example.setoranhafalanapp.ui.components.StatisticBox(
                                                title = "Sudah Setor",
                                                value = sudahSetoran.toString(),
                                                color = Color(0xFFD1F0E0) // Light green
                                            )
                                            
                                            Spacer(modifier = Modifier.height(12.dp))
                                            
                                            com.example.setoranhafalanapp.ui.components.StatisticBox(
                                                title = "Belum Setor",
                                                value = belumSetoran.toString(),
                                                color = Color(0xFFFBE9E7) // Light red
                                            )
                                        }
                                    }
                                }
                            }
                            
                            // Submission Progress Card
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White
                                ),
                                elevation = CardDefaults.cardElevation(2.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = "Progres Setoran Terbaru",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = tealDark
                                    )
                                    
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    // Group by jumlah setoran
                                    val setoranCounts = dosen.info_mahasiswa_pa.daftar_mahasiswa
                                        .groupBy { 
                                            it.info_setoran.total_sudah_setor 
                                        }
                                        .mapValues { it.value.size }
                                    
                                    // Sorted entries for display
                                    val sortedEntries = setoranCounts.entries.sortedBy { it.key }
                                    
                                    // Display progress
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        sortedEntries.forEach { (sudahSetor, count) ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "$sudahSetor setoran",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = Color.DarkGray
                                                )
                                                
                                                Text(
                                                    text = "$count mahasiswa",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = tealDark
                                                )
                                            }
                                            
                                            // Progress indicator using the existing totalMahasiswaPa 
                                            // that was calculated earlier in this same scope
                                            val progress = if (totalMahasiswaPa > 0) {
                                                count.toFloat() / totalMahasiswaPa.toFloat()
                                            } else {
                                                0f
                                            }
                                            LinearProgressIndicator(
                                                progress = progress,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(8.dp)
                                                    .clip(RoundedCornerShape(4.dp)),
                                                color = tealPrimary,
                                                trackColor = Color(0xFFE0F2F1)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        is DashboardState.Error -> {
                            LaunchedEffect(state) {
                                scope.launch {
                                    snackbarHostState.showSnackbar(state.message)
                                }
                            }
                            ErrorCard { dashboardViewModel.fetchDosenInfo() }
                        }
                        else -> {
                            EmptyCard { dashboardViewModel.fetchDosenInfo() }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LoadingCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = tealPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Memuat data dosen...",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun ErrorCard(onRetry: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Gagal memuat data dosen",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFEF4444)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = tealPrimary
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Coba Lagi")
            }
        }
    }
}

@Composable
fun EmptyCard(onLoad: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onLoad,
                colors = ButtonDefaults.buttonColors(
                    containerColor = tealPrimary
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Muat Data Dosen")
            }
        }
    }
}

// New StatisticItem component
@Composable
fun StatisticItem(
    label: String,
    value: String,
    color: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.DarkGray
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }
}

private fun saveImageToInternalStorage(context: Context, uri: Uri): File {
    val inputStream = context.contentResolver.openInputStream(uri)
    val file = File(context.filesDir, "profile_photo.jpg")
    FileOutputStream(file).use { outputStream ->
        inputStream?.copyTo(outputStream)
    }
    inputStream?.close()
    return file
}

private fun saveProfilePhotoPath(context: Context, path: String) {
    val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    sharedPreferences.edit().putString("profile_photo_path", path).apply()
}

private fun loadProfilePhotoPath(context: Context): String? {
    val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    return sharedPreferences.getString("profile_photo_path", null)
}

private fun clearProfilePhoto(context: Context) {
    val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    sharedPreferences.edit().remove("profile_photo_path").apply()
    val file = File(context.filesDir, "profile_photo.jpg")
    file.delete()
} 