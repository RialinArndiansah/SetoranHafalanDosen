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
                    containerColor = Color(0xFF3B82F6),
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
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFC280CC),
                            Color(0xFFAE7CE7),
                            Color(0xFF72A5BE)
                        )
                    )
                )
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = padding.calculateBottomPadding() + 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    WelcomeHeader(userName, profilePhotoUri, defaultProfilePhoto) {
                        launcher.launch("image/*")
                    }
                }

                item {
                    when (val state = dashboardState) {
                        is DashboardState.Loading -> {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(48.dp),
                                    color = Color(0xFF3B82F6)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Memuat data dosen...",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color(0xFF1F2937)
                                )
                            }
                        }
                        is DashboardState.Success -> {
                            DosenInfo(state.data)
                        }
                        is DashboardState.Error -> {
                            LaunchedEffect(state) {
                                scope.launch {
                                    snackbarHostState.showSnackbar(state.message)
                                }
                            }
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Gagal memuat data dosen",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color(0xFFEF4444)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { dashboardViewModel.fetchDosenInfo() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF3B82F6)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Coba Lagi")
                                }
                            }
                        }
                        else -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Button(
                                    onClick = { dashboardViewModel.fetchDosenInfo() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF3B82F6)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Muat Data Dosen")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WelcomeHeader(
    userName: String?,
    profilePhotoUri: String?,
    defaultProfilePhoto: Int,
    onPhotoClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color(0xFF3B82F6), CircleShape)
                    .clickable { onPhotoClick() }
                    .background(Color(0xFFF3F4F6), CircleShape)
            ) {
                if (profilePhotoUri != null && File(profilePhotoUri).exists()) {
                    val bitmap = BitmapFactory.decodeFile(profilePhotoUri)
                    bitmap?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = "Foto Profil",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } ?: Image(
                        painter = painterResource(defaultProfilePhoto),
                        contentDescription = "Foto profil default",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(defaultProfilePhoto),
                        contentDescription = "Foto profil default",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Selamat datang,",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF6B7280)
                )
                userName?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1F2937)
                    )
                }
            }
        }
    }
}

@Composable
fun DosenInfo(dosenResponse: DosenResponse) {
    val dosen = dosenResponse.data
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(4.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Profil Dosen",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1F2937)
                )
                Divider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = Color(0xFFE5E7EB)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Dosen",
                        tint = Color(0xFF3B82F6)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = dosen.nama,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1F2937)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                ProfileItem(label = "NIP", value = dosen.nip)
                ProfileItem(label = "Email", value = dosen.email)
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(4.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Ringkasan Mahasiswa per Angkatan",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1F2937)
                )
                Divider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = Color(0xFFE5E7EB)
                )
                // Header Tabel
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Tahun",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1F2937)
                    )
                    Text(
                        text = "Jumlah Mahasiswa",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1F2937)
                    )
                }
                // Baris Data
                dosen.info_mahasiswa_pa.ringkasan.forEach { ringkasan ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = ringkasan.tahun,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF1F2937)
                        )
                        Text(
                            text = ringkasan.total.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF3B82F6)
                        )
                    }
                    Divider(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        color = Color(0xFFE5E7EB)
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF6B7280)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF1F2937)
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