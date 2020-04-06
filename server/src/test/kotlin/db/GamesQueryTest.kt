package db

import dto.GameDTO
import graphql.schema.DataFetchingEnvironment
import graphql.schema.DataFetchingEnvironmentImpl
import io.mockk.every
import io.mockk.mockkClass
import io.mockk.mockkStatic
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
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

        env = DataFetchingEnvironmentImpl.newDataFetchingEnvironment().build()
    }

    @Test
    fun getReturnsGameWithChicago() {
        val expectedGames: ArrayList<GameDTO> = ArrayList(1)
        expectedGames.add(generateGame("GB@CHI", "CHI", -100.5, "0"))
        val mockResultSet = mockkClass(ResultSet::class)
        every { mockResultSet.next()
        } returns true andThen false
        every { mockResultSet.getString("game")
        } returns expectedGames[0].name
        every { mockResultSet.getString("result")
        } returns expectedGames[0].result
        every { mockResultSet.getFloat("spread")
        } returns expectedGames[0].spread?.toFloat()!!
        every { mockResultSet.getString("week")
        } returns expectedGames[0].week
        every { mockStatement.executeQuery("SELECT game, week, result, spread FROM games WHERE week = '0'") } returns mockResultSet

        val results = GamesQuery(mockConnection).get(env)

        Assertions.assertEquals(expectedGames.map { x -> x.name }, results.map { x -> x.name })
        Assertions.assertEquals(expectedGames.map { x -> x.result }, results.map { x -> x.result })
        Assertions.assertEquals(expectedGames.map { x -> x.spread?.toFloat() }, results.map { x -> x.spread })
        Assertions.assertEquals(expectedGames.map { x -> x.week }, results.map { x -> x.week })
    }

    @Test
    fun getReturnsGameWithTwoGames() {
        val expectedGames: ArrayList<GameDTO> = ArrayList(1)
        expectedGames.add(generateGame("SEA@ARI", "SEA", 10.5, "0"))
        expectedGames.add(generateGame("GB@CHI", "CHI", -100.5, "0"))
        val mockResultSet = mockkClass(ResultSet::class)
        every { mockResultSet.next()
        } returns true andThen true andThen  false
        every { mockResultSet.getString("game")
        } returns expectedGames[0].name andThen expectedGames[1].name
        every { mockResultSet.getString("result")
        } returns expectedGames[0].result andThen expectedGames[1].result
        every { mockResultSet.getFloat("spread")
        } returns expectedGames[0].spread?.toFloat()!! andThen expectedGames[1].spread?.toFloat()!!
        every { mockResultSet.getString("week")
        } returns expectedGames[0].week andThen expectedGames[1].week
        every { mockStatement.executeQuery("SELECT game, week, result, spread FROM games WHERE week = '0'") } returns mockResultSet

        val results = GamesQuery(mockConnection).get(env)

        Assertions.assertEquals(expectedGames.map { x -> x.name }, results.map { x -> x.name })
        Assertions.assertEquals(expectedGames.map { x -> x.result }, results.map { x -> x.result })
        Assertions.assertEquals(expectedGames.map { x -> x.spread?.toFloat() }, results.map { x -> x.spread })
        Assertions.assertEquals(expectedGames.map { x -> x.week }, results.map { x -> x.week })
    }

    private fun generateGame(name: String, result: String, spread: Double, week: String): GameDTO {
        val game = GameDTO(name, week)
        game.result = result
        game.spread = spread
        return game
    }

}