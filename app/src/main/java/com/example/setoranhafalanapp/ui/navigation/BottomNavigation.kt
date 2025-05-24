package com.example.setoranhafalanapp.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.setoranhafalanapp.R

// Warna Teal untuk seluruh aplikasi
val tealPrimary = Color(0xFF008B8B)    // Hijau kebiruan (teal)
val tealDark = Color(0xFF006666)       // Teal gelap
val tealLight = Color(0xFF00AEAE)      // Teal cerah
val tealPastel = Color(0xFFE0F7FA)     // Teal sangat muda

@Composable
fun AppBottomNavigation(navController: NavController) {
    val navItems = listOf(
        NavItem("Dashboard", R.drawable.home, "dashboard"),
        NavItem("Kelola Setoran", R.drawable.note, "kelola_setoran"),
        NavItem("Mahasiswa", R.drawable.student, "students"),
        NavItem("Profil", R.drawable.user, "profile")
    )

    Surface(
        shadowElevation = 8.dp,
        color = Color.White,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        NavigationBar(
            containerColor = Color.White,
            contentColor = tealPrimary,
            tonalElevation = 0.dp,
            modifier = Modifier
                .height(80.dp)
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
        ) {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            navItems.forEach { item ->
                val isSelected = currentRoute == item.route
                val iconColor by animateColorAsState(
                    targetValue = if (isSelected) tealPrimary else Color.Gray.copy(alpha = 0.6f)
                )
                val backgroundColor by animateColorAsState(
                    targetValue = if (isSelected) tealPastel else Color.Transparent
                )
                NavigationBarItem(
                    icon = {
                        if (item.label == "Profil") {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = item.label,
                                modifier = Modifier.size(24.dp),
                                tint = iconColor
                            )
                        } else {
                            Icon(
                                painter = painterResource(item.icon),
                                contentDescription = item.label,
                                modifier = Modifier.size(24.dp),
                                tint = iconColor
                            )
                        }
                    },
                    label = {
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = iconColor
                        )
                    },
                    selected = isSelected,
                    onClick = {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    modifier = Modifier
                        .padding(vertical = 4.dp, horizontal = 2.dp)
                        .clip(RoundedCornerShape(12.dp))
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

data class NavItem(
    val label: String,
    val icon: Int,
    val route: String
)