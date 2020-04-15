package db

import SQLState
import dto.WeekDTO
import graphql.schema.DataFetchingEnvironment
import graphql.schema.DataFetchingEnvironmentImpl
import io.mockk.every
import io.mockk.mockkClass
import io.mockk.mockkStatic
import mockNextReturnTimes
import mockStatementToReturnWeekResultSet
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import setupSQLQueryForWeeks
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
        val sqlState = SQLState().apply {
            weeks.add(weekWithNameAndOrder("3", 3))
        }
        sqlState.mockSQLState(mockStatement)

        val results = WeeksQuery(mockConnection).get(env)

        Assertions.assertEquals(sqlState.weeks[0].name, results[0].name)
        Assertions.assertEquals(sqlState.weeks[0].weekOrder, results[0].weekOrder)
    }

    @Test
    fun getReturnsWeeksWhenThreeWeeks() {
        val sqlState = SQLState().apply {
            weeks.add(weekWithNameAndOrder("0", 1))
            weeks.add(weekWithNameAndOrder("4", 4))
            weeks.add(weekWithNameAndOrder("19", 199))
        }
        sqlState.mockSQLState(mockStatement)

        val results = WeeksQuery(mockConnection).get(env)

        Assertions.assertEquals(sqlState.weeks.map { week -> week.name }, results.map { week -> week.name })
        Assertions.assertEquals(sqlState.weeks.map { week -> week.weekOrder }, results.map { week -> week.weekOrder })
    }

    private fun weekWithNameAndOrder(name: String, order: Int): WeekDTO {
        return WeekDTO(name).apply {
            weekOrder = order
        }
    }
}