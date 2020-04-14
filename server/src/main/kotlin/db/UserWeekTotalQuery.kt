package db

import dto.UserDTO
import dto.UserWeekTotalDTO
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import java.sql.Connection

class UserWeekTotalQuery(val connection: Connection) : DataFetcher<List<UserWeekTotalDTO>> {
    override fun get(environment: DataFetchingEnvironment?): List<UserWeekTotalDTO> {
        val statement = connection.createStatement()

        val userResults = statement.executeQuery("SELECT name FROM users WHERE active = true")

        val results = ArrayList<UserWeekTotalDTO>(0)

        while(userResults.next()) {
            val user = UserDTO(userResults.getString("name"))
            results.add(UserWeekTotalDTO(user))
        }

        return results
    }
}