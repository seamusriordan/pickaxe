package services.utils

import db.CurrentWeekQuery
import db.GamesQuery
import db.UpdatePickMutator
import db.UserPickQuery
import dto.*
import services.RandomPickSelector


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
            return picksQuery.getPicksForWeek(weekString)
                .firstOrNull { userPicks ->
                    userPicks.user.name == rngUserName
                } ?: UserPicksDTO(UserDTO(rngUserName))
        }

        private fun setRandomPickForGame(
            weekString: String,
            game: GameDTO,
            randomPick: String,
            userPickMutator: UpdatePickMutator
        ) {
            userPickMutator.updatePick(
                UserDTO(rngUserName),
                WeekDTO(weekString),
                PickDTO(game.name, randomPick)
            )
        }

        private fun isGameAlreadyPicked(game: GameDTO, rngPicks: UserPicksDTO): Boolean {
            val pickForGame = rngPicks.picks.firstOrNull { pick -> pick.game == game.name }
            return pickForGame != null && containsOneOfTheTeams(pickForGame.pick, game.name)
        }

        private fun containsOneOfTheTeams(pickForGame: String, game: String): Boolean {
            val teams = game.split("@")
            return teams.map { pickForGame.contains(it) }.contains(true)
        }
    }
}
