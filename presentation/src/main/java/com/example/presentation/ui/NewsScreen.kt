package com.example.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.models.News
import com.example.presentation.viewmodel.NewsViewModel
import kotlinx.coroutines.delay

@Composable
fun NewsScreen(newsViewModel: NewsViewModel) {
    val newsList by newsViewModel.news.collectAsState()

    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            newsViewModel.updateFirstNewsWithRandom()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(modifier = Modifier.weight(1f).padding(8.dp)) {
                NewsCard(news = newsList.getOrNull(0), onLikeClick = { newsViewModel.likeNews(newsList[0]) })
            }
            Box(modifier = Modifier.weight(1f).padding(8.dp)) {
                NewsCard(news = newsList.getOrNull(1), onLikeClick = { newsViewModel.likeNews(newsList[1]) })
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(modifier = Modifier.weight(1f).padding(8.dp)) {
                NewsCard(news = newsList.getOrNull(2), onLikeClick = { newsViewModel.likeNews(newsList[2]) })
            }
            Box(modifier = Modifier.weight(1f).padding(8.dp)) {
                NewsCard(news = newsList.getOrNull(3), onLikeClick = { newsViewModel.likeNews(newsList[3]) })
            }
        }
    }
}
@Composable
fun NewsCard(news: News?, onLikeClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        val gradient = Brush.verticalGradient(
            colors = listOf(Color(0xFF191970), Color(0xFF00BFFF))
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = gradient),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = news?.content ?: "Loading...",
                modifier = Modifier
                    .weight(0.85f)
                    .padding(16.dp),
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp),
                color = Color.Yellow
            )
            IconButton(
                onClick = onLikeClick,
                modifier = Modifier
                    .weight(0.15f)
                    .fillMaxWidth()
                    .padding(8.dp)
                    .background(Color.Red, shape = CircleShape)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = "Like",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${news?.likes ?: 0}",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp)
                    )
                }
            }
        }
    }
}