package db

import dto.GameDTO
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import java.sql.Connection
import java.time.OffsetDateTime
import java.util.*
import kotlin.collections.ArrayList

class GamesQuery(private val connection: Connection): DataFetcher<List<GameDTO>> {
    override fun get(environment: DataFetchingEnvironment): List<GameDTO> {
        val week = environment.arguments["week"] as String

        return getGamesForWeek(week)
    }

    fun getGamesForWeek(week: String): List<GameDTO> {
        val results = ArrayList<GameDTO>(0)
        val statement = connection.createStatement()
        val resultSet = statement.executeQuery("SELECT game, week, id, gametime, result, spread FROM games WHERE week = '$week'")


        while (resultSet.next()) {
            val game = GameDTO(resultSet.getString("game"), resultSet.getString("week"))
            game.result = resultSet.getString("result")
            if (resultSet.wasNull()) {
                game.result = null
            }
            game.spread = resultSet.getDouble("spread")
            if (resultSet.wasNull()) {
                game.spread = null
            }
            game.gameTime = resultSet.getObject("gametime", OffsetDateTime::class.java)
            game.id = resultSet.getObject("id", UUID::class.java)

            results.add(game)
        }
        return results
    }
}