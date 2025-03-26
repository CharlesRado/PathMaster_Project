package com.example.pathmasterproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.example.pathmasterproject.navigation.AppNavigation
import com.example.pathmasterproject.services.ArticleViewModel
import com.example.pathmasterproject.services.AuthViewModel
import com.example.pathmasterproject.ui.theme.PathMasterProjectTheme
import com.google.firebase.Firebase
import com.google.firebase.initialize

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Firebase.initialize(this) // Initialise Firebase

        val authViewModel = AuthViewModel(application)
        val articleViewModel = ArticleViewModel()

        setContent {
            val navController = rememberNavController()

            PathMasterProjectTheme {
                AppNavigation(navController, authViewModel, articleViewModel)
            }
        }
    }
}
