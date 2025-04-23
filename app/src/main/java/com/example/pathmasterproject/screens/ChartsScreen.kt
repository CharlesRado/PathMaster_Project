package com.example.pathmasterproject.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.pathmasterproject.navigation.TopNavigationBar
import com.example.pathmasterproject.services.Article
import com.example.pathmasterproject.services.ArticleViewModel
import com.example.pathmasterproject.services.NotificationsViewModel
import com.patrykandpatryk.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatryk.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatryk.vico.compose.chart.Chart
import com.patrykandpatryk.vico.compose.chart.column.columnChart
import com.patrykandpatryk.vico.core.axis.AxisPosition
import com.patrykandpatryk.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatryk.vico.core.entry.entryModelOf
import com.patrykandpatryk.vico.core.entry.FloatEntry
import kotlinx.coroutines.launch

@Composable
fun ChartsScreen(
    navController: NavController,
    articleViewModel: ArticleViewModel,
    notificationsViewModel: NotificationsViewModel
) {
    val categories by articleViewModel.categories.collectAsState(initial = emptyList())
    val articles by articleViewModel.articles.collectAsState(initial = emptyList())
    val favorites by articleViewModel.favorites.collectAsState()

    // Local state to store all items in all categories
    val allArticlesState = remember { mutableStateOf<List<Article>>(emptyList()) }
    val allArticles = allArticlesState.value

    // Scope for launching coroutines
    val coroutineScope = rememberCoroutineScope()

    // Colors for graphics (as Compose values)
    val chartColors = listOf(
        Color(0xFF7889CF),
        Color(0xFF25356C),
        Color(0xFF4CAF50),
        Color(0xFFFFC107),
        Color(0xFFE91E63),
        Color(0xFF03A9F4)
    )

    // Load all category articles
    LaunchedEffect(categories) {
        if (categories.isNotEmpty()) {
            coroutineScope.launch {
                val allArticlesList = articleViewModel.getAllArticlesFromAllCategories()
                println("DEBUG: Total unique articles: ${allArticlesList.size}")
                allArticlesState.value = allArticlesList
            }
        }
    }

    // Preparing data for graphics
    val categoryDistribution = remember(categories, allArticles) {
        categories.map { category ->
            category to allArticles.count { it.category == category }.toFloat()
        }.also {
            println("DEBUG: Distribution by category: $it")
        }
    }

    val favoritesDistribution = remember(favorites, categories) {
        val categoryCounts = favorites.groupBy { it.category }
            .mapValues { it.value.size.toFloat() }
        categories.map { category ->
            category to (categoryCounts[category] ?: 0f)
        }.also {
            println("DEBUG: Favorites distribution: $it")
        }
    }

    // X-axis formatter
    val bottomAxisValueFormatter = remember(categoryDistribution) {
        AxisValueFormatter<AxisPosition.Horizontal.Bottom> { x, _ ->
            val index = x.toInt()
            if (index >= 0 && index < categoryDistribution.size) {
                val name = categoryDistribution[index].first
                // Shorten long names
                if (name.length > 6) name.take(6) + "..." else name
            } else {
                ""
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp)
        ) {
            TopNavigationBar(navController = navController, notificationsViewModel)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Research Analytics",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF25356C),
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                // 1. Chart: Articles by category
                ChartCard(
                    title = "Articles by category",
                    description = "Distribution of articles in each category"
                ) {
                    if (categoryDistribution.isNotEmpty() && categoryDistribution.any { (_, count) -> count > 0 }) {
                        val categoryModel = entryModelOf(
                            categoryDistribution.mapIndexed { index, (_, count) ->
                                FloatEntry(index.toFloat(), count)
                            }
                        )

                        Chart(
                            chart = columnChart(),
                            model = categoryModel,
                            startAxis = rememberStartAxis(
                                valueFormatter = { value, _ -> "${value.toInt()}" }
                            ),
                            bottomAxis = rememberBottomAxis(
                                valueFormatter = bottomAxisValueFormatter
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                    } else {
                        // If No data available
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No data available", color = Color.Gray)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Chart: Favorites by category
                ChartCard(
                    title = "Your favorites by category",
                    description = "Distribution of your favorite articles"
                ) {
                    if (favoritesDistribution.isNotEmpty() && favoritesDistribution.any { (_, count) -> count > 0 }) {
                        val favoritesModel = entryModelOf(
                            favoritesDistribution.mapIndexed { index, (_, count) ->
                                FloatEntry(index.toFloat(), count)
                            }
                        )

                        Chart(
                            chart = columnChart(),
                            model = favoritesModel,
                            startAxis = rememberStartAxis(
                                valueFormatter = { value, _ -> "${value.toInt()}" }
                            ),
                            bottomAxis = rememberBottomAxis(
                                valueFormatter = bottomAxisValueFormatter
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                    } else {
                        // If no favorite available
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No favorite available", color = Color.Gray)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 3. General statistics
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "General statistics",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF25356C),
                            fontSize = 18.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        StatisticItemAnimated(
                            label = "Total articles",
                            value = allArticles.size.toString(),
                            color = Color(0xFF7889CF)
                        )

                        StatisticItemAnimated(
                            label = "Favorites articles",
                            value = favorites.size.toString(),
                            color = Color(0xFFFFC107)
                        )

                        StatisticItemAnimated(
                            label = "Recording rate",
                            value = if (allArticles.isNotEmpty()) {
                                "${((favorites.size.toFloat() / allArticles.size) * 100).toInt()}%"
                            } else "0%",
                            color = Color(0xFF4CAF50)
                        )

                        StatisticItemAnimated(
                            label = "Available categories",
                            value = categories.size.toString(),
                            color = Color(0xFFE91E63)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChartCard(
    title: String,
    description: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF25356C),
                fontSize = 18.sp
            )

            Text(
                text = description,
                color = Color.Gray,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            content()
        }
    }
}

@Composable
fun StatisticItemAnimated(label: String, value: String, color: Color) {
    // Extract numeric value
    val numericValue = value.replace("%", "").toFloatOrNull() ?: 0f

    // Animating value
    val animatedValue by animateFloatAsState(
        targetValue = numericValue,
        animationSpec = tween(
            durationMillis = 1500,
            easing = FastOutSlowInEasing
        ),
        label = "StatValue"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = Color.DarkGray,
            fontSize = 14.sp
        )

        Box(
            modifier = Modifier
                .background(color.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = if (value.contains("%"))
                    "${animatedValue.toInt()}%"
                else
                    "${animatedValue.toInt()}",
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}