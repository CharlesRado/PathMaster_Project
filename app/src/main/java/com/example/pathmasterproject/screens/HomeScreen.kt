package com.example.pathmasterproject.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.pathmasterproject.navigation.Screen
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.compose.*
import com.example.pathmasterproject.navigation.BottomNavigationBar
import com.example.pathmasterproject.services.AuthViewModel

@Composable
fun HomeScreen(authViewModel: AuthViewModel) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Links.route, // ✅ La page principale au démarrage
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Profile.route) { ProfileScreen(authViewModel) }
            composable(Screen.Links.route) { LinksScreen() }
            composable(Screen.Charts.route) { ChartsScreen() }
        }
    }
}