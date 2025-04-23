package com.example.pathmasterproject.services

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.util.Date
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

data class Notification(
    val notificationId: Long = System.currentTimeMillis(),
    val title: String,
    val content: String,
    val timestamp: Date = Date(),
    var isRead: Boolean = false
)

class NotificationsViewModel : ViewModel() {
    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications

    private val _hasUnread = MutableStateFlow(false)
    val hasUnread: StateFlow<Boolean> = _hasUnread

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Map to store the Firestore document IDs associated with each notification
    private val notificationDocumentIds = mutableMapOf<Notification, String>()

    // Firestore earpiece
    private var notificationsListener: ListenerRegistration? = null

    // Variable to indicate whether a new notification has been received
    private val _newNotification = MutableStateFlow<Notification?>(null)
    val newNotification: StateFlow<Notification?> = _newNotification

    // Initialize preferences
    init {
        // Ensure that the headset is configured each time the user changes
        auth.addAuthStateListener { firebaseAuth ->
            if (firebaseAuth.currentUser != null) {
                setupRealtimeNotificationsListener()
            } else {
                notificationsListener?.remove()
                _notifications.value = emptyList()
                _hasUnread.value = false
            }
        }

        // If the user is already logged in, configure the headset immediately
        if (auth.currentUser != null) {
            setupRealtimeNotificationsListener()
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Cleaning the earpiece when the ViewModel is destroyed
        notificationsListener?.remove()
    }

    fun addNotification(title: String, content: String) {
        val newNotification = Notification(
            notificationId = System.currentTimeMillis(),
            title = title,
            content = content
        )

        viewModelScope.launch {
            saveToFirestore(newNotification)
            // Trigger temporary display
            _newNotification.value = newNotification

            // Reset after 5 seconds
            kotlinx.coroutines.delay(5000)
            _newNotification.value = null
        }
    }

    fun markAsRead() {
        val userId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                // Retrieve all unread notifications
                val unreadNotificationsQuery = firestore.collection("users")
                    .document(userId)
                    .collection("notifications")
                    .whereEqualTo("isRead", false)
                    .get()
                    .await()

                val batch = firestore.batch()

                unreadNotificationsQuery.documents.forEach { doc ->
                    val docRef = firestore.collection("users")
                        .document(userId)
                        .collection("notifications")
                        .document(doc.id)

                    batch.update(docRef, "isRead", true)
                }

                batch.commit().await()
            } catch (e: Exception) {
                println("Error marking notifications as read: ${e.message}")
            }
        }
    }

    fun markNotificationAsRead(notificationId: Long) {
        val userId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                // Search notification with this ID
                val snapshot = firestore.collection("users")
                    .document(userId)
                    .collection("notifications")
                    .whereEqualTo("notificationId", notificationId)
                    .get()
                    .await()

                // Update all documents found
                for (doc in snapshot.documents) {
                    firestore.collection("users")
                        .document(userId)
                        .collection("notifications")
                        .document(doc.id)
                        .update("isRead", true)
                        .await()
                }
            } catch (e: Exception) {
                println("Error updating playback status: ${e.message}")
            }
        }
    }

    fun clearNewNotification() {
        _newNotification.value = null
    }

    private fun updateUnreadStatus() {
        _hasUnread.value = _notifications.value.any { !it.isRead }
    }

    // Real-time earpiece for notifications
    private fun setupRealtimeNotificationsListener() {
        val userId = auth.currentUser?.uid ?: return

        // Delete old listener if any
        notificationsListener?.remove()

        try {
            notificationsListener = firestore.collection("users")
                .document(userId)
                .collection("notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        println("Notification listener error: ${error.message}")
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val notificationsList = snapshot.documents.mapNotNull { doc ->
                            try {
                                val timestamp = when {
                                    doc.getTimestamp("timestamp") != null -> doc.getTimestamp("timestamp")!!.toDate()
                                    doc.getLong("timestamp") != null -> Date(doc.getLong("timestamp")!!)
                                    else -> Date()
                                }

                                Notification(
                                    notificationId = doc.getLong("notificationId") ?: System.currentTimeMillis(),
                                    title = doc.getString("title") ?: "",
                                    content = doc.getString("content") ?: "",
                                    timestamp = timestamp,
                                    isRead = doc.getBoolean("isRead") ?: false
                                )
                            } catch (e: Exception) {
                                println("Error converting a notification document: ${e.message}")
                                null
                            }
                        }

                        _notifications.value = notificationsList
                        updateUnreadStatus()
                        println("Loaded notifications: ${notificationsList.size}, unread: ${notificationsList.count { !it.isRead }}")
                    }
                }
        } catch (e: Exception) {
            println("Error when configuring headset: ${e.message}")
        }
    }

    // Save a notification in Firestore
    private suspend fun saveToFirestore(notification: Notification) {
        val userId = auth.currentUser?.uid ?: return

        try {
            val notificationMap = hashMapOf(
                "notificationId" to notification.notificationId,
                "title" to notification.title,
                "content" to notification.content,
                "timestamp" to notification.timestamp,
                "isRead" to notification.isRead
            )
            val documentId = "notification_" + notification.notificationId

            firestore.collection("users")
                .document(userId)
                .collection("notifications")
                .document(documentId)
                .set(notificationMap)
                .await()

            println("Notification successfully added: ${notification.title}")

            _newNotification.value = notification
        } catch (e: Exception) {
            println("Notification registration error: ${e.message}")
        }
    }

    // Update the read status of a notification
    private suspend fun updateReadStatusInFirestore(documentId: String, isRead: Boolean) {
        val userId = auth.currentUser?.uid ?: return

        try {
            firestore.collection("users")
                .document(userId)
                .collection("notifications")
                .document(documentId)
                .update("isRead", isRead)
                .await()

            println("Notification marked as read: $documentId")
        } catch (e: Exception) {
            println("Error updating playback status: ${e.message}")
        }
    }
}