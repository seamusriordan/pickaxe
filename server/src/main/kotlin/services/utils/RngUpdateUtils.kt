package services.utils

import db.*
import dto.GameDTO
import dto.UserPicksDTO
import graphql.schema.DataFetchingEnvironment
import services.RandomPickSelector
import services.utils.UpdateUtils.Companion.buildMutatorEnvironment


class RngUpdateUtils {
    companion object {
        private const val rngUserName = "RNG"

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
                    !isGameAlreadyPicked(game, rngPicks) && gameTimeIsNotSoon(game)
                }
                .forEach { game ->
                    setRandomPickForGame(
                        weekString,
                        game,
                        RandomPickSelector.chooseRandomFor(game.name),
                        userPickMutator
                    )
                }
        }

        private fun gameTimeIsNotSoon(game: GameDTO): Boolean {
            return game.gameTime != null && !UpdateUtils.hasGameStartInXMinutes(game.gameTime, 15)
        }

        private fun getRngPicksForWeek(picksQuery: UserPickQuery, weekString: String): UserPicksDTO {
            return picksQuery
                .getPicksForWeek(weekString)
                .first { userPicks -> userPicks.user.name == rngUserName }
        }

        private fun setRandomPickForGame(
            weekString: String,
            game: GameDTO,
            randomPick: String,
            userPickMutator: UpdatePickMutator
        ) {
            val env: DataFetchingEnvironment = buildMutatorEnvironment(
                rngUserName,
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