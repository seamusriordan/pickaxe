package services.utils

import db.GameMutator
import db.GamesQuery
import db.WeeksQuery
import dto.GameDTO
import dto.WeekDTO
import services.NflApi
import java.io.FileNotFoundException

class GameUpdateUtils {
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
            games.forEach { baseGame ->
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

            if (UpdateUtils.gameStartedMoreThanXHoursAgo(baseGame.gameTime, 2) &&
                gameResultNotRecorded(baseGame)
            ) {
                updateGameDetails(
                    nflApi,
                    baseGame,
                    gameMutator
                )
            }
        }

        fun hasImmanentGamesMissingId(weeksQuery: WeeksQuery, gamesQuery: GamesQuery): Boolean {
            val weeks = weeksQuery.get()
            weeks.forEach { week ->
                if (weekHasImmanentGamesMissingId(
                        week.name,
                        gamesQuery
                    )
                ) {
                    return true
                }
            }
            return false
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

        private fun weekHasImmanentGamesMissingId(week: String, gamesQuery: GamesQuery): Boolean {
            return gamesQuery.getGamesForWeek(week).any {
                it.id == null && it.result == null &&
                        UpdateUtils.hasGameStartInXMinutes(it.gameTime, 15)
            }
        }
    }
}