package services

import db.PickaxeDB
import db.GameMutator
import db.GamesQuery
import db.WeeksQuery
import dto.GameDTO
import dto.WeekDTO
import getEnvOrDefault
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.FileNotFoundException
import java.net.URL
import java.sql.Connection
import java.time.OffsetDateTime


class ServiceRunner {
    private val sixHours = 6 * 3600 * 1000L
    private val fiveMinutes = 5 * 60 * 1000L

    fun start() {
        val nflApiRoot = getEnvOrDefault("NFL_API_ROOT", "http://nfl-wiremock:8080")
        val nflApi = NflApi(URL("${nflApiRoot}/v1/reroute"), URL(nflApiRoot))
        val dbConnection = PickaxeDB().getDBConnection()

        GlobalScope.launch {
            while (true) {
                reloadAllWeeks(nflApi, dbConnection)
                updateGameDetailsForFinalGames(nflApi, dbConnection)
                delay(sixHours)
            }
        }

        GlobalScope.launch {
            while (true) {
                delay(fiveMinutes)
                updateGameDetailsForFinalGames(nflApi, dbConnection)
            }
        }
    }

    private fun updateGameDetailsForFinalGames(nflApi: NflApi, dbConnection: Connection) {
        WeeksQuery(dbConnection).get().map { week ->
            updateDetailsForFinalGamesInWeek(week, dbConnection, nflApi)
        }
    }

    private fun updateDetailsForFinalGamesInWeek(
        week: WeekDTO,
        dbConnection: Connection,
        nflApi: NflApi
    ): List<Unit> {
        return GamesQuery(dbConnection).getGamesForWeek(week.name).map { baseGame ->
            updateDetailsForFinalGame(baseGame, nflApi, GameMutator(dbConnection))
        }
    }

    private fun reloadAllWeeks(nflApi: NflApi, dbConnection: Connection) {
        WeeksQuery(dbConnection).get().map { week ->
            reloadGamesForWeek(week, nflApi, GameMutator(dbConnection))
        }
    }

    companion object {
        fun reloadGamesForWeek(
            week: WeekDTO,
            nflApi: NflApi,
            gameMutator: GameMutator
        ) {
            var games: List<GameDTO> = ArrayList(0)
            try {
                games = nflApi.getWeek(week)
            } catch (e: FileNotFoundException) {
                println("Week ${week.name} could not be fetched - ${e.message}")
            }
            games.map { baseGame ->
                gameMutator.putInDatabase(baseGame)
            }
        }

        fun updateDetailsForFinalGame(
            baseGame: GameDTO,
            nflApi: NflApi,
            gameMutator: GameMutator
        ) {
            if (baseGame.id == null) {
                return
            }

            if (gameStartedMoreThanHoursAgo(baseGame.gameTime, 2) &&
                gameResultNotRecorded(baseGame)
            ) {
                updateGameDetails(nflApi, baseGame, gameMutator)
            }
        }

        private fun gameResultNotRecorded(baseGame: GameDTO) = baseGame.result == null

        private fun updateGameDetails(nflApi: NflApi, baseGame: GameDTO, gameMutator: GameMutator) {
            try {
                val fetchedGame = nflApi.getGame(baseGame)
                gameMutator.putInDatabase(fetchedGame)
            } catch (e: FileNotFoundException) {
                println("Game ${baseGame.week} ${baseGame.name} could not be fetched - ${e.message}")
            }
        }

        private fun gameStartedMoreThanHoursAgo(gameTime: OffsetDateTime?, hoursAgo: Long): Boolean {
            return gameTime != null &&
                    OffsetDateTime.now().isAfter(gameTime.plusHours(hoursAgo))
        }
    }

}
