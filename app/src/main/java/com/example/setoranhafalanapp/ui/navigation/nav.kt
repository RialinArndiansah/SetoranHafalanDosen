package com.example.setoranhafalanapp.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.setoranhafalanapp.ui.dashboard.DashboardScreen
import com.example.setoranhafalan.ui.dashboard.KelolaSetoranScreen
import com.example.setoranhafalan.ui.dashboard.LihatSetoranScreen
import com.example.setoranhafalanapp.ui.login.LoginScreen
import com.example.setoranhafalanapp.ui.login.LoginViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.setoranhafalan.ui.dashboard.MahasiswaScreen
import com.example.setoranhafalanapp.ui.profile.ProfileScreen
import java.io.File

@Composable
fun SetupNavGraph(navController: NavHostController) {
    val context = LocalContext.current
    val loginViewModel: LoginViewModel = viewModel(factory = LoginViewModel.getFactory(context))

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(navController)
        }
        composable("dashboard") {
            DashboardScreen(
                navController = navController,
                onLogout = {
                    loginViewModel.logout()
                    clearProfilePhoto(context)
                    navController.navigate("login") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                }
            )
        }
        composable("lihat_setoran/{nim}") { backStackEntry ->
            val nim = backStackEntry.arguments?.getString("nim") ?: ""
            LihatSetoranScreen(navController, nim)
        }
        composable("kelola_setoran") {
            KelolaSetoranScreen(navController)
        }
        composable("students") {
            MahasiswaScreen(navController)
        }
        composable("profile") {
            ProfileScreen(
                navController = navController,
                onLogout = {
                    loginViewModel.logout()
                    clearProfilePhoto(context)
                    navController.navigate("login") {
                        popUpTo("profile") { inclusive = true }
                    }
                }
            )
        }
    }
}

private fun clearProfilePhoto(context: Context) {
    val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    sharedPreferences.edit().remove("profile_photo_path").apply()
    val file = File(context.filesDir, "profile_photo.jpg")
    file.delete()
}