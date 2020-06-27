package services

import db.CurrentWeekQuery
import db.GameMutator
import db.GamesQuery
import db.UpdatePickMutator
import services.UpdateUtils.Companion.buildMutatorEnvironment
import services.UpdateUtils.Companion.hasGameStartInXMinutes

class VegasUpdateUtils {
    companion object {
        fun updateVegasPicks(
            currentWeekQuery: CurrentWeekQuery,
            gamesQuery: GamesQuery,
            gameMutator: GameMutator,
            pickMutator: UpdatePickMutator,
            vegasPicksApi: VegasPicksApi
        ) {
            val currentWeekString = currentWeekQuery.getCurrentWeek().name

            vegasPicksApi.getVegasPicks().forEach { vegasPick ->
                val gameInDbForVegasPick = gamesQuery
                    .getGamesForWeek(currentWeekString)
                    .filter {game ->
                        game.gameTime != null && !hasGameStartInXMinutes(game.gameTime, 15)
                    }
                    .firstOrNull { game ->
                        game.name == vegasPick.game
                    }

                if( gameInDbForVegasPick != null ) {
                    val env = buildMutatorEnvironment(
                        "Vegas",
                        currentWeekString,
                        gameInDbForVegasPick.name,
                        vegasPick.pick
                    )
                    pickMutator.get(env)

                    val updatedGame = gameInDbForVegasPick.apply {
                        spread = vegasPick.spread
                    }

                    gameMutator.putInDatabase(updatedGame)
                }
            }
        }
    }
}