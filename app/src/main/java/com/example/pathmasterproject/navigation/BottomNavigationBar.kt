package com.example.pathmasterproject.navigation

import androidx.compose.foundation.layout.height
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
        containerColor = Color(0xFF7889CF),
        tonalElevation = 4.dp,
        modifier = Modifier.height(55.dp)
    ) {
        items.forEach { screen ->
            val selected = screen.route == currentRoute
            NavigationBarItem(
                icon = {
                    screen.iconResId?.let {
                        Icon(
                            painter = painterResource(id = it),
                            contentDescription = screen.route,
                            modifier = Modifier.size(30.dp),
                            tint = if (selected) Color.White else Color(0xFFE0E0E0)
                        )
                    }
                },
                selected = selected,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(Screen.Links.route) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                alwaysShowLabel = false,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    unselectedIconColor = Color(0xFFE0E0E0),
                    indicatorColor = Color(0xFF25356C)
                )
            )
        }
    }
}