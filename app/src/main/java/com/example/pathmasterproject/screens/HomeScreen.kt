package com.example.pathmasterproject.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.pathmasterproject.navigation.Screen
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.*
import com.example.pathmasterproject.navigation.BottomNavigationBar
import com.example.pathmasterproject.services.ArticleViewModel
import com.example.pathmasterproject.services.AuthViewModel
import com.example.pathmasterproject.services.NotificationsViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun HomeScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    articleViewModel: ArticleViewModel,
    notificationsViewModel: NotificationsViewModel
) {
    val innerNavController = rememberNavController()

    // Check if user is logged in
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    LaunchedEffect(isLoggedIn) {
        if (!isLoggedIn) {
            navController.navigate("login") {
                popUpTo(navController.graph.startDestinationId) {
                    inclusive = true
                }
            }
        }
    }

    Scaffold(
        bottomBar = { BottomNavigationBar(innerNavController) }
    ) { innerPadding ->
        NavHost(
            navController = innerNavController,
            startDestination = Screen.Links.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Profile.route) { ProfileScreen(navController, authViewModel, notificationsViewModel, articleViewModel) }
            composable(Screen.Links.route) { LinksScreen(navController, articleViewModel, notificationsViewModel) }
            composable(Screen.Charts.route) { ChartsScreen(navController, articleViewModel, notificationsViewModel) }
        }
    }
}