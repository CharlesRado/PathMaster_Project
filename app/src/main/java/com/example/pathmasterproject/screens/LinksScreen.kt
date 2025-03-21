package com.example.pathmasterproject.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LinksScreen() {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Links", fontSize = 24.sp)

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "🔗 Google: https://www.google.com")
        Text(text = "🔗 GitHub: https://github.com")
        Text(text = "🔗 Firebase: https://firebase.google.com")
    }
}