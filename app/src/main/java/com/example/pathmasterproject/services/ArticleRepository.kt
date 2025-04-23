package com.example.pathmasterproject.services

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ArticleRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val articlesCollection = firestore.collection("retrieved_articles")

    suspend fun getArticles(category: String): List<Article> {
        return try {
            val snapshot = articlesCollection.whereEqualTo("category", category).get().await()
            snapshot.documents.mapNotNull { it.toObject(Article::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getCategories(): List<String> {
        return try {
            val snapshot = articlesCollection.get().await()
            snapshot.documents.mapNotNull { it.getString("category") }.distinct()
        } catch (e: Exception) {
            emptyList()
        }
    }
}