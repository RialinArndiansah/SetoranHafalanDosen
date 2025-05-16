package com.example.setoranhafalanapp.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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

@Composable
fun AppBottomNavigation(navController: NavController) {
    val navItems = listOf(
        NavItem("Dashboard", R.drawable.home, "dashboard"),
        NavItem("Lihat Setoran", R.drawable.task, "lihat_setoran"),
        NavItem("Kelola Setoran", R.drawable.note, "kelola_setoran"),
        NavItem("Mahasiswa PA", R.drawable.student, "students")
    )

    NavigationBar(
        containerColor = Color(0xFFFAFAFA),
        contentColor = Color.White,
        tonalElevation = 8.dp,
        modifier = Modifier
            .height(65.dp)
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        navItems.forEach { item ->
            val isSelected = currentRoute == item.route
            val labelColor by animateColorAsState(
                targetValue = if (isSelected) Color.White else Color.Gray
            )
            val backgroundColor by animateColorAsState(
                targetValue = if (isSelected) Color.White.copy(alpha = 0.1f) else Color.Transparent
            )
            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(item.icon),
                        contentDescription = item.label,
                        modifier = Modifier.size(24.dp),
                        tint = Color.Unspecified // gunakan warna asli dari drawable
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = labelColor
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
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(backgroundColor)
            )
        }
    }
}

data class NavItem(
    val label: String,
    val icon: Int,
    val route: String
)
