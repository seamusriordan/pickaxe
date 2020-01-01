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
        passedVariables["userPick"] = UpdatedPickDTO(0, "GB@CHI", "Different");

        envBuilder.variables(passedVariables)

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
        passedVariables["userPick"] = UpdatedPickDTO(0, "BUF@NE", "Very Different");

        envBuilder.variables(passedVariables)

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
        passedVariables["userPick"] = UpdatedPickDTO(0, "SEA@PHI", "PHI");

        envBuilder.variables(passedVariables)

        var env = envBuilder.build()

        userPickDataMutationFetcher.get(env)

        assertEquals("PHI", pickByWeekUserGameIndex(0, 1, 2).pick);
    }
}