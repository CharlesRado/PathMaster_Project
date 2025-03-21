package com.example.pathmasterproject.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.ui.Modifier

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(Screen.Profile, Screen.Links, Screen.Charts)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = Color(0xFF753636),
        tonalElevation = 4.dp,
        modifier = Modifier.height(50.dp)
    ) {
        items.forEach { screen ->
            val selected = screen.route == currentRoute
            NavigationBarItem(
                icon = {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(vertical = 2.dp)
                    ) {
                        screen.iconResId?.let {
                            Image(
                                painter = painterResource(id = it),
                                contentDescription = screen.route,
                                modifier = Modifier.size(if (selected) 44.dp else 40.dp)
                            )
                        }
                    }
                },
                selected = selected,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(Screen.Links.route) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                alwaysShowLabel = false
            )
        }
    }
}