import dto.PickDTO
import dto.UserDTO
import dto.UserPicksDTO
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import java.sql.Connection

class UserPickDataQueryFetcher(connection: Connection) : DataFetcher<List<UserPicksDTO>> {
    override fun get(environment: DataFetchingEnvironment): List<UserPicksDTO> {
        val expectedPicks: ArrayList<UserPicksDTO> = ArrayList(1)
        expectedPicks.add(UserPicksDTO(UserDTO("Seamus")))
        expectedPicks[0].picks.clear()
        expectedPicks[0].picks.add(PickDTO("GB@CHI", "CHI"))

        return expectedPicks
    }
}