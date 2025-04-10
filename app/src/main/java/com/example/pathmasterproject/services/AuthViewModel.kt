package com.example.pathmasterproject.services

import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.pathmasterproject.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch
import at.favre.lib.crypto.bcrypt.BCrypt
import com.example.pathmasterproject.navigation.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var googleSignInClient: GoogleSignInClient
    private val _profilePicture = MutableStateFlow<String?>(null)

    val currentUsername = MutableStateFlow<String?>(null)
    val currentRole = MutableStateFlow<String?>(null)
    val currentEmail = MutableStateFlow<String?>(null)
    val loginTimeInSeconds = MutableStateFlow<Long?>(null)
    val profilePicture: StateFlow<String?> get() = _profilePicture

    init {
        configureGoogleSignIn(application.applicationContext)
    }

    // function to configure google sign-in
    private fun configureGoogleSignIn(context: Context) {
        val clientId = context.getString(R.string.client_id)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(clientId)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(context, gso)
    }

    // function to get google sign-in intent
    fun getGoogleSignInIntent(): Intent {
        return googleSignInClient.signInIntent
    }

    // function to handle email/password sign-up and firestore
    fun signUpWithEmail(
        email: String, password: String, username: String,
        onSuccess: () -> Unit, onFailure: (String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        val hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray())

                        val newUser = mapOf(
                            "username" to username,
                            "email" to email,
                            "password" to hashedPassword,
                            "role" to "user",
                            "loginTimestamp" to System.currentTimeMillis(),
                            "totalTime" to 0L
                        )

                        firestore.collection("users").document(it.uid).set(newUser)
                            .addOnSuccessListener { onSuccess() }
                            .addOnFailureListener { e -> onFailure("Erreur Firestore: ${e.message}") }
                    }
                } else {
                    onFailure(task.exception?.message ?: "Échec de l'inscription")
                }
            }
    }

    // function to handle email/password log-in and firestore
    fun signInWithEmail(
        email: String, password: String,
        onSuccess: (Map<String, Any>?) -> Unit, onFailure: (String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        firestore.collection("users").document(it.uid).get()
                            .addOnSuccessListener { document ->
                                if (document.exists()) {
                                    onSuccess(document.data)
                                } else {
                                    onFailure("Utilisateur introuvable dans Firestore")
                                }
                            }
                            .addOnFailureListener { e -> onFailure("Erreur Firestore: ${e.message}") }
                    }
                } else {
                    onFailure(task.exception?.message ?: "Échec de connexion")
                }
            }
    }

    // function to handle google log-in
    fun signInWithGoogle(idToken: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        val userId = it.uid
                        val email = it.email ?: ""
                        val username = it.displayName ?: "Google User"

                        firestore.collection("users").document(userId).get()
                            .addOnSuccessListener { document ->
                                if (!document.exists()) {
                                    val newUser = mapOf(
                                        "username" to username,
                                        "email" to email,
                                        "role" to "user",
                                        "loginTimestamp" to System.currentTimeMillis()
                                    )
                                    firestore.collection("users").document(userId).set(newUser)
                                        .addOnSuccessListener { onSuccess() }
                                        .addOnFailureListener { e -> onFailure("Erreur Firestore: ${e.message}") }
                                } else {
                                    onSuccess()
                                }
                            }
                            .addOnFailureListener { e -> onFailure("Erreur Firestore: ${e.message}") }
                    }
                } else {
                    onFailure(task.exception?.message ?: "Échec de connexion avec Google")
                }
            }
    }

    fun fetchUserProfile() {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { doc ->
                currentUsername.value = doc.getString("username")
                currentRole.value = doc.getString("role")
                currentEmail.value = doc.getString("email")
                _profilePicture.value = doc.getString("profilePicture")

                val loginTimestamp = doc.getLong("loginTimestamp") ?: 0L
                val now = System.currentTimeMillis()
                loginTimeInSeconds.value = (now - loginTimestamp) / 1000
            }
            .addOnFailureListener {
                currentUsername.value = null
                currentRole.value = null
                currentEmail.value = null
                loginTimeInSeconds.value = null
            }
    }

    fun setProfilePicture(value: String) {
        _profilePicture.value = value
    }

    fun updateUserField(field: String, value: Any) {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users")
            .document(userId)
            .update(field, value)
            .addOnSuccessListener {
                Log.d("AuthViewModel", "$field updated")
            }
            .addOnFailureListener { e ->
                Log.e("AuthViewModel", "Error updating $field", e)
            }
    }
}