package com.example.pathmasterproject.services

import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pathmasterproject.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import at.favre.lib.crypto.bcrypt.BCrypt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var googleSignInClient: GoogleSignInClient
    private val _profilePicture = MutableStateFlow<String?>(null)
    private val _favoritesCount = MutableStateFlow<Int>(0)
    val favoritesCount = _favoritesCount.asStateFlow()

    // Add this property to track connection status
    private val _isLoggedIn = MutableStateFlow(auth.currentUser != null)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    val currentUsername = MutableStateFlow<String?>(null)
    val currentRole = MutableStateFlow<String?>(null)
    val currentEmail = MutableStateFlow<String?>(null)
    val loginTimeInSeconds = MutableStateFlow<Long?>(null)
    val profilePicture: StateFlow<String?> get() = _profilePicture

    init {
        configureGoogleSignIn(application.applicationContext)

        // Add a listener to detect changes in authentication status
        auth.addAuthStateListener { firebaseAuth ->
            _isLoggedIn.value = firebaseAuth.currentUser != null
            if (firebaseAuth.currentUser != null) {
                fetchUserProfile()
            }
        }
    }

    // Function to configure google sign-in
    private fun configureGoogleSignIn(context: Context) {
        val clientId = context.getString(R.string.client_id)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(clientId)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(context, gso)
    }

    // Function to get google sign-in intent
    fun getGoogleSignInIntent(): Intent {
        return googleSignInClient.signInIntent
    }

    // Function to handle email/password sign-up and firestore
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
                            .addOnFailureListener { e -> onFailure("Firestore error: ${e.message}") }
                    }
                } else {
                    onFailure(task.exception?.message ?: "Registration failure")
                }
            }
    }

    // Function to handle email/password log-in and firestore
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
                                    onFailure("User not found in Firestore")
                                }
                            }
                            .addOnFailureListener { e -> onFailure("Connection failure: ${e.message}") }
                    }
                } else {
                    onFailure(task.exception?.message ?: "Connection failure")
                }
            }
    }

    // Function to handle google log-in
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
                                        .addOnFailureListener { e -> onFailure("Firestore error: ${e.message}") }
                                } else {
                                    onSuccess()
                                }
                            }
                            .addOnFailureListener { e -> onFailure("Firestore error: ${e.message}") }
                    }
                } else {
                    onFailure(task.exception?.message ?: "Connection failure with Google")
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

                // Check whether the favorites counter exists and create it if necessary
                if (!doc.contains("favorites_count")) {
                    updateUserField("favorites_count", 0L)
                }
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

    fun signOut(onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                // Disconnect from Firebase
                auth.signOut()

                // Disconnect from Google if necessary
                try {
                    googleSignInClient.signOut().await()
                } catch (e: Exception) {
                    Log.e("AuthViewModel", "Error signing out from Google", e)
                }

                // The authentication status listener will automatically update _isLoggedIn
                onComplete()
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error during signOut", e)
                onComplete()
            }
        }
    }

    fun fetchFavoritesCount() {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val count = document.getLong("favorites_count")?.toInt() ?: 0
                    _favoritesCount.value = count
                }
            }
    }
}