package db

import getEnvOrDefault
import org.slf4j.LoggerFactory
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
        val dbname = getEnvOrDefault("POSTGRES_DB", "pickaxe_dev")

        val dbUrl = generateDbUrl(host, port, dbname)
        logger.info("DB connection to $dbUrl")

        return DriverManager.getConnection(dbUrl, properties)
    }

    companion object {
        val logger: org.slf4j.Logger = LoggerFactory.getLogger(PickaxeDB::class.toString())
        private fun generateDbUrl(host: String, port: String, dbname: String) = "jdbc:postgresql://$host:$port/$dbname"
    }
}