import dto.UserDTO
import dto.UserPicksDTO
import graphql.schema.DataFetchingEnvironment
import graphql.schema.DataFetchingEnvironmentImpl
import io.mockk.every
import io.mockk.mockkClass
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.sql.Connection
import java.sql.DriverManager
import java.sql.Statement

class UpdatePickMutatorTest {
    private lateinit var updatePickMutator: UpdatePickMutator

    private lateinit var dataStore: ArrayList<ArrayList<UserPicksDTO>>

    private lateinit var mockStatement: Statement
    private lateinit var mockConnection: Connection

    @BeforeEach
    fun setup() {
        dataStore = ArrayList(0)
        val weekOfPicks: ArrayList<UserPicksDTO> = ArrayList()

        weekOfPicks.add(UserPicksDTO(UserDTO("Person")))
        weekOfPicks.add(UserPicksDTO(UserDTO("Person2")))

        dataStore.add(weekOfPicks)


        mockkStatic("java.sql.DriverManager")
        mockConnection = mockkClass(Connection::class)
        mockStatement = mockkClass(Statement::class)
        every { mockStatement.executeQuery(any()) } returns null


        every { DriverManager.getConnection(any()) } returns null
        every { DriverManager.getConnection(any(), any()) } returns mockConnection
        every { mockConnection.createStatement() } returns mockStatement

        updatePickMutator = UpdatePickMutator(mockConnection)
    }

    @Test
    fun pickForFirstGameCanBeSetByFetchingEnvironment() {
        val passedArguments
                = generatePickArguments("Person", 0, "GB@CHI", "Different")
        val env = buildEnvForArguments(passedArguments)
        val expectedQuery =
            "UPDATE userpicks SET name = " + passedArguments["name"] + ", week = " + generatePick(0, "GB@CHI", "Different")["week"] + ", game = " + generatePick(0, "GB@CHI", "Different")["game"] + ", pick = " + generatePick(0, "GB@CHI", "Different")["pick"]

        val result = updatePickMutator.get(env)

        verify { mockStatement.executeQuery(expectedQuery) }
        assertTrue(result)
    }

    private fun generatePickArguments(name: String, week: Int, game: String, pick: String): HashMap<String, Any> {
        val passedArguments: HashMap<String, Any> = HashMap()
        passedArguments["name"] = name
        passedArguments["userPick"] = generatePick(week, game, pick)
        return passedArguments
    }

    private fun generatePick(week: Int, game: String, pick: String): HashMap<String, Any> {
        val userPick = HashMap<String, Any>()
        userPick["week"] = week
        userPick["game"] = game
        userPick["pick"] = pick
        return userPick
    }

    private fun pickByWeekUserGameIndex(week: Int, user: Int, game: Int) = dataStore[week][user].picks[game]

    @Test
    fun pickForSecondGameCanBeSetByFetchingEnvironment() {
        val passedArguments
                = generatePickArguments("Person", 0, "BUF@NE", "Very Different")
        val env = buildEnvForArguments(passedArguments)
        val expectedQuery =
            "UPDATE userpicks SET name = " + passedArguments["name"] + ", week = " + generatePick(0, "BUF@NE", "Very Different")["week"] + ", game = " + generatePick(0, "BUF@NE", "Very Different")["game"] + ", pick = " + generatePick(0, "BUF@NE", "Very Different")["pick"]


        val result = updatePickMutator.get(env)

        verify { mockStatement.executeQuery(expectedQuery) }
        assertTrue(result)
    }

    @Test
    fun pickForThirdGameWithSecondUserCanBeSetByFetchingEnvironment() {
        val passedArguments
                = generatePickArguments("Person2", 4,  "SEA@PHI", "PHI")
        val env = buildEnvForArguments(passedArguments)
        val expectedQuery =
            "UPDATE userpicks SET name = " + passedArguments["name"] + ", week = " + generatePick(4,  "SEA@PHI", "PHI")["week"] + ", game = " + generatePick(4,  "SEA@PHI", "PHI")["game"] + ", pick = " + generatePick(4,  "SEA@PHI", "PHI")["pick"]

        val result = updatePickMutator.get(env)

        verify { mockStatement.executeQuery(expectedQuery) }
        assertTrue(result)
    }

    private fun buildEnvForArguments(passedArguments: HashMap<String, Any>): DataFetchingEnvironment {
        return DataFetchingEnvironmentImpl
            .newDataFetchingEnvironment()
            .arguments(passedArguments)
            .build()
    }
}