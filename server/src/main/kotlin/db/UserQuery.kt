package db

import UserDTO
import java.sql.DriverManager
import java.util.*
import kotlin.collections.ArrayList

class UserQuery {
    fun getActiveUsers(): ArrayList<UserDTO> {
        val properties = Properties().apply {
            put("user", "postgres")
            put("password", "pword")
        }

        var port = System.getenv("POSTGRES_PORT")
        if( port == null ) {
            port = "54320"
        }

        var host = System.getenv("POSTGRES_HOST")
        if( host == null ) {
            host = "localhost"
        }

        val connect = DriverManager.getConnection("jdbc:postgresql://$host:$port/pickaxe_dev", properties)

        val statement = connect.createStatement()

        val resultSet = statement.executeQuery("SELECT name FROM users WHERE active = TRUE")

        val results = ArrayList<UserDTO>(0)

        while (resultSet.next()) {
            results.add(UserDTO(resultSet.getString("name")))
        }

        return results
    }
}