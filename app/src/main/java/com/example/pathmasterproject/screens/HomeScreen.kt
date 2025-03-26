package com.example.pathmasterproject.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.pathmasterproject.navigation.Screen
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.navigation.compose.*
import com.example.pathmasterproject.navigation.BottomNavigationBar
import com.example.pathmasterproject.services.ArticleViewModel
import com.example.pathmasterproject.services.AuthViewModel

@Composable
fun HomeScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    articleViewModel: ArticleViewModel
) {
    val innerNavController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavigationBar(innerNavController) }
    ) { innerPadding ->
        NavHost(
            navController = innerNavController,
            startDestination = Screen.Links.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Profile.route) { ProfileScreen(authViewModel) }
            composable(Screen.Links.route) { LinksScreen(navController, articleViewModel) }
            composable(Screen.Charts.route) { ChartsScreen() }
        }
    }
}