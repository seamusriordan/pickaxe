import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment

class UserPickDataMutationFetcher(private var store: List<List<UserPicksDTO>>) : DataFetcher<Unit> {
    override fun get(environment: DataFetchingEnvironment) {
        var envPickDTO = environment.variables["pick"] as UpdatedPickDTO;

        var userPicksDTO = store[0].first {
            it.user.name == environment.variables["name"]
        }

        var pickDTO = userPicksDTO.picks.first {
            it.game == envPickDTO.game
        }

        pickDTO.pick = envPickDTO.pick;
    }

}