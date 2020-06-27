package services

import db.*
import dto.GameDTO
import dto.UserPicksDTO
import graphql.schema.DataFetchingEnvironment
import services.UpdateUtils.Companion.buildMutatorEnvironment


class RngUpdateUtils {
    companion object {
        fun makeRngPicksForCurrentWeek(
            currentWeekQuery: CurrentWeekQuery,
            gamesQuery: GamesQuery,
            picksQuery: UserPickQuery,
            userPickMutator: UpdatePickMutator,
            RandomPickSelector: RandomPickSelector
        ) {
            val weekString = currentWeekQuery.getCurrentWeek().name
            val rngPicks = getRngPicksForWeek(picksQuery, weekString)

            gamesQuery.getGamesForWeek(weekString)
                .filter { game ->
                    !isGameAlreadyPicked(game, rngPicks)
                }
                .filter { game ->
                    game.gameTime != null && !UpdateUtils.hasGameStartInXMinutes(game.gameTime, 15)
                }
                .forEach { game ->
                    val randomPick = RandomPickSelector.chooseRandomFor(game.name)
                    setRandomPickForGame(weekString, game, randomPick, userPickMutator)
                }
        }



        private fun getRngPicksForWeek(picksQuery: UserPickQuery, weekString: String): UserPicksDTO {
            return picksQuery
                .getPicksForWeek(weekString)
                .first { userPicks -> userPicks.user.name == "RNG" }
        }

        private fun setRandomPickForGame(
            weekString: String,
            game: GameDTO,
            randomPick: String,
            userPickMutator: UpdatePickMutator
        ) {
            val env: DataFetchingEnvironment = buildMutatorEnvironment(
                "RNG",
                weekString,
                game.name,
                randomPick
            )
            userPickMutator.get(env)
        }



        private fun isGameAlreadyPicked(game: GameDTO, rngPicks: UserPicksDTO) =
            rngPicks.picks.map { pick -> pick.game }
                .contains(game.name)


    }
}