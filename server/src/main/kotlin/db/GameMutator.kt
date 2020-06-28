package db

import dto.GameDTO
import java.sql.Connection

class GameMutator(private var connection: Connection) {
    fun putInDatabase(game: GameDTO) {
        val statement = connection.createStatement()

        var insertOrUpdateStatement = buildInsertOrUpdateWithoutID(game)


        if (game.id != null) {
            insertOrUpdateStatement = if (game.result != null && game.result != "") {
                buildInsertOrUpdateWithResult(game)
            } else {
                buildWithoutResult(game)
            }
        }

        statement.executeUpdate(insertOrUpdateStatement)
    }

    private fun buildWithoutResult(game: GameDTO): String {
        return if (game.spread == null) {
            buildInsertOrUpdateWithoutResult(game)
        } else {
            buildInsertOrUpdateWithSpreadAndWithoutResult(game)
        }
    }

    private fun buildInsertOrUpdateWithoutID(game: GameDTO) =
        "INSERT INTO games(game, week, gametime, final) VALUES ('${game.name}', '${game.week}', '${game.gameTime}', false) " +
                "ON CONFLICT (game, week) DO UPDATE SET (gametime, final) = ('${game.gameTime}', false)"


    private fun buildInsertOrUpdateWithoutResult(game: GameDTO) =
        "INSERT INTO games(game, week, gametime, final, id) VALUES ('${game.name}', '${game.week}', '${game.gameTime}', false, '${game.id}') " +
                "ON CONFLICT (game, week) DO UPDATE SET (gametime, final, id) = ('${game.gameTime}', false, '${game.id}')"

    private fun buildInsertOrUpdateWithSpreadAndWithoutResult(game: GameDTO) =
        "INSERT INTO games(game, week, gametime, final, spread, id) VALUES ('${game.name}', '${game.week}', '${game.gameTime}', false, '${game.spread}', '${game.id}') " +
                "ON CONFLICT (game, week) DO UPDATE SET (gametime, final, spread, id) = ('${game.gameTime}', false, '${game.spread}', '${game.id}')"


    private fun buildInsertOrUpdateWithResult(game: GameDTO) =
        "INSERT INTO games(game, week, gametime, final, result, id) VALUES ('${game.name}', '${game.week}', '${game.gameTime}', true, '${game.result}', '${game.id}') " +
                "ON CONFLICT (game, week) DO UPDATE SET (gametime, final, result, id) = ('${game.gameTime}', true, '${game.result}', '${game.id}')"

}