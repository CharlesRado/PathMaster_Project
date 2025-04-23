package com.example.pathmasterproject.navigation

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.pathmasterproject.R
import com.example.pathmasterproject.services.NotificationsViewModel

@Composable
fun TopNavigationBar(navController: NavController, notificationsViewModel: NotificationsViewModel) {
    val hasUnread by notificationsViewModel.hasUnread.collectAsState()

    // Ajouter un log pour déboguer
    LaunchedEffect(hasUnread) {
        println("Status des notifications non lues: $hasUnread")
    }

    val notificationScale by animateFloatAsState(
        targetValue = if (hasUnread) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = ""
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { navController.navigate("settings") }) {
            Icon(
                painter = painterResource(id = R.drawable.ic_settings),
                contentDescription = "Settings",
                tint = Color.Unspecified,
                modifier = Modifier.size(28.dp)
            )
        }
        Box(
            contentAlignment = Alignment.TopEnd
        ) {
            IconButton(
                onClick = {
                    navController.navigate("notifications")
                    notificationsViewModel.markAsRead()
                }
            ) {
                val iconRes = if (hasUnread) {
                    R.drawable.ic_notifications_full
                } else {
                    R.drawable.ic_notifications
                }

                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = "Notifications",
                    tint = Color(0xFF7889CF),
                    modifier = Modifier
                        .size(28.dp)
                        .scale(notificationScale)
                )
            }

            // Remarque: Nous supprimons le point rouge car l'icône est colorée comme demandé
        }
    }
}
