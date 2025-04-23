package com.example.pathmasterproject.services

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class ArticleViewModel : ViewModel() {
    private val repository = ArticleRepository()
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _allArticles = MutableStateFlow<List<Article>>(emptyList())
    val allArticles: StateFlow<List<Article>> = _allArticles

    private val _articles = MutableStateFlow<List<Article>>(emptyList())
    val articles: StateFlow<List<Article>> = _articles

    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories: StateFlow<List<String>> = _categories

    private val _favorites = MutableStateFlow<List<Article>>(emptyList())
    val favorites: StateFlow<List<Article>> = _favorites

    // Map to store the Firestore document IDs associated with each item
    private val articleDocumentIds = mutableMapOf<Article, String>()

    // Firestore earpiece for favorites
    private var favoritesListener: ListenerRegistration? = null

    // New property for search results
    private val _searchResults = MutableStateFlow<List<Article>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    // Search query status
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    // Search type (title, website, all)
    private val _searchType = MutableStateFlow("title")
    val searchType = _searchType.asStateFlow()

    suspend fun getAllArticlesFromAllCategories(): List<Article> {
        val allArticles = mutableListOf<Article>()
        for (category in _categories.value) {
            allArticles.addAll(repository.getArticles(category))
        }
        return allArticles.distinctBy { it.title + it.url }
    }

    // Update search query
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        performSearch()
    }

    // Update search type
    fun updateSearchType(type: String) {
        _searchType.value = type
        performSearch()
    }

    init {
        // Ensure that the headset is configured each time the user changes
        auth.addAuthStateListener { firebaseAuth ->
            if (firebaseAuth.currentUser != null) {
                setupRealtimeFavoritesListener()
            } else {
                favoritesListener?.remove()
                _favorites.value = emptyList()
            }
        }

        // If the user is already logged in, configure the headset immediately
        if (auth.currentUser != null) {
            setupRealtimeFavoritesListener()
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Cleaning the earpiece when the ViewModel is destroyed
        favoritesListener?.remove()
    }

    fun getArticlesByCategory(category: String) {
        viewModelScope.launch {
            _articles.value = repository.getArticles(category)
            _searchResults.value = _articles.value
        }
    }

    fun fetchCategories() {
        viewModelScope.launch {
            _categories.value = repository.getCategories()
        }
    }

    fun addToFavorites(article: Article) {
        if (!isInFavorites(article)) {
            viewModelScope.launch {
                saveToFirestore(article)
                updateFavoritesCount(1)
            }
        }
    }

    fun removeFromFavorites(article: Article) {
        if (isInFavorites(article)) {
            viewModelScope.launch {
                removeFromFirestore(article)
                updateFavoritesCount(-1)
            }
        }
    }

    // Utility method to check if an item is already bookmarked
    fun isInFavorites(article: Article): Boolean {
        return _favorites.value.any {
            it.title == article.title &&
                    it.url == article.url &&
                    it.category == article.category
        }
    }

    // Real-time listener for favorites
    private fun setupRealtimeFavoritesListener() {
        val userId = auth.currentUser?.uid ?: return

        // Delete old listener if any
        favoritesListener?.remove()

        try {
            favoritesListener = firestore.collection("users")
                .document(userId)
                .collection("favorites")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        println("Favorites listener error: ${error.message}")
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val favoritesList = snapshot.documents.mapNotNull { doc ->
                            try {
                                Article(
                                    title = doc.getString("title") ?: "",
                                    url = doc.getString("url") ?: "",
                                    abstract = doc.getString("abstract") ?: "",
                                    category = doc.getString("category") ?: "",
                                    website = doc.getString("website") ?: "",
                                    pdfUrl = doc.getString("pdfUrl")
                                )
                            } catch (e: Exception) {
                                println("Error converting a favorite document: ${e.message}")
                                null
                            }
                        }

                        _favorites.value = favoritesList
                        println("Loaded favorites: ${favoritesList.size}")
                    }
                }
        } catch (e: Exception) {
            println("Error configuring earpiece: ${e.message}")
        }
    }

    // Save a favorite to Firestore
    private suspend fun saveToFirestore(article: Article) {
        val userId = auth.currentUser?.uid ?: return

        try {
            // First check if an article with the same title already exists
            val existingArticles = firestore.collection("users")
                .document(userId)
                .collection("favorites")
                .whereEqualTo("title", article.title)
                .whereEqualTo("category", article.category)
                .get()
                .await()

            // If an identical item already exists, do not add it
            if (!existingArticles.isEmpty) {
                println("Item already bookmarked, addition ignored: ${article.title}")
                return
            }

            // Creating a Map object for Firestore
            val favoriteMap = hashMapOf(
                "title" to article.title,
                "url" to article.url,
                "abstract" to article.abstract,
                "category" to article.category,
                "website" to article.website,
                "pdfUrl" to (article.pdfUrl ?: ""),
                "timestamp" to System.currentTimeMillis()
            )

            // Generate a unique ID for this document
            val documentId = article.title.replace(" ", "_").take(20) + "_" + System.currentTimeMillis()

            // Save in Firestore
            firestore.collection("users")
                .document(userId)
                .collection("favorites")
                .document(documentId)
                .set(favoriteMap)
                .await()

            println("Favorite successfully added: ${article.title}")
        } catch (e: Exception) {
            println("Error saving favorite: ${e.message}")
        }
    }

    // Delete a favorite from Firestore
    private suspend fun removeFromFirestore(article: Article) {
        val userId = auth.currentUser?.uid ?: return

        try {
            // Find the document with the same title
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("favorites")
                .whereEqualTo("title", article.title)
                .get()
                .await()

            // Delete all documents found
            for (doc in snapshot.documents) {
                firestore.collection("users")
                    .document(userId)
                    .collection("favorites")
                    .document(doc.id)
                    .delete()
                    .await()
            }

            println("Favorite successfully deleted: ${article.title}")
        } catch (e: Exception) {
            println("Error deleting favorite: ${e.message}")
        }
    }

    // Update favorites counter in user document
    private suspend fun updateFavoritesCount(delta: Int) {
        val userId = auth.currentUser?.uid ?: return

        try {
            // Retrieve user document first
            val userDoc = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            // Get current number of favorites (or 0 if undefined)
            var currentCount = userDoc.getLong("favorites_count")?.toInt() ?: 0

            // If the field doesn't exist, create it first
            if (!userDoc.contains("favorites_count")) {
                firestore.collection("users")
                    .document(userId)
                    .update("favorites_count", 0)
                    .await()
                currentCount = 0
            }

            // Calculate new number (make sure it doesn't fall below 0)
            val newCount = maxOf(0, currentCount + delta)

            // Update document
            firestore.collection("users")
                .document(userId)
                .update("favorites_count", newCount)
                .await()

            println("Updated favorites counter: $newCount")
        } catch (e: Exception) {
            println("Error updating favorites counter: ${e.message}")
        }
    }

    // Search by type and query
    private fun performSearch() {
        val query = _searchQuery.value.lowercase().trim()

        // If the query is empty, display all items in the current category
        if (query.isEmpty()) {
            _searchResults.value = _articles.value
            return
        }

        // Filter articles by search type
        _searchResults.value = when (_searchType.value) {
            "title" -> {
                _articles.value.filter { it.title.lowercase().contains(query) }
            }
            "website" -> {
                _articles.value.filter { it.website.lowercase().contains(query) }
            }
            "all" -> {
                _articles.value.filter {
                    it.title.lowercase().contains(query) ||
                            it.website.lowercase().contains(query) ||
                            it.abstract.lowercase().contains(query) ||
                            it.category.lowercase().contains(query)
                }
            }
            else -> _articles.value
        }
    }

    // Reset search
    fun resetSearch() {
        _searchQuery.value = ""
        _searchResults.value = _articles.value
    }
}