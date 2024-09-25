package com.example.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.models.News
import com.example.domain.repository.NewsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NewsViewModel(
    private val repository: NewsRepository
) : ViewModel() {

    private val _news = MutableStateFlow<List<News>>(emptyList())
    val news: StateFlow<List<News>> = _news

    init {
        loadNews()
    }

    private fun loadNews() {
        viewModelScope.launch(Dispatchers.IO) {
            val allNews = repository.getAllNews()
            _news.value = allNews
        }
    }

    fun likeNews(news: News) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.likeNews(news)
            loadNews()  // Обновляем список новостей после лайка
        }
    }

    fun getRandomNews() {
        viewModelScope.launch(Dispatchers.IO) {
            val randomNews = repository.getRandomNews()
            _news.value = _news.value.map { if (it.id == randomNews.id) randomNews else it }
        }
    }
}
