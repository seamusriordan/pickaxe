package db

import dto.GameDTO
import java.sql.Connection

class UpdateGame(private var connection: Connection) {
    fun putInDatabase(game: GameDTO) {
        val statement = connection.createStatement()

        val insertOrUpdateStatement = if(game.result != null ){
            buildInsertOrUpdateWithResult(game)
        } else {
            buildInsertOrUpdateWithoutResult(game)
        }

        statement.executeUpdate(insertOrUpdateStatement)
    }

    private fun buildInsertOrUpdateWithoutResult(game: GameDTO) =
        "INSERT INTO games VALUES ('${game.name}', '${game.week}', '${game.gameTime}', false, '${game.id}') " +
                "ON CONFLICT (name, week, gametime, final, id) DO UPDATE SET (gametime, final, id) = ('${game.gameTime}', false, ${game.id})"

    private fun buildInsertOrUpdateWithResult(game: GameDTO) =
        "INSERT INTO games VALUES ('${game.name}', '${game.week}', '${game.gameTime}', true, '${game.result}', '${game.id}') " +
                "ON CONFLICT (name, week, gametime, final, result, id) DO UPDATE SET (gametime, final, result, id) = ('${game.gameTime}', true, ${game.result}, ${game.id})"

}