package com.example.pathmasterproject.navigation

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.internal.composableLambda
import androidx.navigation.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pathmasterproject.R
import com.example.pathmasterproject.screens.HomeScreen
import com.example.pathmasterproject.authentication.LoginScreen
import com.example.pathmasterproject.authentication.RegisterScreen
import com.example.pathmasterproject.screens.LinksScreen
import com.example.pathmasterproject.screens.ProfileScreen
import com.example.pathmasterproject.screens.WelcomeScreen
import com.example.pathmasterproject.services.ArticleViewModel
import com.example.pathmasterproject.services.AuthViewModel

sealed class Screen(val route: String, @DrawableRes val iconResId: Int? = null) {
    data object Welcome : Screen("welcome")
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object Home : Screen("home")
    data object Profile : Screen("profile", R.drawable.ic_users)
    data object Links : Screen("links", R.drawable.ic_homes)
    data object Charts : Screen("charts", R.drawable.ic_charts)
}

@Composable
fun AppNavigation(
    navController : NavHostController,
    authViewModel: AuthViewModel,
    articleViewModel: ArticleViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Welcome.route
    ) {
        composable(Screen.Welcome.route) { WelcomeScreen(navController)}
        composable(Screen.Home.route) { HomeScreen(navController, authViewModel, articleViewModel) }
        composable(Screen.Login.route) { LoginScreen(navController, authViewModel)}
        composable(Screen.Register.route) { RegisterScreen(navController, authViewModel) }
        composable(Screen.Links.route) { LinksScreen(navController, articleViewModel) }
        composable(Screen.Profile.route) { ProfileScreen(navController, authViewModel) }
    }
}