import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment

class UpdatePickMutator(private var store: List<List<UserPicksDTO>>) : DataFetcher<Boolean> {
    override fun get(environment: DataFetchingEnvironment): Boolean {
        val envPickDTO = environment.getArgument<HashMap<String, Any>>("userPick")

        val userPicksDTO = store[0].first {
            it.user.name == environment.variables["name"]
        }

        val pickDTO = userPicksDTO.picks.first {
            it.game == envPickDTO["game"]
        }

        pickDTO.pick = envPickDTO["pick"] as String

        return true
    }

}