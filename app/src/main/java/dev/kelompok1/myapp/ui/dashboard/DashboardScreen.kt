package dev.kelompok1.myapp.ui.dashboard

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import dev.kelompok1.myapp.R
import dev.kelompok1.myapp.ui.dashboard.DashboardState
import dev.kelompok1.myapp.ui.dashboard.DashboardViewModel
import dev.kelompok1.myapp.ui.login.LoginViewModel
import dev.kelompok1.myapp.ui.navigation.AppBottomNavigation
import dev.kelompok1.myapp.ui.navigation.tealPrimary
import dev.kelompok1.myapp.ui.navigation.tealDark
import dev.kelompok1.myapp.ui.navigation.tealPastel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.distinctUntilChanged
import java.io.File
import java.io.FileOutputStream
import android.graphics.BitmapFactory
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // Use viewModel factory once and remember it
    val dashboardViewModelFactory = remember { DashboardViewModel.getFactory(context) }
    val dashboardViewModel: DashboardViewModel = viewModel(factory = dashboardViewModelFactory)
    val loginViewModel: LoginViewModel = viewModel(factory = LoginViewModel.getFactory(context))
    
    // Optimize state collection with lifecycle awareness and distinctUntilChanged
    val dashboardState by remember(dashboardViewModel, lifecycleOwner) {
        dashboardViewModel.dashboardState
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .distinctUntilChanged()
    }.collectAsState(initial = DashboardState.Idle)
    
    val userName by remember(dashboardViewModel, lifecycleOwner) {
        dashboardViewModel.userName
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .distinctUntilChanged()
    }.collectAsState(initial = null)
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    var isRefreshing by remember { mutableStateOf(false) }
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isRefreshing)

    // Profile photo state
    var profilePhotoUri by rememberSaveable { mutableStateOf<String?>(loadProfilePhotoPath(context)) }
    val defaultProfilePhoto = R.drawable.user
    
    // Dropdown menu state
    var showProfileMenu by remember { mutableStateOf(false) }
    
    // Refresh profile photo whenever dashboard is composed
    LaunchedEffect(Unit) {
        profilePhotoUri = loadProfilePhotoPath(context)
    }

    // Handle refresh state based on dashboardState - with optimized recomposition
    LaunchedEffect(dashboardState) {
        when (dashboardState) {
            is DashboardState.Loading -> isRefreshing = true
            else -> isRefreshing = false
        }
        
        if (dashboardState is DashboardState.Error) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = (dashboardState as DashboardState.Error).message,
                    duration = SnackbarDuration.Long,
                    withDismissAction = true
                )
            }
        }
    }

    // Fetch data only once when screen is composed
    LaunchedEffect(Unit) {
        if (dashboardState !is DashboardState.Success) {
            dashboardViewModel.fetchDosenInfo()
        }
    }

    // Main screen scaffold with optimized parameters
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
                    // Profile Photo with dropdown menu
                    Box(
                        modifier = Modifier.wrapContentSize(Alignment.TopEnd)
                    ) {
                        // Clickable profile image
                        IconButton(
                            onClick = { showProfileMenu = true },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, Color.White, CircleShape)
                                    .background(tealPastel)
                            ) {
                                if (profilePhotoUri != null && File(profilePhotoUri!!).exists()) {
                                    val bitmap = BitmapFactory.decodeFile(profilePhotoUri)
                                    bitmap?.let {
                                        Image(
                                            bitmap = it.asImageBitmap(),
                                            contentDescription = "Foto Profil",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } ?: Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Profile",
                                        tint = tealPrimary,
                                        modifier = Modifier
                                            .padding(4.dp)
                                            .fillMaxSize()
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Profile",
                                        tint = tealPrimary,
                                        modifier = Modifier
                                            .padding(4.dp)
                                            .fillMaxSize()
                                    )
                                }
                            }
                        }
                        
                        // Dropdown menu
                        DropdownMenu(
                            expanded = showProfileMenu,
                            onDismissRequest = { showProfileMenu = false },
                            modifier = Modifier
                                .width(180.dp)
                                .background(Color.White)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Profil") },
                                onClick = {
                                    showProfileMenu = false
                                    navController.navigate("profile")
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = tealPrimary
                                    )
                                }
                            )
                            Divider()
                            DropdownMenuItem(
                                text = { Text("Keluar", color = Color(0xFFD32F2F)) },
                                onClick = {
                                    showProfileMenu = false
                                    onLogout()
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.ExitToApp,
                                        contentDescription = null,
                                        tint = Color(0xFFD32F2F) // Darker red tint for logout
                                    )
                                },
                                colors = MenuDefaults.itemColors(
                                    textColor = Color(0xFFD32F2F)
                                )
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            // Call AppBottomNavigation directly, without the key() wrapper
            AppBottomNavigation(navController)
        },
        containerColor = Color(0xFFF5F7F9)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            SwipeRefresh(
                state = swipeRefreshState,
                onRefresh = { 
                    // Optimize refresh to not call if already loading
                    if (dashboardState !is DashboardState.Loading) {
                        dashboardViewModel.fetchDosenInfo() 
                    }
                }
            ) {
                // LazyColumn with optimized parameters
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 16.dp,
                        bottom = padding.calculateBottomPadding() + 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    // Use consistent key for stable scrolling
                    state = rememberLazyListState()
                ) {
                    item {
                        // Dashboard Header
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
                                EnhancedLoadingCard()
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
                                
                                // Define categories with their colors
                                val categories = listOf(
                                    CategoryInfo("KP", Color(0xFF4CAF50), Color(0xFFE8F5E9)),
                                    CategoryInfo("SEMKP", Color(0xFF2196F3), Color(0xFFE3F2FD)),
                                    CategoryInfo("Daftar TA", Color(0xFFFF9800), Color(0xFFFFF3E0)),
                                    CategoryInfo("Sempro", Color(0xFF9C27B0), Color(0xFFF3E5F5)),
                                    CategoryInfo("Sidang TA", Color(0xFFF44336), Color(0xFFFFEBEE))
                                )
                                
                                // Group students by angkatan
                                val mahasiswaByAngkatan = dosen.info_mahasiswa_pa.daftar_mahasiswa.groupBy { it.angkatan }
                                
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

                                        // Overall Statistics with enhanced design
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            EnhancedStatisticItem(
                                                label = "Total Mahasiswa",
                                                value = totalMahasiswaPa.toString(),
                                                icon = Icons.Default.Person,
                                                color = tealPrimary,
                                                backgroundColor = tealPastel
                                            )

                                            EnhancedStatisticItem(
                                                label = "Jumlah Angkatan",
                                                value = jumlahAngkatan.toString(),
                                                icon = Icons.Default.Group,
                                                color = Color(0xFF2196F3),
                                                backgroundColor = Color(0xFFE3F2FD)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))

                                        // Submission Status with progress bar
                                        Column(
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "Status Setoran",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = tealDark
                                            )

                                            Spacer(modifier = Modifier.height(8.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                SubmissionStatusItem(
                                                    label = "Sudah Setor",
                                                    value = sudahSetoran.toString(),
                                                    total = totalMahasiswaPa.toString(),
                                                    color = Color(0xFF4CAF50),
                                                    backgroundColor = Color(0xFFE8F5E9)
                                                )

                                                SubmissionStatusItem(
                                                    label = "Belum Setor",
                                                    value = belumSetoran.toString(),
                                                    total = totalMahasiswaPa.toString(),
                                                    color = Color(0xFFF44336),
                                                    backgroundColor = Color(0xFFFFEBEE)
                                                )
                                            }
                                        }
                                    }
                                }

                                // Progress per Angkatan Card
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
                                            text = "Progres per Angkatan",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = tealDark
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))

                                        // Calculate overall progress for all categories
                                        val totalStudents = dosen.info_mahasiswa_pa.daftar_mahasiswa.size
                                        val completedStudents = dosen.info_mahasiswa_pa.daftar_mahasiswa.count { student ->
                                            student.info_setoran.total_sudah_setor > 0
                                        }
                                        val overallProgress = if (totalStudents > 0) {
                                            completedStudents.toFloat() / totalStudents.toFloat()
                                        } else {
                                            0f
                                        }
                                        
                                        // Add a single circular progress indicator at the top with improved styling
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 24.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            // Decorative background circles
                                            Box(
                                                modifier = Modifier
                                                    .size(150.dp)
                                                    .background(tealPastel.copy(alpha = 0.3f), CircleShape)
                                            )
                                            
                                            Box(
                                                modifier = Modifier.size(130.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                CircularProgressIndicator(
                                                    progress = overallProgress,
                                                    modifier = Modifier.fillMaxSize(),
                                                    color = tealPrimary,
                                                    strokeWidth = 10.dp,
                                                    trackColor = tealPrimary.copy(alpha = 0.2f)
                                                )
                                                
                                                // Inner content with improved styling
                                                Box(
                                                    modifier = Modifier
                                                        .size(100.dp)
                                                        .background(Color.White, CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Column(
                                                        horizontalAlignment = Alignment.CenterHorizontally
                                                    ) {
                                                        Text(
                                                            text = "${(overallProgress * 100).toInt()}%",
                                                            style = MaterialTheme.typography.headlineSmall,
                                                            fontWeight = FontWeight.Bold,
                                                            color = tealPrimary
                                                        )
                                                        Text(
                                                            text = "$completedStudents/$totalStudents",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = Color.Gray
                                                        )
                                                        Text(
                                                            text = "Progres Total",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            fontWeight = FontWeight.Medium,
                                                            color = tealDark
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        // Show progress for each category by angkatan
                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            // Create a 2-column grid using Row and Column
                                            val categoriesPairs = categories.chunked(2)
                                            
                                            categoriesPairs.forEach { rowCategories ->
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 4.dp),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    rowCategories.forEach { category ->
                                                        Card(
                                                            modifier = Modifier
                                                                .weight(1f)
                                                                .padding(vertical = 4.dp),
                                                            colors = CardDefaults.cardColors(
                                                                containerColor = category.backgroundColor
                                                            )
                                                        ) {
                                                            Column(
                                                                modifier = Modifier.padding(8.dp)
                                                            ) {
                                                                Text(
                                                                    text = category.name,
                                                                    style = MaterialTheme.typography.titleSmall,
                                                                    fontWeight = FontWeight.SemiBold,
                                                                    color = category.color
                                                                )

                                                                Spacer(modifier = Modifier.height(4.dp))

                                                                Divider(
                                                                    modifier = Modifier.padding(vertical = 4.dp),
                                                                    color = category.color.copy(alpha = 0.2f)
                                                                )

                                                                // Define ayat requirements per category based on reference
                                                                val ayatRequirements = mapOf(
                                                                    "KP" to 8,
                                                                    "SEMKP" to 8,
                                                                    "Daftar TA" to 8,
                                                                    "Sempro" to 12,
                                                                    "Sidang TA" to 3
                                                                )
                                                                val requiredAyat = ayatRequirements[category.name] ?: 0

                                                                // Show progress for each angkatan with updated logic
                                                                mahasiswaByAngkatan.entries.sortedByDescending { it.key }.forEach { (angkatan, students) ->
                                                                    val angkatanTotalStudents = students.size
                                                                    val angkatanCompletedStudents = students.count { student ->
                                                                        student.info_setoran.total_sudah_setor >= requiredAyat
                                                                    }
                                                                    val angkatanProgress = if (angkatanTotalStudents > 0) {
                                                                        angkatanCompletedStudents.toFloat() / angkatanTotalStudents.toFloat()
                                                                    } else {
                                                                        0f
                                                                    }

                                                                    Column(
                                                                        modifier = Modifier
                                                                            .fillMaxWidth()
                                                                            .padding(vertical = 2.dp)
                                                                    ) {
                                                                        Row(
                                                                            modifier = Modifier.fillMaxWidth(),
                                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                                            verticalAlignment = Alignment.CenterVertically
                                                                        ) {
                                                                            Text(
                                                                                text = "Angkatan $angkatan",
                                                                                style = MaterialTheme.typography.bodySmall,
                                                                                color = Color.DarkGray
                                                                            )
                                                                            Text(
                                                                                text = "$angkatanCompletedStudents/$angkatanTotalStudents",
                                                                                style = MaterialTheme.typography.bodySmall,
                                                                                color = category.color
                                                                            )
                                                                        }

                                                                        LinearProgressIndicator(
                                                                            progress = angkatanProgress,
                                                                            modifier = Modifier
                                                                                .fillMaxWidth()
                                                                                .height(3.dp)
                                                                                .clip(RoundedCornerShape(1.5.dp)),
                                                                            color = category.color,
                                                                            trackColor = category.color.copy(alpha = 0.2f)
                                                                        )
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // Latest Submissions Card
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
                                            text = "Setoran Terbaru",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = tealDark
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))

                                        // Horizontal tabs for angkatan
                                        var selectedTabIndex by remember { mutableStateOf(0) }
                                        val sortedAngkatan = mahasiswaByAngkatan.keys.sortedByDescending { it }.toList()

                                        // Tab row
                                        ScrollableTabRow(
                                            selectedTabIndex = selectedTabIndex,
                                            containerColor = Color.White,
                                            contentColor = tealPrimary,
                                            edgePadding = 0.dp,
                                            indicator = { tabPositions ->
                                                TabRowDefaults.Indicator(
                                                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                                    height = 3.dp,
                                                    color = tealPrimary
                                                )
                                            }
                                        ) {
                                            sortedAngkatan.forEachIndexed { index, angkatan ->
                                                Tab(
                                                    selected = selectedTabIndex == index,
                                                    onClick = { selectedTabIndex = index },
                                                    text = {
                                                        Text(
                                                            text = "Angkatan $angkatan",
                                                            style = MaterialTheme.typography.titleSmall,
                                                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                                                        )
                                                    }
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))

                                        // Content for selected angkatan
                                        sortedAngkatan.getOrNull(selectedTabIndex)?.let { selectedAngkatan ->
                                            mahasiswaByAngkatan[selectedAngkatan]?.let { students ->
                                                Card(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 4.dp),
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = Color(0xFFF5F7F9)
                                                    )
                                                ) {
                                                    Column(
                                                        modifier = Modifier.padding(12.dp)
                                                    ) {
                                                        // Show students with their latest submissions
                                                        students.filter { it.info_setoran.total_sudah_setor > 0 }
                                                            .sortedByDescending { it.info_setoran.tgl_terakhir_setor }
                                                            .take(5)
                                                            .forEach { student ->
                                                                LatestSubmissionItem(
                                                                    studentName = student.nama,
                                                                    submissionDate = student.info_setoran.tgl_terakhir_setor ?: "-",
                                                                    progress = "${student.info_setoran.total_sudah_setor}/${student.info_setoran.total_wajib_setor}",
                                                                    color = tealPrimary
                                                                )
                                                            }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            is DashboardState.Error -> {
                                EnhancedErrorCard(
                                    errorMessage = state.message,
                                    onRetry = { dashboardViewModel.fetchDosenInfo() }
                                )
                            }
                            else -> {
                                EmptyCard { dashboardViewModel.fetchDosenInfo() }
                            }
                        }
                    }
                }
            }
            
            // Network activity indicator optimized with key-based visibility
            if (dashboardState is DashboardState.Loading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter),
                    color = tealPrimary
                )
            }
        }
    }
}

@Composable
fun EnhancedLoadingCard() {
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
                color = tealPrimary,
                strokeWidth = 4.dp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Memuat data dosen...",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )
            Text(
                text = "Mohon tunggu sebentar",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun EnhancedErrorCard(errorMessage: String, onRetry: () -> Unit) {
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
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Error",
                tint = Color(0xFFEF4444),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Gagal memuat data",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFEF4444)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = tealPrimary
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Retry",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Coba Lagi")
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

@Composable
fun EnhancedStatisticItem(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
    backgroundColor: Color
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .padding(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )

            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun SubmissionStatusItem(
    label: String,
    value: String,
    total: String,
    color: Color,
    backgroundColor: Color
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .padding(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )

            Text(
                text = "dari $total",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = color
            )

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = value.toFloat() / total.toFloat(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = color,
                trackColor = color.copy(alpha = 0.2f)
            )
        }
    }
}

@Composable
fun LatestSubmissionItem(
    studentName: String,
    submissionDate: String,
    progress: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = studentName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.DarkGray
            )
            Text(
                text = submissionDate,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        Text(
            text = progress,
            style = MaterialTheme.typography.bodyMedium,
            color = color
        )
    }
}

data class CategoryInfo(
    val name: String,
    val color: Color,
    val backgroundColor: Color
)

private fun saveImageToInternalStorage(context: Context, uri: Uri): File {
    val inputStream = context.contentResolver.openInputStream(uri)
    // Get user email
    val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    val userEmail = sharedPreferences.getString("user_email", "") ?: ""
    // Create a unique filename based on email
    val fileName = if (userEmail.isNotEmpty()) {
        "profile_photo_${userEmail.hashCode()}.jpg"
    } else {
        "profile_photo.jpg"
    }
    val file = File(context.filesDir, fileName)
    FileOutputStream(file).use { outputStream ->
        inputStream?.copyTo(outputStream)
    }
    inputStream?.close()
    return file
}

private fun saveProfilePhotoPath(context: Context, path: String) {
    val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    val userEmail = sharedPreferences.getString("user_email", "") ?: ""
    // Save path with email as part of the key
    val key = if (userEmail.isNotEmpty()) {
        "profile_photo_path_$userEmail"
    } else {
        "profile_photo_path"
    }
    sharedPreferences.edit().putString(key, path).apply()
}

private fun loadProfilePhotoPath(context: Context): String? {
    val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    val userEmail = sharedPreferences.getString("user_email", "") ?: ""
    // Load path using email-specific key
    val key = if (userEmail.isNotEmpty()) {
        "profile_photo_path_$userEmail"
    } else {
        "profile_photo_path"
    }
    return sharedPreferences.getString(key, null)
}

private fun clearProfilePhoto(context: Context) {
    // Only clear current user's photo
    val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    val userEmail = sharedPreferences.getString("user_email", "") ?: ""
    
    if (userEmail.isNotEmpty()) {
        val key = "profile_photo_path_$userEmail"
        sharedPreferences.edit().remove(key).apply()
        
        val fileName = "profile_photo_${userEmail.hashCode()}.jpg"
        val file = File(context.filesDir, fileName)
        file.delete()
    } else {
        // Fallback to default behavior
        sharedPreferences.edit().remove("profile_photo_path").apply()
        val file = File(context.filesDir, "profile_photo.jpg")
        file.delete()
    }
}