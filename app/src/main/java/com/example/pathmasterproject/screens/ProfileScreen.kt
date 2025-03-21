package com.example.pathmasterproject.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pathmasterproject.services.AuthViewModel

@Composable
fun ProfileScreen(authViewModel: AuthViewModel) {
    val user = authViewModel.auth.currentUser
    val username = user?.displayName ?: "Utilisateur inconnu"
    val email = user?.email ?: "Email non renseigné"

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Profile", fontSize = 24.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Username : $username", fontSize = 18.sp)
        Text(text = "Email : $email", fontSize = 18.sp)

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = {
            authViewModel.auth.signOut()
        }) {
            Text(text = "Disconnect")
        }
    }
}