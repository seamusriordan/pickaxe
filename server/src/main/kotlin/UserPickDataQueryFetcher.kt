import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment

class UserPickDataQueryFetcher(private var store: List<List<UserPicksDTO>>) : DataFetcher<List<UserPicksDTO>> {
    override fun get(environment: DataFetchingEnvironment): List<UserPicksDTO> {
        return store[0]
    }
}