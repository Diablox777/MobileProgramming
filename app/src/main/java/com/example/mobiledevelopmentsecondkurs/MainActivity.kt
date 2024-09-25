package com.example.mobiledevelopmentsecondkurs

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.data.repository.NewsRepositoryImpl
import com.example.mobiledevelopmentsecondkurs.ui.theme.MobileDevelopmentSecondKursTheme
import com.example.presentation.ui.NewsScreen
import com.example.presentation.viewmodel.NewsViewModel
import com.example.presentation.viewmodel.NewsViewModelFactory

class MainActivity : ComponentActivity() {

    private val newsViewModel: NewsViewModel by viewModels {
        NewsViewModelFactory(NewsRepositoryImpl())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MobileDevelopmentSecondKursTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NewsScreen(newsViewModel = newsViewModel)
                }
            }
        }
    }
}
