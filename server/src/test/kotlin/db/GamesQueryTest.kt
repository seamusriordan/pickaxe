package db

import dto.GameDTO
import getEnvForWeek
import graphql.schema.DataFetchingEnvironment
import graphql.schema.DataFetchingEnvironmentImpl
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockkClass
import io.mockk.mockkStatic
import mockNextReturnTimes
import mockStatementToReturnGameResultSet
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import setupSQLQueryForGames
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.Statement

class GamesQueryTest {

    private lateinit var mockStatement: Statement
    private lateinit var mockConnection: Connection
    private lateinit var env: DataFetchingEnvironment

    @BeforeEach
    fun beforeEach() {
        mockkStatic("java.sql.DriverManager")
        mockConnection = mockkClass(Connection::class)
        mockStatement = mockkClass(Statement::class)

        every { DriverManager.getConnection(any()) } returns null
        every { DriverManager.getConnection(any(), any()) } returns mockConnection
        every { mockConnection.createStatement() } returns mockStatement

        env = getEnvForWeek("0")
    }

    @Test
    fun getReturnsGameWithChicago() {
        val week = "0"
        val expectedGames = arrayListOf(
            generateGame("GB@CHI", "CHI", -100.5, week)
        )
        val mockResultSet = setupSQLQueryForGames(expectedGames)
        configureNonNullReturn(mockResultSet, expectedGames)
        mockStatementToReturnGameResultSet(mockStatement, mockResultSet, week)

        val results = GamesQuery(mockConnection).get(env)

        Assertions.assertEquals(expectedGames.map { x -> x.name }, results.map { x -> x.name })
        Assertions.assertEquals(expectedGames.map { x -> x.result }, results.map { x -> x.result })
        Assertions.assertEquals(expectedGames.map { x -> x.spread }, results.map { x -> x.spread })
        Assertions.assertEquals(expectedGames.map { x -> x.week }, results.map { x -> x.week })
    }

    @Test
    fun nullSpreadInDbIsNullInResult() {
        val expectedGames: ArrayList<GameDTO> = ArrayList(1)
        expectedGames.add(generateGame("GB@CHI", "CHI", null, "0"))
        val mockResultSet = mockkClass(ResultSet::class)

        mockNextReturnTimes(mockResultSet, 1)

        every { mockResultSet.getString("game")
        } returns expectedGames[0].name

        every { mockResultSet.getString("result")
        } returns expectedGames[0].result

        every { mockResultSet.getDouble("spread")
        } returns 0.0

        every { mockResultSet.wasNull() } returnsMany listOf(false, true)

        every { mockResultSet.getString("week")
        } returns expectedGames[0].week

        every { mockStatement.executeQuery("SELECT game, week, result, spread FROM games WHERE week = '0'")
        } returns mockResultSet

        val results = GamesQuery(mockConnection).get(env)

        Assertions.assertNull(results[0].spread)
    }

    @Test
    fun nullResultInDbIsNullInResult() {
        val expectedGames: ArrayList<GameDTO> = ArrayList(1)
        expectedGames.add(generateGame("GB@CHI", null, -10.0, "0"))
        val mockResultSet = mockkClass(ResultSet::class)

        mockNextReturnTimes(mockResultSet, 1)

        every { mockResultSet.getString("game")
        } returns expectedGames[0].name

        every { mockResultSet.getString("result")
        } returns null

        every { mockResultSet.getDouble("spread")
        } returns expectedGames[0].spread!!

        every { mockResultSet.wasNull() } returnsMany listOf(true, false)

        every { mockResultSet.getString("week")
        } returns expectedGames[0].week

        every { mockStatement.executeQuery("SELECT game, week, result, spread FROM games WHERE week = '0'") } returns mockResultSet


        val results = GamesQuery(mockConnection).get(env)

        Assertions.assertNull(results[0].result)
    }

    @Test
    fun getReturnsGameWithTwoGames() {
        val expectedGames: ArrayList<GameDTO> = ArrayList(1)
        expectedGames.add(generateGame("SEA@ARI", "SEA", 10.5, "0"))
        expectedGames.add(generateGame("GB@CHI", "CHI", -100.5, "0"))
        val mockResultSet = mockkClass(ResultSet::class)
        mockNextReturnTimes(mockResultSet, 2)

        configureNonNullReturn(mockResultSet, expectedGames)

        every {
            mockStatement.executeQuery("SELECT game, week, result, spread FROM games WHERE week = '0'")
        } returns mockResultSet


        val results = GamesQuery(mockConnection).get(env)

        Assertions.assertEquals(expectedGames.map { x -> x.name }, results.map { x -> x.name })
        Assertions.assertEquals(expectedGames.map { x -> x.result }, results.map { x -> x.result })
        Assertions.assertEquals(expectedGames.map { x -> x.spread }, results.map { x -> x.spread })
        Assertions.assertEquals(expectedGames.map { x -> x.week }, results.map { x -> x.week })
    }

    @Test
    fun getReturnsGameWithGameInWeek1() {
        val expectedGames: ArrayList<GameDTO> = ArrayList(1)
        expectedGames.add(generateGame("NE@TB", "TB", -10.5, "1"))
        val mockResultSet = mockkClass(ResultSet::class)

        mockNextReturnTimes(mockResultSet, 1)

        configureNonNullReturn(mockResultSet, expectedGames)

        every {
            mockStatement.executeQuery("SELECT game, week, result, spread FROM games WHERE week = '1'")
        } returns mockResultSet

        val results = GamesQuery(mockConnection).get(getEnvForWeek("1"))

        Assertions.assertEquals(expectedGames.map { x -> x.name }, results.map { x -> x.name })
        Assertions.assertEquals(expectedGames.map { x -> x.result }, results.map { x -> x.result })
        Assertions.assertEquals(expectedGames.map { x -> x.spread }, results.map { x -> x.spread })
        Assertions.assertEquals(expectedGames.map { x -> x.week }, results.map { x -> x.week })
    }

    private fun configureNonNullReturn(
        mockResultSet: ResultSet,
        expectedGames: ArrayList<GameDTO>
    ) {
        every {
            mockResultSet.getString("game")
        } returnsMany expectedGames.map { game -> game.name }

        every {
            mockResultSet.getString("result")
        } returnsMany expectedGames.map { game -> game.result }

        every {
            mockResultSet.getDouble("spread")
        } returnsMany expectedGames.map { game -> game.spread!! }

        every {
            mockResultSet.getString("week")
        } returnsMany expectedGames.map { game -> game.week }

        every { mockResultSet.wasNull() } returns false
    }

    private fun generateGame(name: String, result: String?, spread: Double?, week: String): GameDTO {
        val game = GameDTO(name, week)
        game.result = result
        game.spread = spread
        return game
    }

}