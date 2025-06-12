package dev.kelompok1.myapp.ui.profile

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dev.kelompok1.myapp.ui.components.ProfileItem
import dev.kelompok1.myapp.ui.components.StyledSectionCard
import dev.kelompok1.myapp.ui.navigation.AppBottomNavigation
import dev.kelompok1.myapp.ui.navigation.tealPrimary
import dev.kelompok1.myapp.ui.navigation.tealDark
import dev.kelompok1.myapp.ui.navigation.tealLight
import dev.kelompok1.myapp.ui.navigation.tealPastel
import dev.kelompok1.myapp.ui.dashboard.DashboardState
import dev.kelompok1.myapp.ui.dashboard.DashboardViewModel
import java.io.File
import java.io.FileOutputStream
import dev.kelompok1.myapp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val viewModel: ProfileViewModel = viewModel(factory = ProfileViewModel.getFactory(context))
    val dashboardViewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.getFactory(context))
    val dashboardState by viewModel.profileState.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val profilePhotoUri by viewModel.profilePhotoUri.collectAsState()
    val defaultProfilePhoto = R.drawable.user

    // Dialog state for logout confirmation
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Function to save image to local storage
    fun saveImageToLocal(uri: Uri): Uri {
        try {
            // Create file in app's private storage directory
            val timeStamp = System.currentTimeMillis()
            val storageDir = context.filesDir
            val fileName = "profile_photo_$timeStamp.jpg"
            val imageFile = File(storageDir, fileName)
            
            // Copy the selected image to app's private storage
            val inputStream = context.contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(imageFile)
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            
            // Save the file path to SharedPreferences
            val sharedPrefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            with(sharedPrefs.edit()) {
                putString("profile_photo", imageFile.absolutePath)
                apply()
            }
            
            // Return the URI for the saved file
            return Uri.fromFile(imageFile)
        } catch (e: Exception) {
            Log.e("ProfileContent", "Failed to save image: ${e.message}", e)
            // Return original URI if saving failed
            return uri
        }
    }

    // Image picker launcher
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            // Save selected image to local storage with error handling
            val savedUri = saveImageToLocal(it)
            // Update both ViewModels with the new URI
            viewModel.updateProfilePhoto(savedUri)
            dashboardViewModel.updateProfilePhoto(savedUri)
        }
    }

    // Load saved profile photo on initial composition
    LaunchedEffect(Unit) {
        viewModel.fetchDosenInfo()
        
        if (profilePhotoUri == null) {
            val sharedPrefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            val savedPhotoPath = sharedPrefs.getString("profile_photo", null)
            if (savedPhotoPath != null) {
                val file = File(savedPhotoPath)
                if (file.exists()) {
                    val fileUri = Uri.fromFile(file)
                    viewModel.updateProfilePhoto(fileUri)
                    dashboardViewModel.updateProfilePhoto(fileUri)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Profil Dosen",
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
                    // Simple logout icon in app bar
                    IconButton(onClick = {
                        showLogoutDialog = true
                    }) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Logout",
                            tint = Color(0xFFD32F2F)
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
        // Logout confirmation dialog
        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = { Text("Konfirmasi Logout") },
                text = { Text("Apakah Anda yakin ingin keluar dari aplikasi?") },
                confirmButton = {
                    Button(
                        onClick = {
                            showLogoutDialog = false
                            onLogout()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                    ) {
                        Text("Ya, Keluar", color = Color.White)
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showLogoutDialog = false },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = tealPrimary
                        ),
                        border = BorderStroke(1.dp, tealPrimary)
                    ) {
                        Text("Batal", color = tealPrimary)
                    }
                }
            )
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (val state = dashboardState) {
                is DashboardState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = tealPrimary)
                    }
                }
                is DashboardState.Success -> {
                    val dosen = state.data.data
                    
                    // Profile Header with Photo
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(tealPrimary, tealLight)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .border(3.dp, Color.White, CircleShape)
                                    .clickable { launcher.launch("image/*") }
                                    .background(Color.White, CircleShape)
                            ) {
                                if (profilePhotoUri != null) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(profilePhotoUri)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Foto Profil",
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
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = dosen.nama,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "NIP: ${dosen.nip}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }

                    // Main Content
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Dosen Info Card
                        StyledSectionCard(title = "Informasi Dosen") {
                            ProfileItem(label = "Nama", value = dosen.nama)
                            ProfileItem(label = "NIP", value = dosen.nip)
                            ProfileItem(label = "Email", value = dosen.email)
                        }
                        
                        // Mahasiswa PA Card
                        StyledSectionCard(title = "Mahasiswa PA") {
                            dosen.info_mahasiswa_pa.ringkasan.forEach { ringkasan ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Angkatan ${ringkasan.tahun}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.DarkGray
                                    )
                                    Text(
                                        text = "${ringkasan.total} Mahasiswa",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = tealDark
                                    )
                                }
                                Divider(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    color = Color(0xFFEEEEEE)
                                )
                            }
                            
                            Text(
                                text = "Total Mahasiswa: ${dosen.info_mahasiswa_pa.daftar_mahasiswa.size}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = tealPrimary,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                        
                        // Add some space at the bottom
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
                
                is DashboardState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Gagal memuat data profil: ${state.message}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Red
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.fetchDosenInfo() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = tealPrimary
                            )
                        ) {
                            Text("Coba Lagi")
                        }
                    }
                }
                
                else -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Memuat data profil...")
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileSectionCard(
    title: String,
    items: List<ProfileMenuItem>
) {
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
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = title,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = tealDark
            )
            
            items.forEachIndexed { index, item ->
                ProfileMenuItemRow(
                    item = item,
                    isLast = index == items.size - 1
                )
            }
        }
    }
}

@Composable
fun ProfileMenuItemRow(
    item: ProfileMenuItem,
    isLast: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { item.onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.title,
            tint = tealLight,
            modifier = Modifier.size(24.dp)
        )
        
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.DarkGray
            )
            
            item.subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
        
        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = "Navigate",
            tint = Color.LightGray
        )
    }
    
    if (!isLast) {
        Divider(
            modifier = Modifier.padding(start = 56.dp, end = 16.dp),
            color = Color(0xFFEEEEEE)
        )
    }
}

data class ProfileMenuItem(
    val icon: ImageVector,
    val title: String,
    val subtitle: String? = null,
    val onClick: () -> Unit
)

// Helper function to load the profile photo from SharedPreferences
private fun loadProfilePhoto(context: Context): Uri? {
    val sharedPrefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    val savedPhotoPath = sharedPrefs.getString("profile_photo", null)
    
    return if (savedPhotoPath != null) {
        val file = File(savedPhotoPath)
        if (file.exists()) {
            Uri.fromFile(file)
        } else {
            null
        }
    } else {
        null
    }
} 