import dto.UserDTO
import dto.UserPicksDTO
import graphql.schema.DataFetchingEnvironmentImpl
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UpdatePickMutatorTest {
    private lateinit var updatePickMutator: UpdatePickMutator

    private lateinit var dataStore: ArrayList<ArrayList<UserPicksDTO>>

    @BeforeEach
    fun setup() {
        dataStore = ArrayList(0)
        val weekOfPicks: ArrayList<UserPicksDTO> = ArrayList()

        weekOfPicks.add(UserPicksDTO(UserDTO("Person")))
        weekOfPicks.add(UserPicksDTO(UserDTO("Person2")))

        dataStore.add(weekOfPicks)

        updatePickMutator = UpdatePickMutator(dataStore)
    }

    @Test
    fun pickForFirstGameCanBeSetByFetchingEnvironment() {
        val envBuilder = DataFetchingEnvironmentImpl.newDataFetchingEnvironment()

        val passedArguments: HashMap<String, Any> = HashMap()
        passedArguments["name"] = "Person"
        val userPick = HashMap<String, Any>()
        userPick["week"] = 0
        userPick["game"] = "GB@CHI"
        userPick["pick"] = "Different"

        passedArguments["userPick"] = userPick
        envBuilder.arguments(passedArguments)

        val env = envBuilder.build()

        val result = updatePickMutator.get(env)

        assertEquals("Different", pickByWeekUserGameIndex(0, 0, 0).pick)
        assertEquals(true, result)
    }

    private fun pickByWeekUserGameIndex(week: Int, user: Int, game: Int) = dataStore[week][user].picks[game]

    @Test
    fun pickForSecondGameCanBeSetByFetchingEnvironment() {
        val envBuilder = DataFetchingEnvironmentImpl.newDataFetchingEnvironment()

        val passedArguments: HashMap<String, Any> = HashMap()
        passedArguments["name"] = "Person"
        val userPick = HashMap<String, Any>()
        userPick["week"] = 0
        userPick["game"] = "BUF@NE"
        userPick["pick"] = "Very Different"

        passedArguments["userPick"] = userPick

        envBuilder.arguments(passedArguments)


        envBuilder.arguments(passedArguments)

        val env = envBuilder.build()

        val result = updatePickMutator.get(env)

        assertEquals("Very Different", pickByWeekUserGameIndex(0, 0, 1).pick)
        assertEquals(true, result)
    }

    @Test
    fun pickForThirdGameWithSecondUserCanBeSetByFetchingEnvironment() {
        val envBuilder = DataFetchingEnvironmentImpl.newDataFetchingEnvironment()

        val passedArguments: HashMap<String, Any> = HashMap()
        passedArguments["name"] = "Person2"
        val userPick = HashMap<String, Any>()
        userPick["week"] = 0
        userPick["game"] = "SEA@PHI"
        userPick["pick"] = "PHI"

        passedArguments["userPick"] = userPick

        envBuilder.arguments(passedArguments)

        val env = envBuilder.build()

        updatePickMutator.get(env)

        assertEquals("PHI", pickByWeekUserGameIndex(0, 1, 2).pick)
    }
}