package services

import dto.WeekDTO
import getEnvOrDefault
import java.net.URL

class ServiceRunner {
    fun start() {
        val nflApiRoot = getEnvOrDefault("NFL_API_ROOT", "http://nfl-wiremock:8080")
        NflApi(URL("${nflApiRoot}/v1/reroute"), URL(nflApiRoot)).accessToken

        val sampleWeek = WeekDTO("Week 6").apply {
            week = 1
            weekType = "REG"
        }
        val games = NflApi(URL("${nflApiRoot}/v1/reroute"), URL(nflApiRoot)).getWeek(sampleWeek)
        games.map {
            println(it.name)
        }
    }

}
