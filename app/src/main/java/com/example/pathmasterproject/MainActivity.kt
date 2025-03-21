package com.example.pathmasterproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.pathmasterproject.navigation.AppNavigation
import com.example.pathmasterproject.services.AuthViewModel
import com.example.pathmasterproject.ui.theme.PathMasterProjectTheme
import com.google.firebase.Firebase
import com.google.firebase.initialize

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Firebase.initialize(this) // Initialise Firebase

        val authViewModel = AuthViewModel(application)

        setContent {
            val navController = rememberNavController()
            AppNavigation(navController, authViewModel)
        }
    }
}
