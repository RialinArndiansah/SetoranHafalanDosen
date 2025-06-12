package dev.kelompok1.myapp.ui.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import dev.kelompok1.myapp.data.model.Mahasiswa
import dev.kelompok1.myapp.ui.navigation.AppBottomNavigation
import dev.kelompok1.myapp.ui.navigation.tealPrimary
import dev.kelompok1.myapp.ui.navigation.tealDark
import dev.kelompok1.myapp.ui.navigation.tealLight
import dev.kelompok1.myapp.ui.navigation.tealPastel
import dev.kelompok1.myapp.ui.components.CircularProgressBar
import dev.kelompok1.myapp.ui.components.StatisticBox
import dev.kelompok1.myapp.ui.components.formatDateToWIB

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MahasiswaScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.getFactory(context))
    val dashboardState by viewModel.dashboardState.collectAsState()
    var selectedTabIndex by remember { mutableStateOf(0) }
    var showSearchDialog by remember { mutableStateOf(false) }
    var selectedAngkatan by remember { mutableStateOf<String?>(null) }
    var filteredStudents by remember { mutableStateOf<List<Mahasiswa>>(emptyList()) }

    LaunchedEffect(Unit) {
        viewModel.fetchDosenInfo()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Daftar Mahasiswa Bimbingan", fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showSearchDialog = true }) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = tealPrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        bottomBar = {
            AppBottomNavigation(navController)
        },
        containerColor = Color(0xFFF5F7F9)
    ) { padding ->
        
        // Search Dialog
        if (showSearchDialog) {
            Dialog(onDismissRequest = { 
                showSearchDialog = false
                selectedAngkatan = null
            }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(20.dp)
                            .fillMaxWidth()
                    ) {
                         
                        
                        // Step 1: Select Angkatan (if no angkatan is selected yet)
                        if (selectedAngkatan == null) {

                            
                            Text(
                                text = "Pilih Angkatan:",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium,
                                color = Color.DarkGray
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            when (val state = dashboardState) {
                                is DashboardState.Success -> {
                                    val mahasiswaList = state.data.data.info_mahasiswa_pa.daftar_mahasiswa
                                    val angkatanList = mahasiswaList.map { it.angkatan }.distinct().sorted()
                                    
                                    LazyColumn(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(320.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        items(angkatanList.size) { index ->
                                            val angkatan = angkatanList[index]
                                            Card(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable { 
                                                        selectedAngkatan = angkatan
                                                        filteredStudents = mahasiswaList.filter { it.angkatan == angkatan }
                                                    },
                                                colors = CardDefaults.cardColors(
                                                    containerColor = Color.White
                                                ),
                                                shape = RoundedCornerShape(12.dp),
                                                elevation = CardDefaults.cardElevation(2.dp),
                                                border = BorderStroke(1.dp, tealPastel)
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(16.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Person,
                                                        contentDescription = null,
                                                        tint = tealPrimary,
                                                        modifier = Modifier
                                                            .size(28.dp)
                                                            .background(tealPastel, shape = RoundedCornerShape(50))
                                                            .padding(6.dp)
                                                    )
                                                    
                                                    Spacer(modifier = Modifier.width(12.dp))
                                                    
                                                    Text(
                                                        text = "Angkatan $angkatan",
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = Color.DarkGray,
                                                        style = MaterialTheme.typography.titleMedium
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                else -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(100.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(color = tealPrimary)
                                    }
                                }
                            }
                        } 
                        // Step 2: Select student from the chosen angkatan
                        else {
                            Text(
                                text = "Pencarian Mahasiswa",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = tealDark,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = { selectedAngkatan = null },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = tealPrimary
                                    )
                                ) {
                                    Icon(
                                        Icons.Default.ArrowBack, 
                                        contentDescription = "Back",
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Kembali",
                                        fontWeight = FontWeight.Medium,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                
                                Spacer(modifier = Modifier.weight(1f))
                                
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = tealPrimary
                                    ),
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Text(
                                        text = "Angkatan $selectedAngkatan",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                            
                            Divider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = Color(0xFFEEEEEE),
                                thickness = 1.dp
                            )
                            
                            Text(
                                text = "Pilih Mahasiswa:",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium,
                                color = Color.DarkGray
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(320.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(filteredStudents.size) { index ->
                                    val mahasiswa = filteredStudents[index]
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { 
                                                showSearchDialog = false
                                                navController.navigate("lihat_setoran/${mahasiswa.nim}")
                                            },
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        elevation = CardDefaults.cardElevation(2.dp),
                                        border = BorderStroke(1.dp, tealPastel)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = null,
                                                tint = tealPrimary,
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .background(tealPastel, shape = RoundedCornerShape(50))
                                                    .padding(8.dp)
                                            )
                                            
                                            Spacer(modifier = Modifier.width(12.dp))
                                            
                                            Column {
                                                Text(
                                                    text = mahasiswa.nama,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Color.DarkGray,
                                                    style = MaterialTheme.typography.titleMedium
                                                )
                                                Text(
                                                    text = "NIM: ${mahasiswa.nim}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = Color.Gray
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Close button
                        Button(
                            onClick = { 
                                showSearchDialog = false 
                                selectedAngkatan = null
                            },
                            modifier = Modifier
                                .align(Alignment.End)
                                .padding(top = 8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = tealPrimary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Tutup", color = Color.White)
                        }
                    }
                }
            }
        }
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = dashboardState) {
                is DashboardState.Idle -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Memulai pengambilan data...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray
                        )
                    }
                }
                is DashboardState.Loading -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = tealPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Memuat data mahasiswa...", style = MaterialTheme.typography.bodyLarge)
                    }
                }
                is DashboardState.Success -> {
                    val mahasiswaList = state.data.data.info_mahasiswa_pa.daftar_mahasiswa
                    val angkatanList = mahasiswaList.map { it.angkatan }.distinct().sorted()
                    
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Tab Row for angkatan
                        ScrollableTabRow(
                            selectedTabIndex = selectedTabIndex,
                            modifier = Modifier.fillMaxWidth(),
                            containerColor = Color.White,
                            contentColor = tealPrimary,
                            indicator = { tabPositions ->
                                TabRowDefaults.Indicator(
                                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                    height = 3.dp,
                                    color = tealPrimary
                                )
                            },
                            edgePadding = 16.dp
                        ) {
                            angkatanList.forEachIndexed { index, angkatan ->
                                Tab(
                                    selected = selectedTabIndex == index,
                                    onClick = { selectedTabIndex = index },
                                    text = { Text("Angkatan $angkatan", fontWeight = FontWeight.Medium) }
                                )
                            }
                        }
                        
                        // Content
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            val filteredMahasiswa = mahasiswaList.filter { it.angkatan == angkatanList[selectedTabIndex] }
                            items(filteredMahasiswa.size) { index ->
                                MahasiswaItem(
                                    mahasiswa = filteredMahasiswa[index],
                                    onDetailClick = { route ->
                                        navController.navigate(route)
                                    }
                                )
                            }
                        }
                    }
                }
                is DashboardState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Gagal memuat data mahasiswa: ${state.message}",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Red
                        )
                        Spacer(modifier = Modifier.height(8.dp))
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
            }
        }
    }
}

@Composable
fun MahasiswaItem(
    mahasiswa: Mahasiswa,
    onDetailClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = tealPrimary,
                    modifier = Modifier
                        .size(48.dp)
                        .background(tealPastel, shape = RoundedCornerShape(50))
                        .padding(8.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = mahasiswa.nama,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )
                    Text(
                        text = "NIM: ${mahasiswa.nim}",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoColumn("Angkatan", mahasiswa.angkatan)
                InfoColumn("Semester", mahasiswa.semester.toString())
                InfoColumn(
                    "Status", 
                    when {
                        mahasiswa.info_setoran.total_sudah_setor == 0 -> "Let's start\uD83E\uDD17"
                        mahasiswa.info_setoran.persentase_progres_setor == 100f -> "Completed"
                        else -> "On Progress"
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Progres Setoran: ${mahasiswa.info_setoran.persentase_progres_setor}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                CircularProgressBar(
                    progress = mahasiswa.info_setoran.persentase_progres_setor / 100f,
                    size = 60.dp,
                    strokeWidth = 8.dp,
                    progressColor = when {
                        mahasiswa.info_setoran.persentase_progres_setor < 30 -> Color(0xFFE57373)
                        mahasiswa.info_setoran.persentase_progres_setor < 70 -> Color(0xFFFFB74D)
                        else -> Color(0xFF81C784)
                    },
                    backgroundColor = Color(0xFFE0F2F1),
                    textColor = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val route = "lihat_setoran/${mahasiswa.nim}"
                    onDetailClick(route)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = tealPrimary
                )
            ) {
                Text("Detail Setoran", fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = tealPastel.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Terakhir Setor: ${mahasiswa.info_setoran.terakhir_setor}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    mahasiswa.info_setoran.tgl_terakhir_setor?.let {
                        Text(
                            text = "Tanggal: ${formatDateToWIB(it)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.Gray
                        )
                    } ?: Text(
                        text = "Tanggal: -",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun InfoColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Color.Gray
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}
