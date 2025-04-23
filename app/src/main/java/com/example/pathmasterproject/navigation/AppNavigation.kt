package com.example.pathmasterproject.navigation

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.pathmasterproject.R
import com.example.pathmasterproject.authentication.ForgotPasswordScreen
import com.example.pathmasterproject.screens.HomeScreen
import com.example.pathmasterproject.authentication.LoginScreen
import com.example.pathmasterproject.authentication.RegisterScreen
import com.example.pathmasterproject.screens.FavoritesScreen
import com.example.pathmasterproject.screens.LinksScreen
import com.example.pathmasterproject.screens.NotificationScreen
import com.example.pathmasterproject.screens.ProfileScreen
import com.example.pathmasterproject.screens.SettingsScreen
import com.example.pathmasterproject.screens.WelcomeScreen
import com.example.pathmasterproject.services.ArticleViewModel
import com.example.pathmasterproject.services.AuthViewModel
import com.example.pathmasterproject.services.NotificationsViewModel

sealed class Screen(val route: String, @DrawableRes val iconResId: Int? = null) {
    data object Welcome : Screen("welcome")
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object Home : Screen("home")
    data object Profile : Screen("profile", R.drawable.ic_profile)
    data object Links : Screen("links", R.drawable.ic_home)
    data object Charts : Screen("charts", R.drawable.ic_charts)
    data object Favorites : Screen("favorites")
    data object Notifications : Screen("notifications")
    data object Settings : Screen("settings")
    data object ForgotPassword : Screen("forgot_password")
}

@Composable
fun AppNavigation(
    navController : NavHostController,
    authViewModel: AuthViewModel,
    articleViewModel: ArticleViewModel,
    notificationsViewModel: NotificationsViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Welcome.route
    ) {
        composable(Screen.Welcome.route) { WelcomeScreen(navController)}
        composable(Screen.Home.route) { HomeScreen(navController, authViewModel, articleViewModel, notificationsViewModel) }
        composable(Screen.Login.route) { LoginScreen(navController, authViewModel)}
        composable(Screen.Register.route) { RegisterScreen(navController, authViewModel) }
        composable(Screen.Links.route) { LinksScreen(navController, articleViewModel, notificationsViewModel) }
        composable(Screen.Profile.route) { ProfileScreen(navController, authViewModel, notificationsViewModel, articleViewModel) }
        composable(Screen.Favorites.route) { FavoritesScreen(navController, articleViewModel) }
        composable(Screen.Notifications.route) { NotificationScreen(navController,notificationsViewModel) }
        composable(Screen.Settings.route) { SettingsScreen() }
        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(navController, authViewModel)
        }
    }
}