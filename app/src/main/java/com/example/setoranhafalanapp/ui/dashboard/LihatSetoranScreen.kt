package com.example.setoranhafalan.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

import com.example.setoranhafalanapp.ui.navigation.AppBottomNavigation
import com.example.setoranhafalanapp.ui.navigation.tealPrimary
import com.example.setoranhafalanapp.ui.navigation.tealDark
import com.example.setoranhafalanapp.ui.navigation.tealLight
import com.example.setoranhafalanapp.ui.navigation.tealPastel
import com.example.setoranhafalanapp.ui.components.StyledSectionCard
import com.example.setoranhafalanapp.ui.components.ProfileItem
import kotlinx.coroutines.launch
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.scale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LihatSetoranScreen(navController: NavController, nim: String) {
    val context = LocalContext.current
    val viewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.getFactory(context))
    val setoranState by viewModel.setoranState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(nim) {
        if (nim.isNotBlank()) {
            viewModel.fetchSetoranMahasiswa(nim)
        }
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
                when (val state = setoranState) {
                    is SetoranState.Loading -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
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
                            val data = setoran.data
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                item {
                                    StyledSectionCard(title = "Info Mahasiswa") {
                                        ProfileItem(label = "Nama", value = data.info.nama)
                                        ProfileItem(label = "NIM", value = data.info.nim)
                                        ProfileItem(label = "Email", value = data.info.email)
                                        ProfileItem(label = "Angkatan", value = data.info.angkatan)
                                        ProfileItem(label = "Semester", value = data.info.semester.toString())
                                        ProfileItem(label = "Dosen PA", value = data.info.dosen_pa.nama)
                                    }
                                }
                                
                                item {
                                    EnhancedProgressCard(
                                        title = "Progres Setoran",
                                        progressPercentage = data.setoran.info_dasar.persentase_progres_setor.toFloat(),
                                        totalWajibSetor = data.setoran.info_dasar.total_wajib_setor,
                                        totalSudahSetor = data.setoran.info_dasar.total_sudah_setor,
                                        totalBelumSetor = data.setoran.info_dasar.total_belum_setor,
                                        terakhirSetor = data.setoran.info_dasar.terakhir_setor,
                                        tanggalTerakhirSetor = data.setoran.info_dasar.tgl_terakhir_setor?.take(10) ?: "-"
                                    )
                                }
                                
                                item {
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
                                }
                                item {
                                    Text(
                                        text = "Daftar Setoran:",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black
                                        ),
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                                items(data.setoran.detail) { item ->
                                    SetoranCard(
                                        nama = item.nama,
                                        label = item.label,
                                        sudahSetor = item.sudah_setor,
                                        tealColor = tealPrimary,
                                        tealPastelColor = tealPastel
                                    )
                                }
                            }
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
    )
}

@Composable
fun EnhancedProgressCard(
    title: String,
    progressPercentage: Float,
    totalWajibSetor: Int,
    totalSudahSetor: Int,
    totalBelumSetor: Int,
    terakhirSetor: String,
    tanggalTerakhirSetor: String
) {
    var selectedStatistic by remember { mutableStateOf<String?>(null) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Card title with gradient background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(tealPrimary, tealLight),
                            startX = 0f,
                            endX = 1000f
                        )
                    )
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Animated Circular Progress Indicator
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                AnimatedCircularProgressIndicator(
                    currentValue = progressPercentage,
                    maxValue = 100,
                    progressBackgroundColor = Color(0xFFE0F2F1),
                    progressIndicatorColor = when {
                        progressPercentage < 30 -> Color(0xFFE57373)
                        progressPercentage < 70 -> Color(0xFFFFB74D)
                        else -> Color(0xFF81C784)
                    },
                    modifier = Modifier.size(150.dp)
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${progressPercentage.toInt()}%",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = tealDark
                    )
                    Text(
                        text = "Selesai",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Interactive Statistic Boxes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InteractiveStatBox(
                    title = "Wajib Setor",
                    value = totalWajibSetor.toString(),
                    color = tealPastel,
                    isSelected = selectedStatistic == "wajib",
                    onClick = {
                        selectedStatistic = if (selectedStatistic == "wajib") null else "wajib"
                    }
                )
                InteractiveStatBox(
                    title = "Sudah Setor",
                    value = totalSudahSetor.toString(),
                    color = Color(0xFFE0F7FA),
                    isSelected = selectedStatistic == "sudah",
                    onClick = {
                        selectedStatistic = if (selectedStatistic == "sudah") null else "sudah"
                    }
                )
                InteractiveStatBox(
                    title = "Belum Setor",
                    value = totalBelumSetor.toString(),
                    color = Color(0xFFF5F5F5),
                    isSelected = selectedStatistic == "belum",
                    onClick = {
                        selectedStatistic = if (selectedStatistic == "belum") null else "belum"
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Animated Details Card (appears when statistic is selected)
            AnimatedVisibility(
                visible = selectedStatistic != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF9F9F9)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = when (selectedStatistic) {
                                "wajib" -> "Detail Setoran Wajib"
                                "sudah" -> "Detail Setoran Selesai"
                                "belum" -> "Detail Setoran Belum Selesai"
                                else -> ""
                            },
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = when (selectedStatistic) {
                                "wajib" -> "Total ayat/surat yang wajib disetor: $totalWajibSetor"
                                "sudah" -> "Total ayat/surat yang sudah disetor: $totalSudahSetor"
                                "belum" -> "Total ayat/surat yang belum disetor: $totalBelumSetor"
                                else -> ""
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Last setoran info with elevation
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF5F7F9)
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Terakhir Setor:",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium
                            )
                        )
                        Text(
                            text = terakhirSetor,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Tanggal:",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium
                            )
                        )
                        Text(
                            text = tanggalTerakhirSetor,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InteractiveStatBox(
    title: String,
    value: String,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    
    val animatedElevation by animateDpAsState(
        targetValue = when {
            isSelected -> 8.dp
            isHovered -> 4.dp
            else -> 1.dp
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    
    val animatedScale by animateFloatAsState(
        targetValue = when {
            isSelected -> 1.05f
            isHovered -> 1.02f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    
    Box(
        modifier = Modifier
            .width(100.dp)
            .height(80.dp)
            .scale(animatedScale)
            .shadow(
                elevation = animatedElevation,
                shape = RoundedCornerShape(12.dp)
            )
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) color.copy(alpha = 0.9f) else color
            )
            .clickable { onClick() }
            .hoverable(interactionSource)
            .indication(interactionSource, null)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = if (isSelected) Color.Black else Color.DarkGray
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = Color.DarkGray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun AnimatedCircularProgressIndicator(
    modifier: Modifier = Modifier,
    currentValue: Float,
    maxValue: Int,
    progressBackgroundColor: Color,
    progressIndicatorColor: Color
) {
    val animatedProgress = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()
    
    LaunchedEffect(currentValue) {
        coroutineScope.launch {
            animatedProgress.animateTo(
                targetValue = currentValue / maxValue.toFloat(),
                animationSpec = tween(
                    durationMillis = 1000,
                    easing = FastOutSlowInEasing
                )
            )
        }
    }
    
    // Shimmer effect
    val shimmerColors = listOf(
        progressIndicatorColor.copy(alpha = 0.3f),
        progressIndicatorColor.copy(alpha = 0.7f),
        progressIndicatorColor
    )
    
    val transition = rememberInfiniteTransition()
    val translateAnimation = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1500,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        )
    )

    Canvas(modifier = modifier) {
        // Background track
        drawArc(
            color = progressBackgroundColor,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(
                width = 12f,
                cap = StrokeCap.Round
            )
        )
        
        // Progress with shimmer effect
        val brush = Brush.linearGradient(
            colors = shimmerColors,
            start = Offset(translateAnimation.value - 1000f, 0f),
            end = Offset(translateAnimation.value, size.height)
        )
        
        // Progress arc
        drawArc(
            brush = brush,
            startAngle = -90f,
            sweepAngle = 360f * animatedProgress.value,
            useCenter = false,
            style = Stroke(
                width = 12f,
                cap = StrokeCap.Round
            )
        )
        
        // Small circles along the progress arc
        val radius = (size.minDimension - 12f) / 2
        val dotRadius = 4f
        val dotsCount = 8
        
        for (i in 0 until dotsCount) {
            val angle = Math.PI * 2 * i / dotsCount - Math.PI / 2
            val activeProgressAngle = Math.PI * 2 * animatedProgress.value
            
            val isActive = activeProgressAngle >= angle
            val dotColor = if (isActive) progressIndicatorColor else progressBackgroundColor.copy(alpha = 0.5f)
            
            val x = center.x + radius * kotlin.math.cos(angle).toFloat()
            val y = center.y + radius * kotlin.math.sin(angle).toFloat()
            
            drawCircle(
                color = dotColor,
                radius = dotRadius,
                center = Offset(x, y)
            )
        }
    }
}

@Composable
fun SetoranCard(
    nama: String,
    label: String,
    sudahSetor: Boolean,
    tealColor: Color,
    tealPastelColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
        ) {
            // Background setengah lingkaran di sebelah kanan
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .width(140.dp)
                    .clip(RoundedCornerShape(topStart = 80.dp, bottomStart = 80.dp))
                    .background(tealPastelColor)
            )

            // Konten kartu
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Nama surat
                    Text(
                        text = nama,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    )

                    // Row untuk Label dan Status
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Label KP dengan background teal muda
                        Box(
                            modifier = Modifier
                                .background(
                                    color = tealPastelColor,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = tealColor
                                )
                            )
                        }

                        // Status dengan ikon lingkaran
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Ikon lingkaran (CheckCircle untuk sudah setor, RadioButtonUnchecked untuk belum setor)
                            Icon(
                                imageVector = if (sudahSetor) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                                contentDescription = if (sudahSetor) "Sudah Setor" else "Belum Setor",
                                tint = if (sudahSetor) tealColor else Color.Red,
                                modifier = Modifier.size(16.dp)
                            )

                            // Teks status
                            Text(
                                text = if (sudahSetor) "Sudah Setor" else "Belum Setor",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (sudahSetor) tealColor else Color.Red,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }

                // Ikon More (titik tiga)
                IconButton(
                    onClick = { },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = "More",
                        tint = Color.Gray
                    )
                }
            }
        }
    }
}