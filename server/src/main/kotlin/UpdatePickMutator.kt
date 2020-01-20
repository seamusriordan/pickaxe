import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment

class UpdatePickMutator(private var store: List<List<UserPicksDTO>>) : DataFetcher<Boolean> {
    override fun get(environment: DataFetchingEnvironment): Boolean {
        val storedPick = getStoredPickToUpdate(environment)
        val passedUserPick = environment.arguments["userPick"] as HashMap<String, Any>

        storedPick.pick = passedUserPick["pick"] as String

        return true
    }

    private fun getStoredPickToUpdate(environment: DataFetchingEnvironment): PickDTO {
        val userName = environment.arguments["name"] as String
        val passedUserPick = environment.arguments["userPick"] as HashMap<String, Any>
        val gameName = passedUserPick["game"] as String

        return getPickForUserAndGame(userName, gameName)
    }

    private fun getPickForUserAndGame(userName: String, gameName: String): PickDTO {
        val storedUserPicks = getPicksForUserWithName(userName)
        return getPickForGame(storedUserPicks, gameName)
    }

    private fun getPicksForUserWithName(userName: String): UserPicksDTO {
        val defaultWeek = 0
        return store[defaultWeek].first {
            it.user.name == userName
        }
    }

    private fun getPickForGame(userPicks: UserPicksDTO, gameName: String): PickDTO {
        return userPicks.picks.first {
            it.game == gameName
        }
    }
}