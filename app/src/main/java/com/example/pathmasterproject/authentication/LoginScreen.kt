package com.example.pathmasterproject.authentication

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import com.example.pathmasterproject.R
import com.example.pathmasterproject.navigation.Screen
import com.example.pathmasterproject.services.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff

@Composable
fun LoginScreen(navController: NavController, authViewModel: AuthViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
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
        // Log-in title
        Text(
            text = "Log In",
            color = Color(0xFF25356C),
            fontSize = 32.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Email Field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = { Text("Email", color = Color(0xFF7889CF), fontSize = 14.sp) },
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(vertical = 8.dp)
                .height(55.dp),
            textStyle = TextStyle(fontSize = 16.sp),
            shape = RoundedCornerShape(24.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(autoCorrect = false, keyboardType = KeyboardType.Email),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White,
                unfocusedTextColor = Color(0xFF25356C),
                focusedTextColor = Color(0xFF25356C)
            )
        )

        // Password Field
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = { Text("Password", color = Color(0xFF7889CF), fontSize = 14.sp) },
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(vertical = 8.dp)
                .height(55.dp),
            textStyle = TextStyle(fontSize = 16.sp),
            shape = RoundedCornerShape(24.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(autoCorrect = false, keyboardType = KeyboardType.Password),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                        tint = Color(0xFF7889CF)
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White,
                unfocusedTextColor = Color(0xFF25356C),
                focusedTextColor = Color(0xFF25356C)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Forgot Password?",
            color = Color(0xFF7889CF),
            fontSize = 14.sp,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clickable {
                    navController.navigate(Screen.ForgotPassword.route)
                }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Button to log-in into the application
        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    message = "Please fill all fields"
                    return@Button
                }

                isLoading = true
                message = ""

                authViewModel.signInWithEmail(email, password, { userData ->
                    isLoading = false
                    Toast.makeText(context, "Welcome ${userData?.get("username")}!", Toast.LENGTH_SHORT).show()
                    navController.navigate(Screen.Home.route){
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }, { errorMessage ->
                    isLoading = false
                    Log.e("LoginError", errorMessage)
                    message = errorMessage
                })
            },
            modifier = Modifier
                .width(120.dp)
                .height(40.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF25356C)
            ),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text("Log In", fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Register link
        Text(
            text = "Don't have an account? SignUp",
            color = Color(0xFF7889CF),
            fontSize = 14.sp,
            modifier = Modifier.clickable {
                navController.navigate(Screen.Register.route)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Or sign in with:",
            color = Color(0xFF7889CF),
            fontSize = 14.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        // Google Sign-In Button
        IconButton(
            onClick = {
                val signInIntent = authViewModel.getGoogleSignInIntent()
                googleSignInLauncher.launch(signInIntent)
            }
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_google_logo),
                contentDescription = "Google Sign-In",
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

fun handleGoogleSignInResult(
    data: Intent?,
    authViewModel: AuthViewModel,
    navController: NavController,
    context: Context
) {
    try {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        val account = task.getResult(ApiException::class.java)
        val idToken = account?.idToken

        if (idToken != null) {
            authViewModel.signInWithGoogle(idToken, {
                Toast.makeText(context, "Google connection success!", Toast.LENGTH_SHORT).show()
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            }, { errorMessage ->
                Log.e("GoogleSignIn", "Error: $errorMessage")
                Toast.makeText(context, "Google sign-in error: $errorMessage", Toast.LENGTH_SHORT).show()
            })
        } else {
            Log.e("GoogleSignIn", "No ID token received")
            Toast.makeText(context, "Failed to get ID token", Toast.LENGTH_SHORT).show()
        }
    } catch (e: ApiException) {
        Log.e("GoogleSignIn", "Google sign-in failed: ${e.message}, code: ${e.statusCode}", e)
        Toast.makeText(context, "Google sign-in failed: ${e.statusCode}", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Log.e("GoogleSignIn", "Unexpected error during Google sign-in", e)
        Toast.makeText(context, "Unexpected error during Google sign-in", Toast.LENGTH_SHORT).show()
    }
}
