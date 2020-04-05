@file:Suppress("SqlResolve")

package db

import UserDTO
import io.mockk.every
import io.mockk.mockkClass
import io.mockk.mockkStatic
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.Statement

class UserQueryTest {
    private lateinit var mockStatement: Statement
    private lateinit var mockConnection: Connection

    @BeforeEach
    fun beforeEach() {
        mockkStatic("java.sql.DriverManager")
        mockConnection = mockkClass(Connection::class)
        mockStatement = mockkClass(Statement::class)

        every { DriverManager.getConnection(any()) } returns null
        every { DriverManager.getConnection(any(), any()) } returns mockConnection
        every { mockConnection.createStatement() } returns mockStatement
    }

    @Test
    fun getActiveUsersReturnsUsersFromDatabaseWhenSingleUser() {
        val expectedUsers: ArrayList<UserDTO> = ArrayList(1)
        expectedUsers.add(UserDTO("Seamus"))
        val mockResultSet = mockkClass(ResultSet::class)
        every { mockResultSet.next()
        } returns true andThen false;
        every { mockResultSet.getString("name")
        } returns expectedUsers[0].name
        every { mockStatement.executeQuery("SELECT name FROM users WHERE active = TRUE") } returns mockResultSet

        val results = UserQuery(mockConnection).getActiveUsers()

        assertEquals(expectedUsers.map { x -> x.name }, results.map { x -> x.name })
    }

    @Test
    fun getActiveUsersReturnsUsersFromDatabaseWhenTwoUsers() {
        val expectedUsers: ArrayList<UserDTO> = ArrayList(2)
        expectedUsers.add(UserDTO("Stebe"))
        expectedUsers.add(UserDTO("Dave"))
        val mockResultSet = mockkClass(ResultSet::class)
        every { mockResultSet.next()
        } returns true andThen true andThen false
        every { mockResultSet.getString("name")
        } returns expectedUsers[0].name andThen expectedUsers[1].name
        every { mockStatement.executeQuery("SELECT name FROM users WHERE active = TRUE") } returns mockResultSet

        val results = UserQuery(mockConnection).getActiveUsers()

        assertEquals(expectedUsers.map { x -> x.name }, results.map { x -> x.name })
    }
}