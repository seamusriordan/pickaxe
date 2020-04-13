package db

import dto.UserDTO
import getEnvForWeek
import io.mockk.every
import io.mockk.mockkClass
import io.mockk.mockkStatic
import mockNextReturnTimes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.Statement

class UserWeekTotalQueryTest {
    private lateinit var mockStatement: Statement
    private lateinit var mockConnection: Connection

    @BeforeEach
    fun setup() {
        mockkStatic("java.sql.DriverManager")
        mockConnection = mockkClass(Connection::class)
        mockStatement = mockkClass(Statement::class)

        every { DriverManager.getConnection(any()) } returns null
        every { DriverManager.getConnection(any(), any()) } returns mockConnection
        every { mockConnection.createStatement() } returns mockStatement
    }

    @Test
    fun returnsListWithUsersWhenOneUserInUserField() {
        val env = getEnvForWeek("Week 0")
        val expectedUser = UserDTO("Dave")

        val mockResultSet = mockkClass(ResultSet::class)
        mockNextReturnTimes(mockResultSet, 1)
        every { mockResultSet.getString("name") } returns expectedUser.name
        val queryString = "SELECT name FROM users WHERE active = true"
        every { mockStatement.executeQuery(queryString) } returns mockResultSet


        val query = UserWeekTotalQuery(mockConnection).get(env)

        assertEquals(expectedUser.name, query.first().user.name)
        assertEquals(1, query.size)
    }
}