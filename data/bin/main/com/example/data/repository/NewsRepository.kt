package com.example.data.repository

import com.example.data.models.NewsDto
import com.example.domain.models.News
import com.example.domain.repository.NewsRepository

class NewsRepositoryImpl : NewsRepository {

    private val newsList = mutableListOf(
        NewsDto(1, "Ученые обнаружили планету, которая почти полностью покрыта водой!", 0),
        NewsDto(2, "Телескоп \"Джеймс Уэбб\" запечатлел невероятные снимки \"космических столбов\", где рождаются новые звезды.", 0),
        NewsDto(3, "Китай запустил первую в мире миссию по добыче лунного грунта!", 0),
        NewsDto(4, "Исследователи создали \"искусственную планету\", чтобы изучать климатические изменения.", 0),
        NewsDto(5, "Космический корабль \"Dragon\" успешно вернулся на Землю с экипажем астронавтов!", 0),
        NewsDto(6, "Ученые нашли доказательства того, что жизнь на Земле зародилась не на ней!", 0),
        NewsDto(7, "NASA готовит миссию по отправке на Марс дрона-геликоптера! Маленький вертолет будет исследовать поверхность красной планеты и искать следы древней жизни.", 0),
        NewsDto(8, "На орбите Земли появились новые спутники для мониторинга климата.", 0),
        NewsDto(9, "Ученые обнаружили новый тип сверхновой звезды! Это открытие позволит нам лучше понять процессы эволюции звезд.", 0),
        NewsDto(10, "Астрономы зафиксировали странные радиосигналы из космоса. Их происхождение пока не разгадано.", 0)
    )


    private fun NewsDto.toDomain(): News {
        return News(id, content, likes)
    }

    private fun News.toDto(): NewsDto? {
        return newsList.find { it.id == this.id }
    }

    override fun getAllNews(): List<News> {
        return newsList.map { it.toDomain() }
    }

    override fun getRandomNews(): News {
        return newsList.random().toDomain()
    }

    override fun likeNews(news: News) {
        news.toDto()?.let { it.likes++ }
    }
}
