package db

import SQLState
import dto.GameDTO
import getEnvForWeek
import graphql.schema.DataFetchingEnvironment
import io.mockk.every
import io.mockk.mockkClass
import io.mockk.mockkStatic
import mockNextReturnTimes
import mockStatementToReturnGameResultSet
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.Statement
import java.time.OffsetDateTime
import java.util.*

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
        val sqlState = SQLState(week).apply {
            games.add(generateGame("GB@CHI", "CHI", -100.5, week))
        }
        sqlState.mockSQLState(mockStatement)

        val results = GamesQuery(mockConnection).get(env)

        assertGamesShallowEquals(sqlState, results)
    }

    @Test
    fun getReturnsGameWithTwoGames() {
        val week = "0"
        val sqlState = SQLState(week).apply {
            games.add(generateGame("SEA@ARI", "SEA", 10.5, week))
            games.add(generateGame("GB@CHI", "CHI", -100.5, week))
        }
        sqlState.mockSQLState(mockStatement)

        val results = GamesQuery(mockConnection).get(env)

        assertGamesShallowEquals(sqlState, results)
    }

    @Test
    fun getReturnsGameWithGameInWeek1() {
        val week = "1"
        val sqlState = SQLState(week).apply {
            games.add(generateGame("NE@TB", "TB", -10.5, week))

        }
        sqlState.mockSQLState(mockStatement)

        val results = GamesQuery(mockConnection).get(getEnvForWeek(week))

        assertGamesShallowEquals(sqlState, results)
    }

    @Test
    fun nullSpreadInDbIsNullInResult() {
        val week = "0"
        val expectedGames = arrayListOf(
            generateGame("GB@CHI", "CHI", null, week)
        )
        val mockResultSet = mockkClass(ResultSet::class)
        mockNextReturnTimes(mockResultSet, 1)

        every {
            mockResultSet.getString("game")
        } returns expectedGames[0].name

        every {
            mockResultSet.getString("result")
        } returns expectedGames[0].result

        every {
            mockResultSet.getDouble("spread")
        } returns 0.0

        every { mockResultSet.wasNull() } returnsMany listOf(false, true)

        every {
            mockResultSet.getString("week")
        } returns expectedGames[0].week

        every {
            mockResultSet.getObject("id", UUID::class.java)
        } returns expectedGames[0].id

        every {
            mockResultSet.getObject("gametime", OffsetDateTime::class.java)
        } returns expectedGames[0].gameTime

        mockStatementToReturnGameResultSet(mockStatement, mockResultSet, week)

        val results = GamesQuery(mockConnection).get(env)

        Assertions.assertNull(results[0].spread)
    }

    @Test
    fun nullResultInDbIsNullInResult() {
        val week = "0"
        val expectedGames = arrayListOf(
            generateGame("GB@CHI", null, -10.0, week)
        )
        val mockResultSet = mockkClass(ResultSet::class)

        mockNextReturnTimes(mockResultSet, 1)

        every {
            mockResultSet.getString("game")
        } returns expectedGames[0].name

        every {
            mockResultSet.getString("result")
        } returns null

        every {
            mockResultSet.getDouble("spread")
        } returns expectedGames[0].spread!!

        every { mockResultSet.wasNull() } returnsMany listOf(true, false)

        every {
            mockResultSet.getString("week")
        } returns expectedGames[0].week

        every {
            mockResultSet.getObject("id", UUID::class.java)
        } returns expectedGames[0].id

        every {
            mockResultSet.getObject("gametime", OffsetDateTime::class.java)
        } returns expectedGames[0].gameTime

        mockStatementToReturnGameResultSet(mockStatement, mockResultSet, week)

        val results = GamesQuery(mockConnection).get(env)

        Assertions.assertNull(results[0].result)
    }

    @Test
    fun nullIdInDbIsNullInGame() {
        val week = "0"
        val expectedGames = arrayListOf(
            generateGame("GB@CHI", null, -10.0, week)
        )
        val mockResultSet = mockkClass(ResultSet::class)

        mockNextReturnTimes(mockResultSet, 1)

        every {
            mockResultSet.getString("game")
        } returns expectedGames[0].name

        every {
            mockResultSet.getString("result")
        } returns "GB"

        every {
            mockResultSet.getDouble("spread")
        } returns expectedGames[0].spread!!

        every { mockResultSet.wasNull() } returnsMany listOf(true, false)

        every {
            mockResultSet.getString("week")
        } returns expectedGames[0].week

        every {
            mockResultSet.getObject("id", UUID::class.java)
        } returns null

        every {
            mockResultSet.getObject("gametime", OffsetDateTime::class.java)
        } returns expectedGames[0].gameTime

        mockStatementToReturnGameResultSet(mockStatement, mockResultSet, week)

        val results = GamesQuery(mockConnection).get(env)

        Assertions.assertNull(results[0].result)
    }

    @Test
    fun nullGametimeInDbIsNullInGame() {
        val week = "0"
        val expectedGames = arrayListOf(
            generateGame("GB@CHI", null, -10.0, week)
        )
        val mockResultSet = mockkClass(ResultSet::class)

        mockNextReturnTimes(mockResultSet, 1)

        every {
            mockResultSet.getString("game")
        } returns expectedGames[0].name

        every {
            mockResultSet.getString("result")
        } returns "GB"

        every {
            mockResultSet.getDouble("spread")
        } returns expectedGames[0].spread!!

        every { mockResultSet.wasNull() } returnsMany listOf(true, false)

        every {
            mockResultSet.getString("week")
        } returns expectedGames[0].week

        every {
            mockResultSet.getObject("id", UUID::class.java)
        } returns expectedGames[0].id

        every {
            mockResultSet.getObject("gametime", OffsetDateTime::class.java)
        } returns null

        mockStatementToReturnGameResultSet(mockStatement, mockResultSet, week)

        val results = GamesQuery(mockConnection).get(env)

        Assertions.assertNull(results[0].result)
    }

    private fun assertGamesShallowEquals(sqlState: SQLState, results: List<GameDTO>) {
        assertEquals(sqlState.games.map { x -> x.name }, results.map { x -> x.name })
        assertEquals(sqlState.games.map { x -> x.result }, results.map { x -> x.result })
        assertEquals(sqlState.games.map { x -> x.spread }, results.map { x -> x.spread })
        assertEquals(sqlState.games.map { x -> x.week }, results.map { x -> x.week })
        assertEquals(sqlState.games.map { x -> x.gameTime }, results.map { x -> x.gameTime })
        assertEquals(sqlState.games.map { x -> x.id }, results.map { x -> x.id })
    }

    private fun generateGame(name: String, result: String?, spread: Double?, week: String): GameDTO {
        val game = GameDTO(name, week)
        game.result = result
        game.spread = spread
        game.id = UUID.randomUUID()
        game.gameTime = OffsetDateTime.now()
        return game
    }

}