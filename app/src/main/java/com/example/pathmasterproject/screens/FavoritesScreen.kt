package com.example.pathmasterproject.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavController
import com.example.pathmasterproject.R
import com.example.pathmasterproject.services.Article
import com.example.pathmasterproject.services.ArticleViewModel
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import android.graphics.Rect
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
import kotlin.math.roundToInt

@Composable
fun FavoritesScreen(navController: NavController, viewModel: ArticleViewModel) {
    val favorites by viewModel.favorites.collectAsState()
    val context = LocalContext.current

    // Variables pour le drag and drop
    var draggedArticle by remember { mutableStateOf<Article?>(null) }
    var isDragging by remember { mutableStateOf(false) }
    var removeFabBounds by remember { mutableStateOf(Rect()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_home),
                    contentDescription = "Back",
                    tint = Color(0xFF25356C),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "My Favorites",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF25356C)
            )

            Spacer(modifier = Modifier.weight(1f))

            Icon(
                painter = painterResource(id = R.drawable.ic_star),
                contentDescription = "Favorites",
                tint = Color(0xFFFFC107),
                modifier = Modifier.size(28.dp)
            )
        }

        Divider(thickness = 1.dp, color = Color(0xFFE0E0E0))
        Spacer(modifier = Modifier.height(16.dp))

        // No favorites message
        if (favorites.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_star),
                        contentDescription = "No favorites",
                        tint = Color.Gray,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No favorite articles yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { navController.navigate("links") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF7889CF)
                        )
                    ) {
                        Text("Browse Articles")
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                // List of favorite articles with scroll support
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(favorites) { article ->
                        DraggableFavoriteArticleCard(
                            article = article,
                            isDragging = isDragging && draggedArticle == article,
                            onDragStart = {
                                isDragging = true
                                draggedArticle = article
                            },
                            onDragCancel = {
                                isDragging = false
                                draggedArticle = null
                            },
                            onDragEnd = { droppedArticle, dropOffset ->
                                val isInRemoveFabArea = removeFabBounds.contains(
                                    dropOffset.x.roundToInt(),
                                    dropOffset.y.roundToInt()
                                )
                                if (isInRemoveFabArea) {
                                    viewModel.removeFromFavorites(droppedArticle)
                                }
                                draggedArticle = null
                                isDragging = false
                            },
                            onRemove = {
                                viewModel.removeFromFavorites(article)
                            }
                        )
                    }
                }

                // FAB to remove an item from favorites (drag and drop target)
                if (isDragging) {
                    FavoritesremoveButton(
                        isHighlighted = isDragging,
                        onFabPositioned = { removeFabBounds = it }
                    )
                }
            }
        }
    }
}

@Composable
fun DraggableFavoriteArticleCard(
    article: Article,
    isDragging: Boolean,
    onDragStart: () -> Unit,
    onDragCancel: () -> Unit,
    onDragEnd: (Article, Offset) -> Unit,
    onRemove: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var offset by remember { mutableStateOf(Offset.Zero) }
    var lastPositionOnScreen by remember { mutableStateOf(Offset.Zero) }

    // Slide scaling animation during sliding
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
                        offset = Offset.Zero
                    }
                )
            }
            .onGloballyPositioned { coordinates ->
                if (offset != Offset.Zero) {
                    coordinates.boundsInWindow().let { bounds ->
                        lastPositionOnScreen = Offset(
                            bounds.center.x,
                            bounds.center.y
                        )
                    }
                }
            }
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_article),
                        contentDescription = "Article",
                        modifier = Modifier.size(32.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = article.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Category: ${article.category}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }

                    IconButton(onClick = onRemove) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Remove from favorites",
                            tint = Color(0xFFE57373)
                        )
                    }
                }

                // Button to show/hide detailed content
                TextButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = if (expanded) "Show less" else "Show more",
                        color = Color(0xFF7889CF)
                    )
                }

                // Detailed content
                if (expanded) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = article.abstract,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.DarkGray
                    )

                    Spacer(modifier = Modifier.height(16.dp))

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
fun FavoritesremoveButton(
    isHighlighted: Boolean,
    onFabPositioned: (Rect) -> Unit
) {
    val fabScale by animateFloatAsState(if (isHighlighted) 1.2f else 1f)
    val fabColor by animateColorAsState(
        if (isHighlighted) Color(0xFFE57373) else Color.White,
        label = "fabColor"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        // Visual indication zone when the element is above the deletion zone
        if (isHighlighted) {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .background(Color(0x33E57373), CircleShape)
                    .align(Alignment.BottomEnd)
            )
        }

        FloatingActionButton(
            onClick = {}, // nothing to do here its just for the drag and drop
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
                imageVector = Icons.Default.Delete,
                contentDescription = "Remove from favorites",
                modifier = Modifier.size(24.dp),
                tint = Color(0xFFE57373)
            )
        }
    }
}