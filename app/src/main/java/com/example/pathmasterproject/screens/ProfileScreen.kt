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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import com.example.pathmasterproject.R
import coil.compose.rememberAsyncImagePainter
import com.example.pathmasterproject.navigation.TopNavigationBar
import com.example.pathmasterproject.utils.formatTime
import java.io.ByteArrayOutputStream

@Composable
fun ProfileScreen(navController: NavController, authViewModel: AuthViewModel) {
    val context = LocalContext.current
    var selectedImage by remember { mutableStateOf<Bitmap?>(null) }

    // user data
    val user = authViewModel.auth.currentUser
    val email by authViewModel.currentEmail.collectAsState()
    val username by authViewModel.currentUsername.collectAsState()
    val role by authViewModel.currentRole.collectAsState()
    val loginSeconds by authViewModel.loginTimeInSeconds.collectAsState()
    val profilePicture by authViewModel.profilePicture.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    // editable fields
    var expandedEmail by remember { mutableStateOf(false) }
    var expandedUsername by remember { mutableStateOf(false) }
    var newEmail by remember { mutableStateOf("") }
    var newUsername by remember { mutableStateOf("") }

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
        }
    }

    // Retrieve user data
    LaunchedEffect(Unit) {
        authViewModel.fetchUserProfile()
    }

    // Picture fallback
    val photoUrl = user?.photoUrl?.toString()
    val profileImage = rememberAsyncImagePainter(
        model = photoUrl ?: R.drawable.ic_user
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA)),
    ) {
        TopNavigationBar(navController = navController)

        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {


            Spacer(modifier = Modifier.height(32.dp))
            // User information (username)
            Text(
                text = username ?: "Unknown user",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 32.sp, fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Profile picture
            // Show selected image or fallback
            if (selectedImage != null) {
                Image(
                    bitmap = selectedImage!!.asImageBitmap(),
                    contentDescription = "Profile picture",
                    modifier = Modifier.size(100.dp).clip(CircleShape)
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
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.ic_user),
                    contentDescription = "Profile picture",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
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

            Spacer(modifier = Modifier.height(32.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.Start
            ) {

                InfoItem(R.drawable.ic_mail, "Email", email ?: "Not provided")
                InfoItem(R.drawable.ic_role, "Role", role ?: "Not defined")
                InfoItem(R.drawable.ic_time, "Connected since", formatTime(loginSeconds ?: 0))
                InfoItem(R.drawable.ic_done_article, "Articles consulted", "12")
            }

            Spacer(modifier = Modifier.height(64.dp))

            // Editable Fields Section
            ExpandableEditItem(
                label = "Edit Email",
                expanded = expandedEmail,
                value = newEmail,
                onValueChange = { newEmail = it },
                onToggleExpand = { expandedEmail = !expandedEmail },
                onSave = {
                    authViewModel.updateUserField("email", newEmail)
                    expandedEmail = false
                }
            )

            ExpandableEditItem(
                label = "Edit Username",
                expanded = expandedUsername,
                value = newUsername,
                onValueChange = { newUsername = it },
                onToggleExpand = { expandedUsername = !expandedUsername },
                onSave = {
                    authViewModel.updateUserField("username", newUsername)
                    expandedUsername = false
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 60.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_logout), // Replace with your actual disconnect image
                    contentDescription = "Logout",
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { showDialog = true },
                    colorFilter = ColorFilter.tint(Color.Red)
                )
            }
        }
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Confirm logout") },
                text = { Text("Are you sure you want to disconnect?") },
                confirmButton = {
                    TextButton(onClick = {
                        authViewModel.auth.signOut()
                        navController.navigate("login") {
                            popUpTo("home") { inclusive = true }
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

@Composable
fun ExpandableEditItem(
    label: String,
    expanded: Boolean,
    value: String,
    onValueChange: (String) -> Unit,
    onToggleExpand: () -> Unit,
    onSave: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleExpand() }
                .padding(vertical = 8.dp),
            fontWeight = FontWeight.Bold
        )
        if (expanded) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = { Text("New value") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onSave,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Save")
            }
        }
    }
}