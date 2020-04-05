import dto.PickDTO
import dto.UserDTO
import dto.UserPicksDTO
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import java.sql.Connection

class UserPickDataQueryFetcher(private val connection: Connection) : DataFetcher<List<UserPicksDTO>> {
    override fun get(environment: DataFetchingEnvironment): List<UserPicksDTO> {
        val statement = connection.createStatement()
        val week = environment.arguments["week"]

        val resultSet = statement.executeQuery("SELECT name, game, pick FROM userpicks WHERE week = $week")

        val results = ArrayList<UserPicksDTO>(0)

        while (resultSet.next()) {
            val name = resultSet.getString("name")
            var result = results.find {
                it.user.name.contains(name)
            }
            if( result == null ) {
                result = UserPicksDTO(UserDTO(name))
                result.picks.clear()
                results.add(result)
            }
            result.picks.add(PickDTO(resultSet.getString("game"), resultSet.getString("pick")))
        }

        return results
    }
}