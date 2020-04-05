package db

import UserDTO
import java.sql.DriverManager
import java.util.*
import kotlin.collections.ArrayList

class UserQuery {
    fun getActiveUsers(): ArrayList<UserDTO> {
        val properties = Properties().apply {
            put("user", "pickaxe")
            put("password", "a_password")
        }

        val connect = DriverManager.getConnection("jdbc:postgresql://localhost:54320/pickaxe_dev", properties)

        val statement = connect.createStatement()

        val resultSet = statement.executeQuery("SELECT name FROM users WHERE active = TRUE")

        val results = ArrayList<UserDTO>(0)

        while (resultSet.next()) {
            results.add(UserDTO(resultSet.getString("name")))
        }

        return results
    }
}