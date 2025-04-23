package com.example.pathmasterproject.screens

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.pathmasterproject.services.Article
import androidx.compose.runtime.*
import com.example.pathmasterproject.services.ArticleViewModel
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.example.pathmasterproject.R
import android.net.Uri
import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.pathmasterproject.navigation.TopNavigationBar
import com.example.pathmasterproject.services.NotificationsViewModel
import com.example.pathmasterproject.services.TemporaryNotification
import kotlin.math.roundToInt

@Composable
fun LinksScreen(navController: NavController, viewModel: ArticleViewModel, notificationsViewModel: NotificationsViewModel) {
    val categories = viewModel.categories.collectAsState(initial = emptyList()).value
    var selectedCategory by remember { mutableStateOf("") }
    val articles = viewModel.articles.collectAsState(initial = emptyList()).value
    val searchResults = viewModel.searchResults.collectAsState(initial = emptyList()).value
    val searchQuery = viewModel.searchQuery.collectAsState().value
    val searchType = viewModel.searchType.collectAsState().value
    val context = LocalContext.current

    var fabBounds by remember { mutableStateOf(Rect()) }
    var isDragging by remember { mutableStateOf(false) }
    var draggedArticle by remember { mutableStateOf<Article?>(null) }

    // variable for the popup notification
    val newNotification by notificationsViewModel.newNotification.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchCategories()
    }

    LaunchedEffect(categories) {
        if (categories.isNotEmpty() && selectedCategory.isEmpty()) {
            selectedCategory = categories.first()
        }
    }

    LaunchedEffect(selectedCategory) {
        if (selectedCategory.isNotEmpty()) {
            viewModel.getArticlesByCategory(selectedCategory)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            TopNavigationBar(navController = navController, notificationsViewModel)

            Spacer(modifier = Modifier.height(26.dp))

            ArticleHeader(
                selectedCategory = selectedCategory,
                categories = categories,
                onCategorySelected = {
                    selectedCategory = it
                    viewModel.resetSearch()
                },
                searchQuery = searchQuery,
                onQueryChange = { query -> viewModel.updateSearchQuery(query) },
                searchType = searchType,
                onSearchTypeChange = { type -> viewModel.updateSearchType(type) },
                onClearSearch = { viewModel.resetSearch() }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Display a message if no results
            if (searchQuery.isNotEmpty() && searchResults.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No articles found for \"$searchQuery\"",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.Top
            ) {
                // Use search results instead of articles directly
                items(if (searchQuery.isEmpty()) articles else searchResults) { article ->
                    ExpandableArticleItem(
                        article = article,
                        isDragging = isDragging && draggedArticle == article,
                        onDragEnd = { droppedArticle, dropOffset ->
                            val isInFabArea = fabBounds.contains(
                                dropOffset.x.roundToInt(),
                                dropOffset.y.roundToInt()
                            )
                            if (isInFabArea) {
                                viewModel.addToFavorites(droppedArticle)
                                notificationsViewModel.addNotification(
                                    "Article added to favorites",
                                    droppedArticle.title
                                )

                                // Afficher une notification Android native
                                showNativeNotification(
                                    context,
                                    "Article added to favorites",
                                    droppedArticle.title
                                )
                            }
                            draggedArticle = null
                        },
                        onDragStart = {
                            isDragging = true
                            draggedArticle = article
                        },
                        onDragCancel = {
                            isDragging = false
                            draggedArticle = null
                        },
                        onDragStopped = {
                            isDragging = false
                            draggedArticle = null
                        }
                    )
                }
            }
        }

        FavoriteFloatingButton(
            onClick = { navController.navigate("favorites") },
            isHighlighted = isDragging,
            onFabPositioned = { fabBounds = it }
        )

        // Display temporary notification if a new notification is present
        if (newNotification != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 60.dp)
            ) {
                TemporaryNotification(notificationsViewModel)
            }
        }
    }
}

// Function to display an Android native notification
fun showNativeNotification(context: Context, title: String, content: String) {
    val notificationManager = ContextCompat.getSystemService(
        context,
        NotificationManager::class.java
    ) as NotificationManager

    // Create a notification channel (mandatory for Android 8.0+)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            "favorites_channel",
            "Favorites Notifications",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)
    }

    // Create the notification
    val notification = NotificationCompat.Builder(context, "favorites_channel")
        .setSmallIcon(R.drawable.ic_star)
        .setContentTitle(title)
        .setContentText(content)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)
        .build()

    // Display the notification
    notificationManager.notify(System.currentTimeMillis().toInt(), notification)
}

@Composable
fun ArticleHeader(
    selectedCategory: String,
    categories: List<String>,
    onCategorySelected: (String) -> Unit,
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    searchType: String,
    onSearchTypeChange: (String) -> Unit,
    onClearSearch: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Title with icon
        Text(
            text = "Research Articles",
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFF25356C)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Search bar
        SearchBar(
            searchQuery = searchQuery,
            onQueryChange = onQueryChange,
            searchType = searchType,
            onSearchTypeChange = onSearchTypeChange,
            onClearSearch = onClearSearch
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Category chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            items(categories) { category ->
                val selected = category == selectedCategory
                Button(
                    onClick = { onCategorySelected(category) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selected) Color(0xFF25356C) else Color(0xFF7889CF),
                        contentColor = if (selected) Color.White else Color.White
                    ),
                    shape = RoundedCornerShape(50),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 2.dp)
                ) {
                    Text(category)
                }
            }
        }

        Divider(thickness = 1.dp, color = Color(0xFFE0E0E0))
    }
}

@Composable
fun ExpandableArticleItem(
    article: Article,
    isDragging: Boolean,
    onDragEnd: (Article, Offset) -> Unit,
    onDragStart: () -> Unit,
    onDragCancel: () -> Unit,
    onDragStopped: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var offset by remember { mutableStateOf(Offset.Zero) }
    var lastPositionOnScreen by remember { mutableStateOf(Offset.Zero) }
    val layoutCoordinates = remember { mutableStateOf<androidx.compose.ui.layout.LayoutCoordinates?>(null) }
    val density = LocalDensity.current
    // Scale animation when dragging
    val scale by animateFloatAsState(if (offset != Offset.Zero) 1.05f else 1f)

    Box(
        modifier = Modifier
            .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
            .scale(scale)
            .zIndex(if (offset != Offset.Zero) 1f else 0f)
            .pointerInput(Unit) {
                detectDragGesturesAfterLongPress(
                    onDragStart = {
                        onDragStart()
                    },
                    onDragCancel = {
                        onDragCancel()
                        offset = Offset.Zero
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        offset += dragAmount
                        lastPositionOnScreen = change.position + offset
                    },
                    onDragEnd = {
                        onDragEnd(article, lastPositionOnScreen)
                        onDragStopped()
                        offset = Offset.Zero
                    }
                )
            }
            .onGloballyPositioned { coordinates ->
                // Store the layout coordinates for position calculations
                if (offset != Offset.Zero) {
                    coordinates.boundsInWindow().let { bounds ->
                        lastPositionOnScreen = Offset(
                            bounds.center.x,
                            bounds.center.y
                        )
                    }
                }
            }
            .padding(vertical = 4.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFECECEC)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_article),
                        contentDescription = "Article Icon",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = article.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = if (expanded) FontWeight.Bold else FontWeight.Normal
                            ),
                            maxLines = if (expanded) Int.MAX_VALUE else 2,
                            overflow = if (expanded) TextOverflow.Visible else TextOverflow.Ellipsis,
                            color = if (expanded) Color(0xFF25356C) else Color.Unspecified
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Category: ${article.category}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }

                    // Expand/reduce button is the only interactive element
                    TextButton(
                        onClick = { expanded = !expanded },
                        modifier = Modifier.padding(0.dp)
                    ) {
                        Text(
                            text = if (expanded) "Show less" else "Show more",
                            color = Color(0xFF7889CF),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                if (expanded) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = article.abstract,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.DarkGray
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (article.pdfUrl != null) {
                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(article.pdfUrl))
                                    context.startActivity(intent)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF1A73E8)
                                )
                            ) {
                                Text("📄 Download PDF")
                            }
                        }

                        if (article.website.isNotBlank()) {
                            TextButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(article.url))
                                    context.startActivity(intent)
                                }
                            ) {
                                Text("Visit Website", color = Color(0xFF1A73E8))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FavoriteFloatingButton(
    onClick: () -> Unit,
    isHighlighted: Boolean,
    onFabPositioned: (Rect) -> Unit
) {
    val density = LocalDensity.current
    val fabScale by animateFloatAsState(if (isHighlighted) 1.2f else 1f)
    val fabColor by animateColorAsState(
        if (isHighlighted) Color(0xFF8DA0EF) else Color.White,
        label = "fabColor"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.BottomEnd
    ){
        // Visual drop zone indicator when dragging
        if (isHighlighted) {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .background(Color(0x338DA0EF), CircleShape)
                    .align(Alignment.BottomEnd)
            )
        }

        FloatingActionButton(
            onClick = onClick,
            containerColor = fabColor,
            contentColor = Color.Unspecified,
            modifier = Modifier
                .size(56.dp)
                .scale(fabScale)
                .onGloballyPositioned { coordinates ->
                    val bounds = coordinates.boundsInWindow()
                    val rect = Rect()
                    rect.set(
                        (bounds.left - 20).roundToInt(),
                        (bounds.top - 20).roundToInt(),
                        (bounds.right + 20).roundToInt(),
                        (bounds.bottom + 20).roundToInt()
                    )
                    onFabPositioned(rect)
                }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_star),
                contentDescription = "Favorites",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// Search Bar component
@Composable
fun SearchBar(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    searchType: String,
    onSearchTypeChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .height(48.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Search icon
            Icon(
                painter = painterResource(id = R.drawable.ic_search),
                contentDescription = "Search",
                tint = Color(0xFF7889CF),
                modifier = Modifier.padding(start = 16.dp).size(20.dp)
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                // Search text field
                BasicTextField(
                    value = searchQuery,
                    onValueChange = onQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(
                        color = Color(0xFF25356C),
                        fontSize = 16.sp
                    ),
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        Box(modifier = Modifier.fillMaxWidth()) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search articles...",
                                    color = Color(0xFF9DA3B4),
                                    fontSize = 16.sp
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }

            // If the field is not empty, display a delete button
            if (searchQuery.isNotEmpty()) {
                IconButton(
                    onClick = onClearSearch,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_clear),
                        contentDescription = "Clear search",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Filter button with drop-down menu
            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_filter),
                        contentDescription = "Filter search",
                        tint = Color(0xFF25356C)
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Search by Title") },
                        onClick = {
                            onSearchTypeChange("title")
                            expanded = false
                        },
                        leadingIcon = {
                            if (searchType == "title") {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_check),
                                    contentDescription = null
                                )
                            }
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Search by Website") },
                        onClick = {
                            onSearchTypeChange("website")
                            expanded = false
                        },
                        leadingIcon = {
                            if (searchType == "website") {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_check),
                                    contentDescription = null
                                )
                            }
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Search All") },
                        onClick = {
                            onSearchTypeChange("all")
                            expanded = false
                        },
                        leadingIcon = {
                            if (searchType == "all") {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_check),
                                    contentDescription = null
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}