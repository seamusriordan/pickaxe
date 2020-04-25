package services

import dto.WeekDTO
import getEnvOrDefault
import java.net.URL

class ServiceRunner {
    fun start() {
        val nflApiRoot = getEnvOrDefault("NFL_API_ROOT", "http://nfl-wiremock:8080")
        val nflApi = NflApi(URL("${nflApiRoot}/v1/reroute"), URL(nflApiRoot))

        nflApi.accessToken

        val sampleWeek = WeekDTO("Week 6").apply {
            week = 6
            weekType = "REG"
        }
        val games = nflApi.getWeek(sampleWeek)
        games.map {
            println(it.name)
        }


        println(games.first().id)

        val game = nflApi.getGame(games.first())
        println(game.name)
        println(game.gameTime)
        println(game.result)

    }

}
