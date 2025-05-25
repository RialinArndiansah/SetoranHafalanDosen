package dev.kelompok1.myapp.ui

import android.content.Context
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.kelompok1.myapp.ui.dashboard.DashboardScreen
import dev.kelompok1.myapp.ui.dashboard.KelolaSetoranScreen
import dev.kelompok1.myapp.ui.dashboard.LihatSetoranScreen
import dev.kelompok1.myapp.ui.login.LoginScreen
import dev.kelompok1.myapp.ui.login.LoginViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.kelompok1.myapp.ui.dashboard.MahasiswaScreen
import dev.kelompok1.myapp.ui.profile.ProfileScreen
import java.io.File

// Define routes as constants to avoid string typos
object Routes {
    const val LOGIN = "login"
    const val DASHBOARD = "dashboard"
    const val LIHAT_SETORAN = "lihat_setoran/{nim}"
    const val KELOLA_SETORAN = "kelola_setoran"
    const val STUDENTS = "students"
    const val PROFILE = "profile"
}

@Composable
fun SetupNavGraph(navController: NavHostController) {
    val context = LocalContext.current
    val loginViewModel: LoginViewModel = viewModel(factory = LoginViewModel.getFactory(context))
    
    // For efficient navigation between screens - remember callback functions
    val onLogoutCallback = remember {
        {
            loginViewModel.logout()
            clearProfilePhoto(context)
            navController.navigate(Routes.LOGIN) {
                popUpTo(Routes.DASHBOARD) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {
        composable(
            route = Routes.LOGIN,
            enterTransition = {
                fadeIn(
                    animationSpec = tween(200, easing = EaseOut)
                ) + slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(300, easing = EaseInOut)
                )
            },
            exitTransition = {
                fadeOut(
                    animationSpec = tween(200, easing = EaseOut)
                ) + slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(300, easing = EaseInOut)
                )
            }
        ) {
            LoginScreen(navController)
        }
        
        composable(
            route = Routes.DASHBOARD,
            enterTransition = { 
                fadeIn(animationSpec = tween(150)) 
            },
            exitTransition = { 
                fadeOut(animationSpec = tween(150)) 
            }
        ) {
            DashboardScreen(
                navController = navController,
                onLogout = onLogoutCallback
            )
        }
        
        composable(
            route = Routes.LIHAT_SETORAN,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(250, easing = EaseInOut)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(250, easing = EaseInOut)
                )
            }
        ) { backStackEntry ->
            val nim = backStackEntry.arguments?.getString("nim") ?: ""
            LihatSetoranScreen(navController, nim)
        }
        
        composable(
            route = Routes.KELOLA_SETORAN,
            enterTransition = { 
                fadeIn(animationSpec = tween(150)) 
            },
            exitTransition = { 
                fadeOut(animationSpec = tween(150)) 
            }
        ) {
            KelolaSetoranScreen(navController)
        }
        
        composable(
            route = Routes.STUDENTS,
            enterTransition = { 
                fadeIn(animationSpec = tween(150)) 
            },
            exitTransition = { 
                fadeOut(animationSpec = tween(150)) 
            }
        ) {
            MahasiswaScreen(navController)
        }
        
        composable(
            route = Routes.PROFILE,
            enterTransition = { 
                fadeIn(animationSpec = tween(150)) 
            },
            exitTransition = { 
                fadeOut(animationSpec = tween(150)) 
            }
        ) {
            ProfileScreen(
                navController = navController,
                onLogout = onLogoutCallback
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