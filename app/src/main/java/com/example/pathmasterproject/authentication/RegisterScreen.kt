package com.example.pathmasterproject.authentication

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.example.pathmasterproject.R
import com.example.pathmasterproject.navigation.Screen
import com.example.pathmasterproject.services.AuthViewModel

@Composable
fun RegisterScreen(navController: NavController, authViewModel: AuthViewModel) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    val context = LocalContext.current
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        handleGoogleSignInResult(data, authViewModel, navController, context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFD8D8D8))
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // register title
        Text(
            text = "Sign Up",
            color = Color(0xFF25356C),
            fontSize = 32.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // username field
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            placeholder = { Text("Username", color = Color(0xFF7889CF), fontSize = 14.sp) },
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(vertical = 8.dp)
                .height(48.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(autoCorrect = false),
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White
            )
        )

        // email field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = { Text("Email", color = Color(0xFF7889CF), fontSize = 14.sp) },
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(vertical = 8.dp)
                .height(48.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(autoCorrect = false),
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White
            )
        )

        // password field
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = { Text("Password", color = Color(0xFF7889CF), fontSize = 14.sp) },
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(vertical = 8.dp)
                .height(48.dp),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(autoCorrect = false),
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White
            )
        )

        // confirm password field
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            placeholder = { Text("Confirm Password", color = Color(0xFF7889CF), fontSize = 14.sp) },
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(vertical = 8.dp)
                .height(48.dp),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(autoCorrect = false),
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // button to confirm registration
        Button(
            onClick = {
            if (password == confirmPassword) {
                authViewModel.signUpWithEmail(email, password, username, {
                    Toast.makeText(context, "Account created !", Toast.LENGTH_SHORT).show()
                    navController.navigate(Screen.Login.route)
                }, {
                    message = it
                })
            } else {
                message = "Passwords don't match !"
            }
        }) {
            Text("Confirm")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // google Sign-Up Button
        Text(text = "Or sign up with Google", color = Color(0xFF7889CF))

        IconButton(
            onClick = {
            val signInIntent = authViewModel.getGoogleSignInIntent()
            googleSignInLauncher.launch(signInIntent)
        }) {
            Image(
                painter = painterResource(id = R.drawable.ic_google_logo),
                contentDescription = "Google Sign-Up")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // log-in link
        Text(
            text = "You already have an account? LogIn",
            color = Color(0xFF7889CF),
            fontSize = 14.sp,
            modifier = Modifier.clickable {
                navController.navigate(Screen.Login.route)
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = message, color = MaterialTheme.colorScheme.error)
    }
}