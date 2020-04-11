package db

import dto.GameDTO
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import java.sql.Connection

class GamesQuery(private val connection: Connection): DataFetcher<List<GameDTO>> {
    override fun get(environment: DataFetchingEnvironment): List<GameDTO> {
        val statement = connection.createStatement()
        val week = environment.arguments["week"] as String

        val resultSet = statement.executeQuery("SELECT game, week, result, spread FROM games WHERE week = '$week'")

        val results = ArrayList<GameDTO>(0)

        while (resultSet.next()) {
            val game = GameDTO(resultSet.getString("game"), resultSet.getString("week"))
            game.result = resultSet.getString("result")
            if(resultSet.wasNull()){
                game.result = null
            }
            game.spread = resultSet.getDouble("spread")
            if(resultSet.wasNull()){
                game.spread = null
            }
            results.add(game)
        }

        return results
    }
}