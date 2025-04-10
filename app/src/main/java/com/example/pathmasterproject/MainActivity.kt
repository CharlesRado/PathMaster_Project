package com.example.pathmasterproject

import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.compose.rememberNavController
import com.example.pathmasterproject.navigation.AppNavigation
import com.example.pathmasterproject.services.ArticleViewModel
import com.example.pathmasterproject.services.AuthViewModel
import com.example.pathmasterproject.ui.theme.PathMasterProjectTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.initialize

class MainActivity : ComponentActivity() {

    private var sessionStartTime: Long = 0
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Firebase.initialize(this) // Initialise Firebase

        val authViewModel = AuthViewModel(application)
        val articleViewModel = ArticleViewModel()
        sessionStartTime = SystemClock.elapsedRealtime()

        // Ajout d’un observer pour détecter l’arrêt de l’app
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
                    Log.d("TimeTracker", "Temps total mis à jour : +$elapsedTime secondes")
                }.addOnFailureListener {
                    Log.e("TimeTracker", "Erreur Firestore lors de l'enregistrement du temps", it)
                }
            }
        })

        setContent {
            val navController = rememberNavController()

            PathMasterProjectTheme {
                AppNavigation(navController, authViewModel, articleViewModel)
            }
        }
    }
}
