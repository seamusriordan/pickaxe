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

class WeeksQueryTest {
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
    fun getReturnsWeeksWhenOnlyWeekIs0() {
        val expectedWeeks = ArrayList<WeekDTO>(1)
        expectedWeeks.add(WeekDTO("0"))

        val mockResultSet = mockkClass(ResultSet::class)
        every {
            mockResultSet.next()
        } returns true andThen false
        every {
            mockResultSet.getString("name")
        } returns expectedWeeks[0].name
        every { mockStatement.executeQuery("SELECT name FROM weeks") } returns mockResultSet

        val results = WeeksQuery(mockConnection).get(env)

        Assertions.assertEquals(expectedWeeks[0].week, results[0].week)
    }

    @Test
    fun getReturnsWeeksWhenThreeWeeks() {
        val expectedWeeks = ArrayList<WeekDTO>(1)
        expectedWeeks.add(WeekDTO("0"))
        expectedWeeks.add(WeekDTO("4"))
        expectedWeeks.add(WeekDTO("19"))

        val mockResultSet = mockkClass(ResultSet::class)
        every {
            mockResultSet.next()
        } returns true andThen true andThen true andThen false
        every {
            mockResultSet.getString("name")
        } returns expectedWeeks[0].name andThen expectedWeeks[1].name andThen expectedWeeks[2].name
        every { mockStatement.executeQuery("SELECT name FROM weeks") } returns mockResultSet

        val results = WeeksQuery(mockConnection).get(env)

        Assertions.assertEquals(expectedWeeks.map { week -> week.name }, results.map { week -> week.name })
    }
}