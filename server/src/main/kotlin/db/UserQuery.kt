package db

import UserDTO
import java.sql.Connection
import kotlin.collections.ArrayList

class UserQuery(private val connection: Connection) {
    fun getActiveUsers(): ArrayList<UserDTO> {
        val statement = connection.createStatement()

        val resultSet = statement.executeQuery("SELECT name FROM users WHERE active = TRUE")

        val results = ArrayList<UserDTO>(0)

        while (resultSet.next()) {
            results.add(UserDTO(resultSet.getString("name")))
        }

        return results
    }


}