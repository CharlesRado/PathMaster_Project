package com.example.pathmasterproject.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pathmasterproject.services.AuthViewModel
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import com.example.pathmasterproject.R
import coil.compose.rememberAsyncImagePainter
import com.example.pathmasterproject.navigation.TopNavigationBar
import com.example.pathmasterproject.services.NotificationsViewModel
import com.example.pathmasterproject.services.TemporaryNotification
import com.example.pathmasterproject.utils.formatTime
import java.io.ByteArrayOutputStream
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import com.example.pathmasterproject.services.ArticleViewModel

@Composable
fun ProfileScreen(navController: NavController, authViewModel: AuthViewModel, notificationsViewModel: NotificationsViewModel, articleViewModel: ArticleViewModel) {
    val context = LocalContext.current
    var selectedImage by remember { mutableStateOf<Bitmap?>(null) }

    // User data
    val user = authViewModel.auth.currentUser
    val email by authViewModel.currentEmail.collectAsState()
    val username by authViewModel.currentUsername.collectAsState()
    val role by authViewModel.currentRole.collectAsState()
    val loginSeconds by authViewModel.loginTimeInSeconds.collectAsState()
    val profilePicture by authViewModel.profilePicture.collectAsState()
    val favorites by articleViewModel.favorites.collectAsState()
    val favoritesCount = favorites.size
    var showDialog by remember { mutableStateOf(false) }
    val newNotification by notificationsViewModel.newNotification.collectAsState()

    // Editable fields
    var expandedEmail by remember { mutableStateOf(false) }
    var expandedUsername by remember { mutableStateOf(false) }
    var expandedLogout by remember { mutableStateOf(false) }
    var newEmail by remember(email) { mutableStateOf(email ?: "") }
    var newUsername by remember(username) { mutableStateOf(username ?: "") }

    // Launcher for image selection
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val bitmap = if (Build.VERSION.SDK_INT < 28) {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, it)
            } else {
                val source = ImageDecoder.createSource(context.contentResolver, it)
                ImageDecoder.decodeBitmap(source)
            }
            selectedImage = bitmap

            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            val base64String = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
            authViewModel.updateUserField("profilePicture", base64String)
            authViewModel.setProfilePicture(base64String)

            // Add notification for profile photo change
            notificationsViewModel.addNotification(
                "Updated Profile",
                "Your profile photo has been successfully updated"
            )
        }
    }

    // Retrieve user data
    LaunchedEffect(Unit) {
        authViewModel.fetchUserProfile()
        authViewModel.fetchFavoritesCount()
    }

    // Picture fallback
    val photoUrl = user?.photoUrl?.toString()
    val profileImage = rememberAsyncImagePainter(
        model = photoUrl ?: R.drawable.ic_profile
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA)),
    ) {
        TopNavigationBar(navController = navController, notificationsViewModel)

        // Main content with scroll
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 60.dp)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // User information (username)
            Text(
                text = username ?: "Unknown user",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 32.sp, fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Profile picture
            // Show selected image or fallback
            if (selectedImage != null) {
                Image(
                    bitmap = selectedImage!!.asImageBitmap(),
                    contentDescription = "Profile picture",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                )
            } else if (!profilePicture.isNullOrEmpty()) {
                val imageBytes = Base64.decode(profilePicture, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Profile picture",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.ic_profile),
                    contentDescription = "Profile picture",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Edit Picture",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = Color.Gray,
                modifier = Modifier.clickable {
                    launcher.launch("image/*")
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Information section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    InfoItem(R.drawable.ic_mail, "Email", email ?: "Not provided")
                    InfoItem(R.drawable.ic_role, "Role", role ?: "Not defined")
                    InfoItem(R.drawable.ic_time, "Connected since", formatTime(loginSeconds ?: 0))
                    InfoItem(R.drawable.ic_star_empty, "Favorite Articles", favoritesCount.toString())
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Editable Fields Section (email)
            ExpandableEditItem(
                label = "Edit Email",
                expanded = expandedEmail,
                value = newEmail,
                initialValue = email ?: "",
                onValueChange = { newEmail = it },
                onToggleExpand = {
                    expandedEmail = !expandedEmail
                    if (expandedEmail) {
                        expandedUsername = false
                        expandedLogout = false
                        newEmail = email ?: ""
                    }
                },
                onSave = {
                    if (newEmail.isNotEmpty() && newEmail != email) {
                        authViewModel.updateUserField("email", newEmail)
                        notificationsViewModel.addNotification(
                            "Updated email",
                            "Your email has been successfully updated"
                        )
                        expandedEmail = false
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Editable Fields Section (username)
            ExpandableEditItem(
                label = "Edit Username",
                expanded = expandedUsername,
                value = newUsername,
                initialValue = username ?: "",
                onValueChange = { newUsername = it },
                onToggleExpand = {
                    expandedUsername = !expandedUsername
                    if (expandedUsername) {
                        expandedEmail = false
                        expandedLogout = false
                        newUsername = username ?: ""
                    }
                },
                onSave = {
                    if (newUsername.isNotEmpty() && newUsername != username) {
                        authViewModel.updateUserField("username", newUsername)
                        notificationsViewModel.addNotification(
                            "Updated profile",
                            "Your username has been successfully updated"
                        )
                        expandedUsername = false
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Logout component added in consistent style
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (expandedLogout) Color(0xFFFFF0F0) else Color.White
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = if (expandedLogout) 4.dp else 1.dp
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedLogout = !expandedLogout },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_logout),
                                contentDescription = "Logout",
                                tint = Color(0xFFE57373),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Logout",
                                color = Color(0xFFE57373),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Icon(
                            painter = painterResource(
                                id = if (expandedLogout) R.drawable.ic_chevron_up else R.drawable.ic_chevron_down
                            ),
                            contentDescription = if (expandedLogout) "Collapse" else "Expand",
                            tint = Color(0xFFE57373)
                        )
                    }

                    AnimatedVisibility(
                        visible = expandedLogout,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                        ) {
                            Text(
                                text = "Are you sure you want to disconnect?",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.DarkGray
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                OutlinedButton(
                                    onClick = { expandedLogout = false },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = Color(0xFF25356C)
                                    ),
                                    border = BorderStroke(1.dp, Color(0xFFCCCCCC)),
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Text("Cancel")
                                }

                                Button(
                                    onClick = {
                                        showDialog = true
                                        expandedLogout = false
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFE57373),
                                        contentColor = Color.White // Texte en blanc
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Logout")
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
        if (newNotification != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 60.dp)
            ) {
                TemporaryNotification(notificationsViewModel)
            }
        }
    }
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Confirm logout") },
            text = { Text("Are you sure you want to disconnect?") },
            confirmButton = {
                TextButton(onClick = {
                    authViewModel.signOut {
                        navController.navigate("login") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                    showDialog = false
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun InfoItem(iconRes: Int, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = label,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "$label : ",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

// Logout component added in consistent style
@Composable
fun ExpandableEditItem(
    label: String,
    expanded: Boolean,
    value: String,
    initialValue: String,
    onValueChange: (String) -> Unit,
    onToggleExpand: () -> Unit,
    onSave: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (expanded) Color(0xFFF0F4FF) else Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (expanded) 4.dp else 1.dp
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = label,
                    color = Color(0xFF25356C),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )

                Icon(
                    painter = painterResource(
                        id = if (expanded) R.drawable.ic_chevron_up else R.drawable.ic_chevron_down
                    ),
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = Color(0xFF7889CF)
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    // Current value display
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Current:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = initialValue,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = Color(0xFF25356C)
                        )
                    }

                    // Input field with modern styling
                    OutlinedTextField(
                        value = value,
                        onValueChange = onValueChange,
                        label = { Text("New $label") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF7889CF),
                            unfocusedBorderColor = Color(0xFFCCCCCC),
                            focusedLabelColor = Color(0xFF7889CF),
                            unfocusedLabelColor = Color.Gray
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = onToggleExpand,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF25356C)
                            ),
                            border = BorderStroke(1.dp, Color(0xFFCCCCCC)),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = onSave,
                            enabled = value.isNotEmpty() && value != initialValue,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF7889CF),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}