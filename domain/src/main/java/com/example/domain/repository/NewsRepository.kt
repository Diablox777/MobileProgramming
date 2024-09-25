package com.example.domain.repository

import com.example.domain.models.News

interface NewsRepository {
    fun getAllNews(): List<News>
    fun getRandomNews(): News
    fun likeNews(news: News)
}
