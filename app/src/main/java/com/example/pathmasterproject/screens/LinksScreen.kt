package com.example.pathmasterproject.screens

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.example.pathmasterproject.R
import android.net.Uri
import androidx.compose.foundation.lazy.LazyRow
import com.example.pathmasterproject.navigation.TopNavigationBar

@Composable
fun LinksScreen(navController: NavController, viewModel: ArticleViewModel) {
    val categories = viewModel.categories.collectAsState(initial = emptyList()).value
    var selectedCategory by remember { mutableStateOf("") }
    val articles = viewModel.articles.collectAsState(initial = emptyList()).value

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

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopNavigationBar(navController = navController)

        Spacer(modifier = Modifier.height(26.dp))

        ArticleHeader(
            selectedCategory = selectedCategory,
            categories = categories,
            onCategorySelected = { selectedCategory = it }
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            items(articles) { article ->
                ExpandableArticleItem(article)
            }
        }
    }
}

@Composable
fun ArticleHeader(
    selectedCategory: String,
    categories: List<String>,
    onCategorySelected: (String) -> Unit
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
fun ExpandableArticleItem(article: Article) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .padding(16.dp),
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
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Category: ${article.category}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Go to article",
                    tint = Color.Gray
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = article.abstract,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray
                )

                Spacer(modifier = Modifier.height(12.dp))
                if (!article.pdfUrl.isNullOrBlank()) {
                    TextButton(onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(article.pdfUrl))
                        context.startActivity(intent)
                    }) {
                        Text(
                            text = "📄 Download PDF",
                            style = MaterialTheme.typography.labelLarge.copy(color = Color(0xFF1A73E8))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                if (article.website.isNotBlank()) {
                    Text(
                        text = "Source: ${article.website}",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}
