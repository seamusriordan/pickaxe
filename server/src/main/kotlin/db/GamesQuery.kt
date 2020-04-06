package db

import dto.GameDTO
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import java.sql.Connection

class GamesQuery(private val connection: Connection): DataFetcher<List<GameDTO>> {
    override fun get(environment: DataFetchingEnvironment?): List<GameDTO> {
        val statement = connection.createStatement()

        val resultSet = statement.executeQuery("SELECT game, week, result, spread FROM games WHERE week = '0'")

        val results = ArrayList<GameDTO>(0)

        while (resultSet.next()) {
            val game = GameDTO(resultSet.getString("game"))
            game.result = resultSet.getString("result")
            game.week = resultSet.getString("week")
            game.spread = resultSet.getFloat("spread")
            results.add(game)
        }

        return results
    }
}