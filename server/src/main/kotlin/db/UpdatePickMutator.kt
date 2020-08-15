package db

import dto.PickDTO
import dto.UserDTO
import dto.WeekDTO
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import java.sql.Connection

class UpdatePickMutator(private var connection: Connection) : DataFetcher<Boolean> {
    override fun get(environment: DataFetchingEnvironment): Boolean {
        val passedUserPick = environment.arguments["userPick"] as HashMap<*, *>

        val name = environment.arguments["name"] as String
        if(name == "RNG" || name == "Vegas") {
            return false
        }
        val week = passedUserPick["week"] as String
        val game = passedUserPick["game"] as String
        val pick = passedUserPick["pick"] as String

        return updatePick(
            UserDTO(name),
            WeekDTO(week),
            PickDTO(game, pick)
        )
    }

    fun updatePick(user: UserDTO, week: WeekDTO, pick: PickDTO): Boolean {
        val statement = connection.createStatement()
        val update = "INSERT INTO userpicks VALUES ('${user.name}', '${week.name}', '${pick.game}', '${pick.pick}') " +
                "ON CONFLICT (name, week, game) DO UPDATE SET pick = '${pick.pick}'"
        statement.executeUpdate(update)
        return true
    }
}