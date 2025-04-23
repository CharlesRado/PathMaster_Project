package com.example.pathmasterproject

import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.compose.rememberNavController
import com.example.pathmasterproject.navigation.AppNavigation
import com.example.pathmasterproject.services.ArticleViewModel
import com.example.pathmasterproject.services.AuthViewModel
import com.example.pathmasterproject.services.NotificationsViewModel
import com.example.pathmasterproject.ui.theme.PathMasterProjectTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.initialize
import android.Manifest

class MainActivity : ComponentActivity() {

    private var sessionStartTime: Long = 0
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Firebase.initialize(this) // Initialize Firebase

        val authViewModel = AuthViewModel(application)
        val articleViewModel = ArticleViewModel()
        val notificationsViewModel = NotificationsViewModel()
        sessionStartTime = SystemClock.elapsedRealtime()

        // Requires POST_NOTIFICATIONS permission if Android 13+.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission()
        }

        // Add an observer to detect app shutdown
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStop(owner: LifecycleOwner) {
                super.onStop(owner)
                val elapsedTime = (SystemClock.elapsedRealtime() - sessionStartTime) / 1000
                val userId = auth.currentUser?.uid ?: return

                val userRef = firestore.collection("users").document(userId)
                firestore.runTransaction { transaction ->
                    val snapshot = transaction.get(userRef)
                    val oldTime = snapshot.getLong("totalTime") ?: 0
                    transaction.update(userRef, "totalTime", oldTime + elapsedTime)
                }.addOnSuccessListener {
                    Log.d("TimeTracker", "Total time updated: +$elapsedTime seconds")
                }.addOnFailureListener {
                    Log.e("TimeTracker", "Firestore error when recording time", it)
                }
            }
        })

        setContent {
            val navController = rememberNavController()

            PathMasterProjectTheme {
                AppNavigation(navController, authViewModel, articleViewModel, notificationsViewModel)
            }
        }
    }
    // Function for Android 13+ to request permission for notifications
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionLauncher = registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (!isGranted) {
                    Log.w("Permissions", "POST_NOTIFICATIONS permission denied")
                }
            }

            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
