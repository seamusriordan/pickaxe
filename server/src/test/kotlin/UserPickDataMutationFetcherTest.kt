import graphql.schema.DataFetchingEnvironmentImpl
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UserPickDataMutationFetcherTest {
    private lateinit var userPickDataMutationFetcher: UserPickDataMutationFetcher;

    private lateinit var dataStore: ArrayList<ArrayList<UserPicksDTO>>;

    @BeforeEach
    fun setup() {
        dataStore = ArrayList(0);
        var weekOfPicks: ArrayList<UserPicksDTO> = ArrayList();

        weekOfPicks.add(UserPicksDTO(UserDTO("Person")))
        weekOfPicks.add(UserPicksDTO(UserDTO("Person2")))

        dataStore.add(weekOfPicks)

        userPickDataMutationFetcher = UserPickDataMutationFetcher(dataStore)
    }

    @Test
    fun pickForFirstGameCanBeSetByFetchingEnvironment() {
        val envBuilder = DataFetchingEnvironmentImpl.newDataFetchingEnvironment();

        var passedVariables: HashMap<String, Any> = HashMap<String, Any>();
        passedVariables["name"] = "Person"
        var userPick = HashMap<String, Any>();
        userPick["week"] = 0
        userPick["game"] = "GB@CHI"
        userPick["pick"] = "Different"

        passedVariables["userPick"] = userPick
        envBuilder.variables(passedVariables)
        envBuilder.arguments(passedVariables)

        var env = envBuilder.build()

        val result = userPickDataMutationFetcher.get(env)

        assertEquals("Different", pickByWeekUserGameIndex(0, 0, 0).pick);
        assertEquals(true, result);
    }

    private fun pickByWeekUserGameIndex(week: Int, user: Int, game: Int) = dataStore[week][user].picks[game]

    @Test
    fun pickForSecondGameCanBeSetByFetchingEnvironment() {
        val envBuilder = DataFetchingEnvironmentImpl.newDataFetchingEnvironment();

        var passedVariables: HashMap<String, Any> = HashMap<String, Any>();
        passedVariables["name"] = "Person"
        var userPick = HashMap<String, Any>();
        userPick["week"] = 0
        userPick["game"] = "BUF@NE"
        userPick["pick"] = "Very Different"

        passedVariables["userPick"] = userPick

        envBuilder.variables(passedVariables)
        envBuilder.arguments(passedVariables)


        envBuilder.arguments(passedVariables)

        var env = envBuilder.build()

        val result = userPickDataMutationFetcher.get(env)

        assertEquals("Very Different", pickByWeekUserGameIndex(0, 0, 1).pick);
        assertEquals(true, result);
    }

    @Test
    fun pickForThirdGameWithSecondUserCanBeSetByFetchingEnvironment() {
        val envBuilder = DataFetchingEnvironmentImpl.newDataFetchingEnvironment();

        var passedVariables: HashMap<String, Any> = HashMap<String, Any>();
        passedVariables["name"] = "Person2"
        var userPick = HashMap<String, Any>();
        userPick["week"] = 0
        userPick["game"] = "SEA@PHI"
        userPick["pick"] = "PHI"

        passedVariables["userPick"] = userPick

        envBuilder.variables(passedVariables)
        envBuilder.arguments(passedVariables)

        var env = envBuilder.build()

        userPickDataMutationFetcher.get(env)

        assertEquals("PHI", pickByWeekUserGameIndex(0, 1, 2).pick);
    }
}