package db

import dto.GameDTO
import java.sql.Connection

class GameMutator(private var connection: Connection) {
    fun putInDatabase(game: GameDTO) {
        val statement = connection.createStatement()

        var insertOrUpdateStatement = buildInsertOrUpdateWithoutID(game)


        if (game.id != null) {
            if (game.result != null) {
                insertOrUpdateStatement = buildInsertOrUpdateWithResult(game)
            } else {
                insertOrUpdateStatement = buildInsertOrUpdateWithoutResult(game)
            }
        }

        statement.executeUpdate(insertOrUpdateStatement)
    }

    private fun buildInsertOrUpdateWithoutID(game: GameDTO) =
        "INSERT INTO games(game, week, gametime, final) VALUES ('${game.name}', '${game.week}', '${game.gameTime}', false) " +
                "ON CONFLICT (game, week) DO UPDATE SET (gametime, final) = ('${game.gameTime}', false)"


    private fun buildInsertOrUpdateWithoutResult(game: GameDTO) =
        "INSERT INTO games(game, week, gametime, final, id) VALUES ('${game.name}', '${game.week}', '${game.gameTime}', false, '${game.id}') " +
                "ON CONFLICT (game, week) DO UPDATE SET (gametime, final, id) = ('${game.gameTime}', false, '${game.id}')"


    private fun buildInsertOrUpdateWithResult(game: GameDTO) =
        "INSERT INTO games(game, week, gametime, final, result, id) VALUES ('${game.name}', '${game.week}', '${game.gameTime}', true, '${game.result}', '${game.id}') " +
                "ON CONFLICT (game, week) DO UPDATE SET (gametime, final, result, id) = ('${game.gameTime}', true, '${game.result}', '${game.id}')"

}