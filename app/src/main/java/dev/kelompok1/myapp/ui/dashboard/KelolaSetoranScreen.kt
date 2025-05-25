package dev.kelompok1.myapp.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import dev.kelompok1.myapp.data.model.SetoranItem
import dev.kelompok1.myapp.ui.navigation.AppBottomNavigation
import kotlinx.coroutines.launch
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import android.app.DatePickerDialog
import java.util.*
import java.text.SimpleDateFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KelolaSetoranScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.getFactory(context))
    val dashboardState by viewModel.dashboardState.collectAsState()
    val setoranState by viewModel.setoranState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var nimInput by remember { mutableStateOf("") }
    var idKomponenSetoranInput by remember { mutableStateOf("") }
    var namaKomponenSetoranInput by remember { mutableStateOf("") }
    var idSetoranInput by remember { mutableStateOf("") }
    var idKomponenSetoranDeleteInput by remember { mutableStateOf("") }
    var namaKomponenSetoranDeleteInput by remember { mutableStateOf("") }
    var nimExpanded by remember { mutableStateOf(false) }
    var komponenExpanded by remember { mutableStateOf(false) }
    var setoranExpanded by remember { mutableStateOf(false) }
    var isDateSelected by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf("") }

    val mahasiswaList = when (val state = dashboardState) {
        is DashboardState.Success -> state.data.data.info_mahasiswa_pa.daftar_mahasiswa
        else -> emptyList()
    }

    val komponenSetoranList = when (val state = setoranState) {
        is SetoranState.Success -> state.data?.data?.setoran?.detail
            ?.filter { !it.sudah_setor }
            ?.map { it } ?: emptyList()
        else -> emptyList()
    }

    val idSetoranList = when (val state = setoranState) {
        is SetoranState.Success -> state.data?.data?.setoran?.detail
            ?.filter { it.sudah_setor && it.info_setoran != null }
            ?.mapNotNull { detail ->
                detail.info_setoran?.let { info ->
                    SetoranItem(
                        id = info.id,
                        idKomponenSetoran = detail.id,
                        namaKomponenSetoran = detail.nama
                    )
                }
            } ?: emptyList()
        else -> emptyList()
    }

    LaunchedEffect(nimInput) {
        if (nimInput.isNotBlank()) {
            viewModel.fetchSetoranMahasiswa(nimInput)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.fetchDosenInfo()
    }

    val gradientBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFFC280CC),
            Color(0xFFAE7CE7),
            Color(0xFF72A5BE)
        ),
        start = Offset(0f, 0f),
        end = Offset(0f, Float.POSITIVE_INFINITY)
    )

    // Date Picker setup
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    // Create DatePickerDialog but don't show it initially
    val datePickerDialog = DatePickerDialog(
        context,
        { _, selectedYear, selectedMonth, selectedDay ->
            // Format the date in YYYY-MM-DD format for API compatibility
            selectedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
        },
        year,
        month,
        day
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Kelola Setoran",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF4CAF50),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
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
                .background(gradientBrush)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Card 1: Pilih Mahasiswa
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Pilih Mahasiswa",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1F2937)
                            )
                            Divider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = Color(0xFFE5E7EB)
                            )
                            ExposedDropdownMenuBox(
                                expanded = nimExpanded,
                                onExpandedChange = { nimExpanded = !nimExpanded }
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
                                        focusedIndicatorColor = Color(0xFF3B82F6),
                                        unfocusedIndicatorColor = Color(0xFFD1D5DB)
                                    ),
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = "Person Icon",
                                            tint = Color(0xFF3B82F6)
                                        )
                                    },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = nimExpanded)
                                    }
                                )
                                ExposedDropdownMenu(
                                    expanded = nimExpanded,
                                    onDismissRequest = { nimExpanded = false }
                                ) {
                                    mahasiswaList.forEach { mahasiswa ->
                                        DropdownMenuItem(
                                            text = { Text("${mahasiswa.nim} - ${mahasiswa.nama}") },
                                            onClick = {
                                                nimInput = mahasiswa.nim
                                                nimExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                // Card 2: Tambah Setoran with Date Selection
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Tambah Setoran",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1F2937)
                            )
                            Divider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = Color(0xFFE5E7EB)
                            )
                            // Date Selection Checkbox
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isDateSelected,
                                    onCheckedChange = { isDateSelected = it }
                                )
                                Text(
                                    text = if (isDateSelected) "Pilih Tanggal" else "Default Hari Ini",
                                    modifier = Modifier.padding(start = 8.dp),
                                    color = Color(0xFF1F2937)
                                )
                            }
                            if (isDateSelected) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = selectedDate,
                                        onValueChange = { },
                                        label = { Text("Tanggal Setoran") },
                                        modifier = Modifier
                                            .weight(1f),
                                        readOnly = true,
                                        colors = TextFieldDefaults.colors(
                                            focusedContainerColor = Color(0xFFF3F4F6),
                                            unfocusedContainerColor = Color(0xFFF3F4F6),
                                            focusedIndicatorColor = Color(0xFF3B82F6),
                                            unfocusedIndicatorColor = Color(0xFFD1D5DB)
                                        ),
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.DateRange,
                                                contentDescription = "Date Icon",
                                                tint = Color(0xFF3B82F6)
                                            )
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = {
                                            datePickerDialog.show()
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF3B82F6)
                                        ),
                                        modifier = Modifier
                                            .height(56.dp)
                                    ) {
                                        Text("Pilih")
                                    }
                                }
                            }
                            ExposedDropdownMenuBox(
                                expanded = komponenExpanded,
                                onExpandedChange = { komponenExpanded = !komponenExpanded }
                            ) {
                                TextField(
                                    value = namaKomponenSetoranInput,
                                    onValueChange = {},
                                    label = { Text("Komponen Setoran") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    readOnly = true,
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color(0xFFF3F4F6),
                                        unfocusedContainerColor = Color(0xFFF3F4F6),
                                        focusedIndicatorColor = Color(0xFF3B82F6),
                                        unfocusedIndicatorColor = Color(0xFFD1D5DB)
                                    ),
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = komponenExpanded)
                                    }
                                )
                                ExposedDropdownMenu(
                                    expanded = komponenExpanded,
                                    onDismissRequest = { komponenExpanded = false }
                                ) {
                                    komponenSetoranList.forEach { komponen ->
                                        DropdownMenuItem(
                                            text = { Text(komponen.nama) },
                                            onClick = {
                                                idKomponenSetoranInput = komponen.id
                                                namaKomponenSetoranInput = komponen.nama
                                                komponenExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    if (nimInput.isBlank()) {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Pilih mahasiswa terlebih dahulu")
                                        }
                                    } else if (idKomponenSetoranInput.isBlank() || namaKomponenSetoranInput.isBlank()) {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Pilih komponen setoran")
                                        }
                                    } else {
                                        // Pass the selected date to the API if date selection is enabled and a date is selected
                                        if (isDateSelected && selectedDate.isNotBlank()) {
                                            viewModel.postSetoranMahasiswa(
                                                nimInput, 
                                                idKomponenSetoranInput, 
                                                namaKomponenSetoranInput,
                                                selectedDate
                                            )
                                        } else {
                                            viewModel.postSetoranMahasiswa(
                                                nimInput, 
                                                idKomponenSetoranInput, 
                                                namaKomponenSetoranInput
                                            )
                                        }
                                        
                                        idKomponenSetoranInput = ""
                                        namaKomponenSetoranInput = ""
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF3B82F6)
                                )
                            ) {
                                Text(
                                    "Tambah Setoran",
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
                // Card 3: Hapus Setoran
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Hapus Setoran",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1F2937)
                            )
                            Divider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = Color(0xFFE5E7EB)
                            )
                            ExposedDropdownMenuBox(
                                expanded = setoranExpanded,
                                onExpandedChange = { setoranExpanded = !setoranExpanded }
                            ) {
                                TextField(
                                    value = namaKomponenSetoranDeleteInput,
                                    onValueChange = {},
                                    label = { Text("Setoran") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    readOnly = true,
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color(0xFFF3F4F6),
                                        unfocusedContainerColor = Color(0xFFF3F4F6),
                                        focusedIndicatorColor = Color(0xFF3B82F6),
                                        unfocusedIndicatorColor = Color(0xFFD1D5DB)
                                    ),
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = setoranExpanded)
                                    }
                                )
                                ExposedDropdownMenu(
                                    expanded = setoranExpanded,
                                    onDismissRequest = { setoranExpanded = false }
                                ) {
                                    idSetoranList.forEach { setoran ->
                                        DropdownMenuItem(
                                            text = { Text(setoran.namaKomponenSetoran) },
                                            onClick = {
                                                idSetoranInput = setoran.id ?: ""
                                                idKomponenSetoranDeleteInput = setoran.idKomponenSetoran
                                                namaKomponenSetoranDeleteInput = setoran.namaKomponenSetoran
                                                setoranExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    if (nimInput.isBlank()) {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Pilih mahasiswa terlebih dahulu")
                                        }
                                    } else if (idSetoranInput.isBlank() || idKomponenSetoranDeleteInput.isBlank() || namaKomponenSetoranDeleteInput.isBlank()) {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Pilih setoran untuk dihapus")
                                        }
                                    } else {
                                        viewModel.deleteSetoranMahasiswa(nimInput, idSetoranInput, idKomponenSetoranDeleteInput, namaKomponenSetoranDeleteInput)
                                        idSetoranInput = ""
                                        idKomponenSetoranDeleteInput = ""
                                        namaKomponenSetoranDeleteInput = ""
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFEF4444)
                                )
                            ) {
                                Text(
                                    "Hapus Setoran",
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }

                // Feedback State
                item {
                    when (val state = setoranState) {
                        is SetoranState.Success -> {
                            state.data?.let {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFFD1FAE5)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "Aksi setoran berhasil, data diperbarui",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color(0xFF065F46)
                                        )
                                    }
                                }
                            }
                        }
                        is SetoranState.Error -> {
                            LaunchedEffect(state) {
                                scope.launch {
                                    snackbarHostState.showSnackbar(state.message)
                                }
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}