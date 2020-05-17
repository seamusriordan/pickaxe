@file:Suppress("SqlResolve")

package db

import SQLState
import dto.UserDTO
import graphql.schema.DataFetchingEnvironment
import graphql.schema.DataFetchingEnvironmentImpl
import io.mockk.every
import io.mockk.mockkClass
import io.mockk.mockkStatic
import mockNextReturnTimes
import mockStatementToReturnUserResultSet
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import setupSQLQueryForUsers
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.Statement

class UserQueryTest {
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
    fun getReturnsActiveUsersFromDatabaseWhenSingleUser() {
        val sqlState = SQLState().apply {
            users.add(UserDTO("Seamus"))
        }
        sqlState.mockSQLState(mockStatement)

        val results = UserQuery(mockConnection).get(env)

        assertEquals(sqlState.users.map { x -> x.name }, results.map { x -> x.name })
    }

    @Test
    fun getReturnsActiveUsersFromDatabaseWhenTwoUsers() {
        val sqlState = SQLState().apply {
            users.add(UserDTO("Stebe"))
            users.add(UserDTO("Dabe"))
        }
        sqlState.mockSQLState(mockStatement)

        val results = UserQuery(mockConnection).get(env)

        assertEquals(sqlState.users.map { x -> x.name }, results.map { x -> x.name })
    }
}