import dto.PickDTO
import dto.UserPicksDTO
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
        val expectedQuery = "UPDATE userpicks SET name = $name, week = $week, game = $game, pick = $pick"

        statement.executeQuery(expectedQuery)
        return true
    }
}