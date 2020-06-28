package services

import db.*
import dto.WeekDTO
import getEnvOrDefault
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import services.utils.GameUpdateUtils.Companion.hasImmanentGamesMissingId
import services.utils.GameUpdateUtils.Companion.reloadGamesForWeek
import services.utils.GameUpdateUtils.Companion.updateDetailsForFinalGame
import services.utils.RngUpdateUtils
import services.utils.VegasUpdateUtils
import java.net.URL
import java.sql.Connection


class ServiceRunner {
    private val sixHours = 6 * 3600 * 1000L
    private val fiveMinutes = 5 * 60 * 1000L

    fun start() {
        val nflApiRoot = getEnvOrDefault("NFL_API_ROOT", "http://nfl-wiremock:8080")
        val nflApi = NflApi(URL("${nflApiRoot}/v1/reroute"), URL(nflApiRoot))

        val vegasPicksApiRoot = getEnvOrDefault("VEGAS_PICKS_URL", "http://nfl-wiremock:8080/nfl/odds/las-vegas/")
        val vegasPicksApi = VegasPicksApi(URL(vegasPicksApiRoot))

        val dbConnection = PickaxeDB().getDBConnection()

        GlobalScope.launch {
            while (true) {
                reloadAllWeeks(nflApi, dbConnection)
                updateGameDetailsForFinalGames(nflApi, dbConnection)
                makeRngPicksForCurrentWeek(dbConnection)
                updateVegasPicksForCurrentWeek(dbConnection, vegasPicksApi)
                delay(sixHours)
            }
        }

        GlobalScope.launch {
            while (true) {
                delay(fiveMinutes)
                if (hasImmanentGamesMissingId(WeeksQuery(dbConnection), GamesQuery(dbConnection))) {
                    reloadAllWeeks(nflApi, dbConnection)
                }
                updateGameDetailsForFinalGames(nflApi, dbConnection)
            }
        }
    }

    private fun updateGameDetailsForFinalGames(nflApi: NflApi, dbConnection: Connection) {
        WeeksQuery(dbConnection).get().forEach { week ->
            updateDetailsForFinalGamesInWeek(week, dbConnection, nflApi)
        }
    }

    private fun updateDetailsForFinalGamesInWeek(
        week: WeekDTO,
        dbConnection: Connection,
        nflApi: NflApi
    ) {
        return GamesQuery(dbConnection).getGamesForWeek(week.name).forEach { baseGame ->
            updateDetailsForFinalGame(baseGame, nflApi, GameMutator(dbConnection))
        }
    }

    private fun reloadAllWeeks(nflApi: NflApi, dbConnection: Connection) {
        WeeksQuery(dbConnection).get().forEach { week ->
            reloadGamesForWeek(week, nflApi, GameMutator(dbConnection))
        }
    }

    private fun makeRngPicksForCurrentWeek(dbConnection: Connection) {
        RngUpdateUtils.makeRngPicksForCurrentWeek(
            CurrentWeekQuery(WeeksQuery(dbConnection), GamesQuery(dbConnection)),
            GamesQuery(dbConnection),
            UserPickQuery(dbConnection),
            UpdatePickMutator(dbConnection),
            RandomPickSelector()
        )
    }

    private fun updateVegasPicksForCurrentWeek(dbConnection: Connection, vegasPicksApi: VegasPicksApi) {
        VegasUpdateUtils.updateVegasPicks(
            CurrentWeekQuery(WeeksQuery(dbConnection), GamesQuery(dbConnection)),
            GamesQuery(dbConnection),
            GameMutator(dbConnection),
            UpdatePickMutator(dbConnection),
            vegasPicksApi
        )
    }
}
