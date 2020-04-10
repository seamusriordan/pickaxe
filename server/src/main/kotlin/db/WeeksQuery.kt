package db

import dto.WeekDTO
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import java.sql.Connection

class WeeksQuery(private val connection: Connection) :DataFetcher<List<WeekDTO>> {
    override fun get(environment: DataFetchingEnvironment?): List<WeekDTO> {
        val statement = connection.createStatement()

        val resultSet = statement.executeQuery("SELECT week FROM weeks")

        val results = ArrayList<WeekDTO>(0)
        while(resultSet.next()){
            results.add(WeekDTO(resultSet.getString("week")))
        }
        return results
    }
}
