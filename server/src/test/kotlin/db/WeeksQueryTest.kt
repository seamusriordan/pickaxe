package db

import dto.WeekDTO
import graphql.schema.DataFetchingEnvironment
import graphql.schema.DataFetchingEnvironmentImpl
import io.mockk.every
import io.mockk.mockkClass
import io.mockk.mockkStatic
import mockNextReturnTimes
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
        addWeekWithNameAndOrder(expectedWeeks, "3", 3)

        val mockResultSet = mockkClass(ResultSet::class)
        mockNextReturnTimes(mockResultSet, 1)

        every {
            mockResultSet.getString("name")
        } returns expectedWeeks[0].name

        every {
            mockResultSet.getInt("week_order")
        } returns expectedWeeks[0].weekOrder!!

        every { mockStatement.executeQuery("SELECT name, week_order FROM weeks") } returns mockResultSet


        val results = WeeksQuery(mockConnection).get(env)

        Assertions.assertEquals(expectedWeeks[0].name, results[0].name)
        Assertions.assertEquals(expectedWeeks[0].weekOrder, results[0].weekOrder)
    }

    @Test
    fun getReturnsWeeksWhenThreeWeeks() {
        val expectedWeeks = ArrayList<WeekDTO>(1)
        addWeekWithNameAndOrder(expectedWeeks, "0", 1)
        addWeekWithNameAndOrder(expectedWeeks, "4", 4)
        addWeekWithNameAndOrder(expectedWeeks, "19", 199)

        val mockResultSet = mockkClass(ResultSet::class)
        mockNextReturnTimes(mockResultSet, 3)

        every {
            mockResultSet.getString("name")
        } returnsMany expectedWeeks.map { week -> week.name }

        every {
            mockResultSet.getInt("week_order")
        } returnsMany expectedWeeks.map { week -> week.weekOrder!! }

        every { mockStatement.executeQuery("SELECT name, week_order FROM weeks") } returns mockResultSet

        val results = WeeksQuery(mockConnection).get(env)

        Assertions.assertEquals(expectedWeeks.map { week -> week.name }, results.map { week -> week.name })
        Assertions.assertEquals(expectedWeeks.map { week -> week.weekOrder }, results.map { week -> week.weekOrder })
    }



    private fun addWeekWithNameAndOrder(expectedWeeks: ArrayList<WeekDTO>, name: String, order: Int) {
        val week = WeekDTO(name).apply {
            weekOrder = order
        }
        expectedWeeks.add(week)
    }
}