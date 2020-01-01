import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment

class UserPickDataMutationFetcher(private var store: List<List<UserPicksDTO>>) : DataFetcher<Boolean> {
    override fun get(environment: DataFetchingEnvironment): Boolean {

        println("Mutation requested with $environment")
        println(environment.getArgument<Map<String, Object>>("userPick"))
        println(environment.getArgument<Map<String, Object>>("name"))


//
//        val userPicksDTO = store[0].first {
//            it.user.name == environment.variables["name"]
//        }
//
//        val pickDTO = userPicksDTO.picks.first {
//            it.game == envPickDTO.game
//        }
//
//        pickDTO.pick = envPickDTO.pick;

        return true
    }

}