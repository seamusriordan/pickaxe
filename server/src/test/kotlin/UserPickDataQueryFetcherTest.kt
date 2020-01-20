import graphql.schema.DataFetchingEnvironmentImpl
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UserPickDataQueryFetcherTest {
    private lateinit var userPickDataQueryFetcher: UserPickDataQueryFetcher

    private lateinit var dataStore: ArrayList<ArrayList<UserPicksDTO>>

    @BeforeEach
    fun setup() {
        dataStore = ArrayList(0)
        val weekOfPicks: ArrayList<UserPicksDTO> = ArrayList()

        weekOfPicks.add(UserPicksDTO(UserDTO("Person")))

        dataStore.add(weekOfPicks)

        userPickDataQueryFetcher = UserPickDataQueryFetcher(dataStore)
    }

    @Test
    fun implementsGetWhichReturnsFirstElementInStore() {
        val env = DataFetchingEnvironmentImpl.newDataFetchingEnvironment().build()
        val listForWeek = userPickDataQueryFetcher.get(env)

        assertEquals(dataStore[0], listForWeek)
    }
}