package db

import dto.UserWeekTotalDTO
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import java.sql.Connection

class UserWeekTotalQuery(val connection: Connection) : DataFetcher<List<UserWeekTotalDTO>> {
    override fun get(environment: DataFetchingEnvironment): List<UserWeekTotalDTO> {
        val week = environment.arguments["week"] as String
        val users = UserQuery(connection).getActiveUsers()
        val games = GamesQuery(connection).getGamesForWeek(week)
        val allUserPicks = UserPickQuery(connection).getPicksForWeek(week)

        val results = ArrayList<UserWeekTotalDTO>(0)

        for (user in users) {
            val weeklyTotal = UserWeekTotalDTO(user)

            val userPicks = allUserPicks.find { pick -> pick.user.name == user.name }

            userPicks?.picks?.forEach { pick ->
                val game = games.find { game -> game.name == pick.game }
                if (game?.result == pick.pick) {
                    weeklyTotal.games.add(game)
                }
            }

            results.add(weeklyTotal)
        }

        return results
    }
}