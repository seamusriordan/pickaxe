package db

import java.sql.Connection
import java.sql.DriverManager
import java.util.*

class PickaxeDB {
    fun getDBConnection(): Connection {
        val user = getEnvOrDefault("POSTGRES_USER", "postgres")
        val password = getEnvOrDefault("POSTGRES_PASSWORD", "pword")


        val properties = Properties().apply {
            put("user", user)
            put("password", password)
        }

        val port = getEnvOrDefault("POSTGRES_PORT", "5432")
        val host = getEnvOrDefault("POSTGRES_HOST", "postgres")

        return DriverManager.getConnection(generateDbUrl(host, port), properties)
    }

    private fun generateDbUrl(host: String, port: String) = "jdbc:postgresql://$host:$port/pickaxe_dev"

    private fun getEnvOrDefault(env: String, default: String): String {
        var value = System.getenv(env)
        if (value == null) {
            value = default
        }
        return value
    }
}