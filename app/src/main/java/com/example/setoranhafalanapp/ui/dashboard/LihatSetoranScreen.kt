package com.example.setoranhafalan.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.setoranhafalanapp.data.model.SetoranMahasiswaResponse

import com.example.setoranhafalanapp.ui.navigation.AppBottomNavigation
import com.example.setoranhafalanapp.ui.navigation.tealPrimary
import com.example.setoranhafalanapp.ui.navigation.tealDark
import com.example.setoranhafalanapp.ui.navigation.tealLight
import com.example.setoranhafalanapp.ui.navigation.tealPastel
import com.example.setoranhafalanapp.ui.components.StatisticBox
import com.example.setoranhafalanapp.ui.components.StyledSectionCard
import com.example.setoranhafalanapp.ui.components.ProfileItem
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Brush

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LihatSetoranScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.getFactory(context))
    val dashboardState by viewModel.dashboardState.collectAsState()
    val setoranState by viewModel.setoranState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var nimInput by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val mahasiswaList = when (val state = dashboardState) {
        is DashboardState.Success -> state.data.data.info_mahasiswa_pa.daftar_mahasiswa
        else -> emptyList()
    }

    LaunchedEffect(Unit) {
        viewModel.fetchDosenInfo()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Setoran Mahasiswa", fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = tealPrimary,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        bottomBar = {
            AppBottomNavigation(navController)
        },
        content = { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color(0xFFF5F7F9))
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            ),
                            elevation = CardDefaults.cardElevation(2.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Pilih Mahasiswa",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = tealDark
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                ExposedDropdownMenuBox(
                                    expanded = expanded,
                                    onExpandedChange = { expanded = !expanded }
                                ) {
                                    TextField(
                                        value = if (nimInput.isNotBlank()) {
                                            val selected = mahasiswaList.find { it.nim == nimInput }
                                            selected?.let { "${it.nim} - ${it.nama}" } ?: nimInput
                                        } else {
                                            ""
                                        },
                                        onValueChange = {},
                                        label = { Text("NIM - Nama Mahasiswa") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor(),
                                        readOnly = true,
                                        colors = TextFieldDefaults.colors(
                                            focusedContainerColor = Color(0xFFF3F4F6),
                                            unfocusedContainerColor = Color(0xFFF3F4F6),
                                            focusedIndicatorColor = tealPrimary,
                                            unfocusedIndicatorColor = Color(0xFFD1D5DB)
                                        ),
                                        trailingIcon = {
                                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                        }
                                    )
                                    ExposedDropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false }
                                    ) {
                                        mahasiswaList.forEach { mahasiswa ->
                                            DropdownMenuItem(
                                                text = { Text("${mahasiswa.nim} - ${mahasiswa.nama}") },
                                                onClick = {
                                                    nimInput = mahasiswa.nim
                                                    expanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        if (nimInput.isNotBlank()) {
                                            viewModel.fetchSetoranMahasiswa(nimInput)
                                        } else {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Pilih mahasiswa terlebih dahulu")
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = tealPrimary
                                    )
                                ) {
                                    Text("Lihat Setoran")
                                }
                            }
                        }
                    }

                    item {
                        when (val state = setoranState) {
                            is SetoranState.Loading -> {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(48.dp),
                                        color = tealPrimary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Memuat setoran...",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                            is SetoranState.Success -> {
                                state.data?.let { setoran ->
                                    SetoranMahasiswaInfo(setoran)
                                } ?: Text(
                                    text = "Data setoran kosong",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            is SetoranState.Error -> {
                                LaunchedEffect(state) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(state.message)
                                    }
                                }
                                Text(
                                    text = "Gagal memuat setoran: ${state.message}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            else -> {}
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun SetoranMahasiswaInfo(setoran: SetoranMahasiswaResponse) {
    val data = setoran.data

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        StyledSectionCard(title = "Info Mahasiswa") {
            ProfileItem(label = "Nama", value = data.info.nama)
            ProfileItem(label = "NIM", value = data.info.nim)
            ProfileItem(label = "Email", value = data.info.email)
            ProfileItem(label = "Angkatan", value = data.info.angkatan)
            ProfileItem(label = "Semester", value = data.info.semester.toString())
            ProfileItem(label = "Dosen PA", value = data.info.dosen_pa.nama)
        }


        StyledSectionCard(title = "Progres Setoran") {
            Text(
                text = "Persentase: ${data.setoran.info_dasar.persentase_progres_setor}%",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = (data.setoran.info_dasar.persentase_progres_setor / 100.0).toFloat(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = when {
                    data.setoran.info_dasar.persentase_progres_setor < 30 -> Color(0xFFE57373)
                    data.setoran.info_dasar.persentase_progres_setor < 70 -> Color(0xFFFFB74D)
                    else -> Color(0xFF81C784)
                }
            )

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatisticBox(
                    title = "Wajib Setor",
                    value = data.setoran.info_dasar.total_wajib_setor.toString(),
                    color = tealPastel
                )
                StatisticBox(
                    title = "Sudah Setor",
                    value = data.setoran.info_dasar.total_sudah_setor.toString(),
                    color = Color(0xFFE0F7FA)
                )
                StatisticBox(
                    title = "Belum Setor",
                    value = data.setoran.info_dasar.total_belum_setor.toString(),
                    color = Color(0xFFF5F5F5)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Terakhir Setor: ${data.setoran.info_dasar.terakhir_setor}")
            data.setoran.info_dasar.tgl_terakhir_setor?.let {
                Text("Tanggal: ${it.take(10)}")
            }
        }


        StyledSectionCard(title = "Ringkasan Setoran") {
            data.setoran.ringkasan.forEach { ringkasan ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(ringkasan.label)
                    Text("${ringkasan.total_sudah_setor}/${ringkasan.total_wajib_setor} (${ringkasan.persentase_progres_setor}%)")
                }
            }
        }

        // === DETAIL SETORAN ===
        StyledSectionCard(title = "Detail Setoran") {
            data.setoran.detail.forEach { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = tealPastel.copy(alpha = 0.5f)
                    ),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(item.nama, fontWeight = FontWeight.SemiBold)
                        Text("Label: ${item.label}")
                        Text("Status: ${if (item.sudah_setor) "Sudah Setor" else "Belum Setor"}")
                        item.info_setoran?.let { info ->
                            Text("Tanggal Setoran: ${info.tgl_setoran}")
                            Text("Tanggal Validasi: ${info.tgl_validasi}")
                            Text("Dosen: ${info.dosen_yang_mengesahkan.nama}")
                        }
                    }
                }
            }
        }
    }
}