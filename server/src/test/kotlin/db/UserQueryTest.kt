package db

import UserDTO
import io.mockk.every
import io.mockk.mockkClass
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Statement

class UserQueryTest {
    @Test
    fun getActiveUsersReturnsUsersFromDatabaseWhenSingleUser() {
        val expectedUsers: ArrayList<UserDTO> = ArrayList(1)
        expectedUsers.add(UserDTO("Seamus"))

        val mockResultSet = mockkClass(ResultSet::class)
        every { mockResultSet.next() } returns true andThen false
        every { mockResultSet.getString("name") } returns expectedUsers[0].name

        val mockStatement = mockkClass(Statement::class)
        every { mockStatement.executeQuery(any()) } returns mockResultSet

        val mockConnection = mockkClass(Connection::class)
        every { mockConnection.createStatement() } returns mockStatement

        val results = UserQuery().getActiveUsers()

        assertEquals(expectedUsers.map { x -> x.name }, results.map { x -> x.name })
    }
}