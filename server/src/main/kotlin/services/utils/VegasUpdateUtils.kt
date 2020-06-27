package services.utils

import db.CurrentWeekQuery
import db.GameMutator
import db.GamesQuery
import db.UpdatePickMutator
import dto.GameDTO
import dto.PickWithSpreadDTO
import services.VegasPicksApi
import services.utils.UpdateUtils.Companion.buildMutatorEnvironment
import services.utils.UpdateUtils.Companion.hasGameStartInXMinutes

class VegasUpdateUtils {
    companion object {
        private const val vegasUserName = "Vegas"

        fun updateVegasPicks(
            currentWeekQuery: CurrentWeekQuery,
            gamesQuery: GamesQuery,
            gameMutator: GameMutator,
            pickMutator: UpdatePickMutator,
            vegasPicksApi: VegasPicksApi
        ) {
            val currentWeekString = currentWeekQuery.getCurrentWeek().name

            vegasPicksApi.getVegasPicks().forEach { vegasPick ->
                updateVegasPickData(
                    currentWeekString,
                    gamesQuery,
                    pickMutator,
                    gameMutator,
                    vegasPick
                )
            }
        }

        private fun updateVegasPickData(
            currentWeekString: String,
            gamesQuery: GamesQuery,
            pickMutator: UpdatePickMutator,
            gameMutator: GameMutator,
            vegasPick: PickWithSpreadDTO
        ) {
            val gameInDbForVegasPick = findMatchingGameInDb(gamesQuery, currentWeekString, vegasPick)

            if (gameInDbForVegasPick != null) {
                updatePick(pickMutator, currentWeekString, gameInDbForVegasPick.name, vegasPick.pick)
                updateSpread(gameMutator, gameInDbForVegasPick, vegasPick.spread)
            }
        }

        private fun updateSpread(gameMutator: GameMutator, gameInDbForVegasPick: GameDTO, spread: Double) {
            val updatedGame = gameInDbForVegasPick.apply {
                this.spread = spread
            }
            gameMutator.putInDatabase(updatedGame)
        }

        private fun updatePick(
            pickMutator: UpdatePickMutator,
            currentWeekString: String,
            game: String,
            vegasPick: String
        ) {
            val env = buildMutatorEnvironment(
                vegasUserName,
                currentWeekString,
                game,
                vegasPick
            )
            pickMutator.get(env)
        }

        private fun findMatchingGameInDb(
            gamesQuery: GamesQuery,
            currentWeekString: String,
            vegasPick: PickWithSpreadDTO
        ): GameDTO? {
            return gamesQuery
                .getGamesForWeek(currentWeekString)
                .filter { gameIsNotSoon(it) }
                .firstOrNull { it.name == vegasPick.game }
        }

        private fun gameIsNotSoon(game: GameDTO): Boolean {
            return game.gameTime != null && !hasGameStartInXMinutes(game.gameTime, 15)
        }
    }
}