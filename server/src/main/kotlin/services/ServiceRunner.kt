package services

import db.PickaxeDB
import db.GameMutator
import dto.GameDTO
import dto.WeekDTO
import getEnvOrDefault
import java.io.FileNotFoundException
import java.net.URL

class ServiceRunner : Runnable {
    override fun run() {
        val nflApiRoot = getEnvOrDefault("NFL_API_ROOT", "http://nfl-wiremock:8080")
        val nflApi = NflApi(URL("${nflApiRoot}/v1/reroute"), URL(nflApiRoot))
        val gameMutator = GameMutator(PickaxeDB().getDBConnection())

        val sampleWeek = WeekDTO("Week 6").apply {
            week = 6
            weekType = "REG"
        }
        var games: List<GameDTO> = ArrayList(0)
        try {
            games = nflApi.getWeek(sampleWeek)
        } catch (e: FileNotFoundException) {
            println("Week ${sampleWeek.name} could not be fetched - ${e.message}")
        }

        games.map { baseGame ->
            gameMutator.putInDatabase(baseGame)
        }

        games.map { baseGame ->
            try {
                if(baseGame.id != null) {
                    val fetchedGame = nflApi.getGame(baseGame)
                    gameMutator.putInDatabase(fetchedGame)
                }
            } catch (e: FileNotFoundException) {
                println("Game ${baseGame.week} ${baseGame.name} could not be fetched - ${e.message}")
            }
        }

    }

}
