package com.example.setoranhafalan.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.setoranhafalan.data.model.Mahasiswa
import com.example.setoranhafalanapp.ui.navigation.AppBottomNavigation
import com.example.setoranhafalanapp.ui.navigation.tealPrimary
import com.example.setoranhafalanapp.ui.navigation.tealDark
import com.example.setoranhafalanapp.ui.navigation.tealLight
import com.example.setoranhafalanapp.ui.navigation.tealPastel
import com.example.setoranhafalanapp.ui.components.CircularProgressBar
import com.example.setoranhafalanapp.ui.components.StatisticBox

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MahasiswaScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.getFactory(context))
    val dashboardState by viewModel.dashboardState.collectAsState()

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
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(state.data.data.info_mahasiswa_pa.daftar_mahasiswa.size) { index ->
                            MahasiswaItem(
                                mahasiswa = state.data.data.info_mahasiswa_pa.daftar_mahasiswa[index],
                                onDetailClick = { route ->
                                    navController.navigate(route)
                                }
                            )
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
                InfoColumn("Status", if (mahasiswa.info_setoran.total_belum_setor == 0) "Lunas" else "Belum Lunas")
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
                            text = "Tanggal : $it",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InfoColumn(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = Color.Gray
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            fontWeight = FontWeight.SemiBold,
            color = Color.DarkGray
        )
    }
}
