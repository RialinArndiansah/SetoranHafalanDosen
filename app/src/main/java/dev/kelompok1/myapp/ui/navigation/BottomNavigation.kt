package dev.kelompok1.myapp.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import dev.kelompok1.myapp.R
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import dev.kelompok1.myapp.ui.Routes

// Warna Teal untuk seluruh aplikasi
val tealPrimary = Color(0xFF008B8B)    // Hijau kebiruan (teal)
val tealDark = Color(0xFF006666)       // Teal gelap
val tealLight = Color(0xFF00AEAE)      // Teal cerah
val tealPastel = Color(0xFFE0F7FA)     // Teal sangat muda

// Memoize the nav items to prevent unnecessary recreations
private val navItems = listOf(
    NavItem("Dashboard", R.drawable.home, Routes.DASHBOARD),
    NavItem("Kelola Setoran", R.drawable.note, Routes.KELOLA_SETORAN),
    NavItem("Mahasiswa", R.drawable.student, Routes.STUDENTS),
    NavItem("Profil", R.drawable.user, Routes.PROFILE)
)

@Composable
fun AppBottomNavigation(navController: NavController) {
    // Use remember to prevent recalculation of these values on recomposition
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    
    // Remember screen dimensions to prevent recalculation
    val screenHeight = remember { configuration.screenHeightDp.dp }
    val screenWidth = remember { configuration.screenWidthDp.dp }
    
    // Calculate dynamic sizes based on screen dimensions - slightly increased sizes
    val navHeight = remember { (screenHeight * 0.065f).coerceIn(52.dp, 65.dp) }
    val iconSize = remember { (screenWidth * 0.048f).coerceIn(18.dp, 24.dp) }
    val cornerRadius = remember { (screenWidth * 0.042f).coerceIn(14.dp, 20.dp) }
    val itemPadding = remember { (screenWidth * 0.018f).coerceIn(3.dp, 7.dp) }
    
    // Calculate font size based on screen width - slightly larger text
    val fontSize = remember { (screenWidth.value * 0.028f).coerceIn(9f, 13f).sp }
    
    // Get current route more efficiently using derivedStateOf
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute by remember {
        derivedStateOf { navBackStackEntry?.destination?.route }
    }
    
    // Create optimized navigation handler
    val navigateToRoute = remember<(String) -> Unit> {
        { route ->
            // This approach improves performance by preventing the creation of a new lambda for each nav item
            navController.navigate(route) {
                // Pop up to the start destination to avoid building up a large stack of destinations
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                // Avoid multiple copies of the same destination when reselecting the same item
                launchSingleTop = true
                // Restore state when reselecting a previously selected item
                restoreState = true
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 14.dp,
                    shape = RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius),
                    spotColor = tealPrimary.copy(alpha = 0.18f)
                ),
            color = Color.White,
            shape = RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius)
        ) {
            NavigationBar(
                containerColor = Color.White,
                contentColor = tealPrimary,
                tonalElevation = 0.dp,
                modifier = Modifier
                    .height(navHeight)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius))
            ) {
                // Use index as key for better performance with animations
                navItems.forEachIndexed { index, item ->
                    val isSelected = currentRoute == item.route
                    
                    // Use animateColorAsState with label for better performance
                    val iconColor by animateColorAsState(
                        targetValue = if (isSelected) tealPrimary else Color.Gray.copy(alpha = 0.6f),
                        label = "iconColor_${item.route}"
                    )
                    val backgroundColor by animateColorAsState(
                        targetValue = if (isSelected) tealPastel else Color.Transparent,
                        label = "backgroundColor_${item.route}"
                    )
                    
                    // Use key to prevent unnecessary recompositions
                    key(item.route) {
                        NavigationBarItem(
                            icon = {
                                Box(
                                    modifier = Modifier
                                        .size(iconSize * 1.15f)
                                        .clip(RoundedCornerShape(7.dp))
                                        .background(backgroundColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    when (item.label) {
                                        "Dashboard" -> {
                                            Icon(
                                                imageVector = Icons.Default.Home,
                                                contentDescription = item.label,
                                                modifier = Modifier.size(iconSize),
                                                tint = iconColor
                                            )
                                        }
                                        "Profil" -> {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = item.label,
                                                modifier = Modifier.size(iconSize),
                                                tint = iconColor
                                            )
                                        }
                                        else -> {
                                            Icon(
                                                painter = painterResource(item.icon),
                                                contentDescription = item.label,
                                                modifier = Modifier.size(iconSize),
                                                tint = iconColor
                                            )
                                        }
                                    }
                                }
                            },
                            label = {
                                Text(
                                    text = item.label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = fontSize
                                    ),
                                    color = iconColor,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            },
                            selected = isSelected,
                            onClick = {
                                if (!isSelected) {
                                    // Only navigate if this isn't the current route to prevent unnecessary recomposition
                                    navigateToRoute(item.route)
                                }
                            },
                            modifier = Modifier
                                .padding(vertical = itemPadding, horizontal = itemPadding)
                                .clip(RoundedCornerShape(9.dp))
                                .background(backgroundColor),
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = tealPrimary,
                                unselectedIconColor = Color.Gray.copy(alpha = 0.6f),
                                selectedTextColor = tealPrimary,
                                unselectedTextColor = Color.Gray.copy(alpha = 0.6f),
                                indicatorColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        }
    }
}

data class NavItem(
    val label: String,
    val icon: Int,
    val route: String
)