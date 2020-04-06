package db

import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import java.sql.Connection

class UpdatePickMutator(private var connection: Connection) : DataFetcher<Boolean> {
    override fun get(environment: DataFetchingEnvironment): Boolean {
        val passedUserPick = environment.arguments["userPick"] as HashMap<*, *>
        val name = environment.arguments["name"]
        val week = passedUserPick["week"]
        val game = passedUserPick["game"]
        val pick = passedUserPick["pick"]

        val statement = connection.createStatement()
        val update = "INSERT INTO userpicks VALUES ('$name', '$week', '$game', '$pick') " +
                "ON CONFLICT (name, week, game) DO UPDATE SET pick = '$pick'"

        statement.executeUpdate(update)
        return true
    }
}