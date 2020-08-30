package db

import dto.UserDTO
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import java.sql.Connection
import kotlin.collections.ArrayList

class UserQuery(private val connection: Connection) : DataFetcher<List<UserDTO>> {
    override fun get(environment: DataFetchingEnvironment): List<UserDTO> {
        return getActiveUsers()
    }

    fun getActiveUsers(): List<UserDTO> {
        val statement = connection.createStatement()

        val resultSet = statement.executeQuery("SELECT name FROM users WHERE active = TRUE ORDER BY id")

        val results = ArrayList<UserDTO>(0)

        while (resultSet.next()) {
            results.add(UserDTO(resultSet.getString("name")))
        }

        return results
    }


}