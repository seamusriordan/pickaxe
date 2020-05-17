package db

import dto.WeekDTO
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import java.sql.Connection

class WeeksQuery(private val connection: Connection) :DataFetcher<List<WeekDTO>> {
    fun get(): List<WeekDTO> {
        val statement = connection.createStatement()

        val resultSet = statement.executeQuery("SELECT name, week_type, week, week_order FROM weeks")

        val results = ArrayList<WeekDTO>(0)
        while(resultSet.next()){
            val week = WeekDTO(resultSet.getString("name")).apply {
                weekType = resultSet.getString("week_type")
                week = resultSet.getInt("week")
                weekOrder = resultSet.getInt("week_order")
            }
            results.add(week)
        }
        return results
    }

    override fun get(environment: DataFetchingEnvironment?): List<WeekDTO> {
        return get()
    }
}
