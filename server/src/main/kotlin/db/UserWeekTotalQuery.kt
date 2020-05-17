package db

import dto.*
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
            val picksForUser = allUserPicks.find(matchUserPicksForUser(user))
            val correctGamesForUser = getCorrectPicksForUser(user, picksForUser, games)
            results.add(correctGamesForUser)
        }

        return results
    }

    private fun getCorrectPicksForUser(
        user: UserDTO,
        userPicks: UserPicksDTO?,
        games: ArrayList<GameDTO>
    ): UserWeekTotalDTO {
        val correctPicks = UserWeekTotalDTO(user)
        userPicks?.picks?.forEach { userPick ->
            addGameForMatchingPick(correctPicks, games, userPick)
        }
        return correctPicks
    }

    private fun matchUserPicksForUser(user: UserDTO) = { pick: UserPicksDTO -> pick.user.name == user.name }

    private fun addGameForMatchingPick(
        correctPicks: UserWeekTotalDTO,
        games: ArrayList<GameDTO>,
        userPick: PickDTO
    ) {
        val game = games.find(gameForPick(userPick))
        if (gameMatchesPick(game, userPick.pick)) {
            correctPicks.games.add(game!!)
        }
    }

    private fun gameForPick(userPick: PickDTO) = { game: GameDTO -> game.name == userPick.game }

    private fun gameMatchesPick(game: GameDTO?, pick: String) =
        game != null && game.result.equals(pick.trim(), ignoreCase = true)
}