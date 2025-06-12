package dev.kelompok1.myapp.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Dashboard : Screen("dashboard")
    object LihatSetoran : Screen("lihat_setoran/{nim}") {
        fun createRoute(nim: String): String = "lihat_setoran/$nim"
    }
    object KelolaSetoran : Screen("kelola_setoran")
    object Students : Screen("students")
    object Profile : Screen("profile")
} 