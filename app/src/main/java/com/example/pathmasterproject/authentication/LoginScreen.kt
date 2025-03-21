package com.example.pathmasterproject.authentication

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.TextStyle
import com.example.pathmasterproject.R
import com.example.pathmasterproject.navigation.Screen
import com.example.pathmasterproject.services.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException

@Composable
fun LoginScreen(navController: NavController, authViewModel: AuthViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
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
        // log-in title
        Text(
            text = "Log In",
            color = Color(0xFF25356C),
            fontSize = 32.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // email Field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = { Text("Email", color = Color(0xFF7889CF), fontSize = 14.sp) },
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(vertical = 8.dp)
                .height(48.dp),
            shape = RoundedCornerShape(24.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(autoCorrect = false),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White
            )
        )

        // password Field
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = { Text("Password", color = Color(0xFF7889CF), fontSize = 14.sp) },
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(vertical = 8.dp)
                .height(48.dp),
            textStyle = TextStyle(fontSize = 12.sp),
            shape = RoundedCornerShape(24.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(autoCorrect = false),
            visualTransformation = PasswordVisualTransformation(),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        // button to log-in into the application
        Button(onClick = {
            authViewModel.signInWithEmail(email, password, { userData ->
                Toast.makeText(context, "Welcome ${userData?.get("username")}!", Toast.LENGTH_SHORT).show()
                navController.navigate(Screen.Home.route)
            }, { errorMessage ->
                message = errorMessage
            })
        }) {
            Text("Log In")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // register link
        Text(
            text = "Don't have an account? SignUp",
            color = Color(0xFF7889CF),
            fontSize = 14.sp,
            modifier = Modifier.clickable {
                navController.navigate(Screen.Register.route)
            }
        )

        // google Sign-In Button
        IconButton(
            onClick = {
            val signInIntent = authViewModel.getGoogleSignInIntent()
            googleSignInLauncher.launch(signInIntent)
        }) {
            Image(
                painter = painterResource(id = R.drawable.ic_google_logo),
                contentDescription = "Google Log-IN")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = message, color = MaterialTheme.colorScheme.error)
    }
}

fun handleGoogleSignInResult(
    data: Intent?,
    authViewModel: AuthViewModel,
    navController: NavController,
    context: Context
) {
    val task = GoogleSignIn.getSignedInAccountFromIntent(data)
    try {
        val account = task.getResult(ApiException::class.java)
        val idToken = account?.idToken
        if (idToken != null) {
            authViewModel.signInWithGoogle(idToken, {
                Toast.makeText(context, "Google connection success !", Toast.LENGTH_SHORT).show()
                navController.navigate(Screen.Home.route)
            }, { errorMessage ->
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            })
        }
    } catch (e: ApiException) {
        Toast.makeText(context, "Google sign-in failed: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
