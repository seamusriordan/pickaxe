package db

import dto.WeekDTO
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

class CurrentWeekQueryTest {
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
    fun getReturnsCurrentWeek0WhenOnlyWeekIs0() {
        val expectedWeek = WeekDTO("0")

        val mockResultSet = mockkClass(ResultSet::class)
        every { mockResultSet.next()
        } returns true andThen false
        every { mockResultSet.getString("name")
        } returns expectedWeek.name
        every { mockStatement.executeQuery("SELECT name FROM weeks") } returns mockResultSet

        val results = CurrentWeekQuery(mockConnection).get(env)

        Assertions.assertEquals(expectedWeek.name, results.name)
    }

    @Test
    fun getReturnsCurrentWeek1WhenOnlyWeekIs1() {
        val expectedWeek = WeekDTO("1")

        val mockResultSet = mockkClass(ResultSet::class)
        every { mockResultSet.next()
        } returns true andThen false
        every { mockResultSet.getString("name")
        } returns expectedWeek.name
        every { mockStatement.executeQuery("SELECT name FROM weeks") } returns mockResultSet

        val results = CurrentWeekQuery(mockConnection).get(env)

        Assertions.assertEquals(expectedWeek.name, results.name)
    }

    @Test
    fun getReturnsCurrentWeek3WhenFirstWeekIs3() {
        val expectedWeek = WeekDTO("3")

        val mockResultSet = mockkClass(ResultSet::class)
        every { mockResultSet.next()
        } returns true andThen true andThen false
        every { mockResultSet.getString("name")
        } returns expectedWeek.name andThen "7"
        every { mockStatement.executeQuery("SELECT name FROM weeks") } returns mockResultSet

        val results = CurrentWeekQuery(mockConnection).get(env)

        Assertions.assertEquals(expectedWeek.name, results.name)
    }
}