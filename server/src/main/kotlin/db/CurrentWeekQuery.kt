package db

import dto.WeekDTO
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import java.sql.Connection

class CurrentWeekQuery(private val connection: Connection) : DataFetcher<WeekDTO> {
    override fun get(environment: DataFetchingEnvironment?): WeekDTO {
        val statement = connection.createStatement()

        val resultSet = statement.executeQuery("SELECT week FROM weeks")

        resultSet.next()
        return WeekDTO(resultSet.getString("week"))
    }
}
