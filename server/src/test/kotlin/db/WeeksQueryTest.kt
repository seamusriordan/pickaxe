package db

import SQLState
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
            weeks.add(buildWeek("3", "REG",3, 13))
        }
        sqlState.mockSQLState(mockStatement)

        val results = WeeksQuery(mockConnection).get(env)

        Assertions.assertEquals(sqlState.weeks[0].name, results[0].name)
        Assertions.assertEquals(sqlState.weeks[0].weekOrder, results[0].weekOrder)
        Assertions.assertEquals(sqlState.weeks[0].weekType, results[0].weekType)
    }


    @Test
    fun getWithoutArgumentsReturnsSameDataAsWith() {
        val sqlState = SQLState().apply {
            weeks.add(buildWeek("3", "REG",3, 13))
        }
        sqlState.mockSQLState(mockStatement)

        val results = WeeksQuery(mockConnection).get()

        Assertions.assertEquals(sqlState.weeks[0].name, results[0].name)
        Assertions.assertEquals(sqlState.weeks[0].weekOrder, results[0].weekOrder)
        Assertions.assertEquals(sqlState.weeks[0].weekType, results[0].weekType)
    }

    @Test
    fun getReturnsWeeksWhenThreeWeeks() {
        val sqlState = SQLState().apply {
            weeks.add(buildWeek("0", "REG",1, 1))
            weeks.add(buildWeek("4", "POST",1, 4))
            weeks.add(buildWeek("19", "PRE",0, 199))
        }
        sqlState.mockSQLState(mockStatement)

        val results = WeeksQuery(mockConnection).get(env)

        Assertions.assertEquals(sqlState.weeks.map { week -> week.name }, results.map { week -> week.name })
        Assertions.assertEquals(sqlState.weeks.map { week -> week.weekOrder }, results.map { week -> week.weekOrder })
        Assertions.assertEquals(sqlState.weeks.map { week -> week.weekType }, results.map { week -> week.weekType })
        Assertions.assertEquals(sqlState.weeks.map { week -> week.week }, results.map { week -> week.week })
    }

    private fun buildWeek(name: String, type: String, weekNumber: Int, order: Int): WeekDTO {
        return WeekDTO(name).apply {
            weekType = type
            week = weekNumber
            weekOrder = order
        }
    }
}