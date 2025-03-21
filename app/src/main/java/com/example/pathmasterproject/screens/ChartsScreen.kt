package com.example.pathmasterproject.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ChartsScreen() {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Charts", fontSize = 24.sp)

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "📊 Statistic 1: 75%")
        Text(text = "📊 Statistic 2: 50%")
        Text(text = "📊 Statistic 3: 90%")
    }
}