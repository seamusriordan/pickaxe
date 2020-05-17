@file:Suppress("SqlResolve")

package db

import SQLState
import dto.PickDTO
import dto.UserDTO
import dto.UserPicksDTO
import getEnvForWeek
import io.mockk.every
import io.mockk.mockkClass
import io.mockk.mockkStatic
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.sql.Connection
import java.sql.DriverManager
import java.sql.Statement

class UserPickQueryTest {
    private lateinit var userPickQuery: UserPickQuery
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

        userPickQuery = UserPickQuery(mockConnection)
    }

    @Test
    fun getReturnsUserPicksWithOneUserAndOnePickForWeekWithWeek0() {
        val week = "0"
        val env = getEnvForWeek(week)
        val sqlState = SQLState(week).apply {
            picks.add(UserPicksDTO(UserDTO("Seamus")).apply {
                picks.add(PickDTO("GB@CHI", "CHI"))
            })
        }
        sqlState.mockSQLState(mockStatement)

        val results = UserPickQuery(mockConnection).get(env)

        assertShallowPicksEquality(sqlState, results)
    }

    @Test
    fun getReturnsUserPicksWithOneUserAndOnePickForWeekWithWeek7() {
        val week = "7"
        val env = getEnvForWeek(week)
        val sqlState = SQLState(week).apply {
            picks.add(UserPicksDTO(UserDTO("Seamus")).apply {
                picks.add(PickDTO("GB@CHI", "CHI"))
            })
        }
        sqlState.mockSQLState(mockStatement)

        val results = UserPickQuery(mockConnection).get(env)

        assertShallowPicksEquality(sqlState, results)
    }

    @Test
    fun getReturnsUserPicksWithTwoUsersAndOnePickForWeekWithWeek0() {
        val week = "0"
        val env = getEnvForWeek(week)
        val sqlState = SQLState(week).apply {
            picks.add(UserPicksDTO(UserDTO("Seamus")).apply {
                picks.add(PickDTO("GB@CHI", "CHI"))
            })
            picks.add(UserPicksDTO(UserDTO("Sereres")).apply {
                picks.add(PickDTO("SEA@PHI", "PHI"))
            })
        }
        sqlState.mockSQLState(mockStatement)

        val results = UserPickQuery(mockConnection).get(env)

        assertShallowPicksEquality(sqlState, results)
    }


    @Test
    fun getReturnsUserPicksWithOneUserAndTwoPicksForWeekWithWeek0() {
        val week = "0"
        val env = getEnvForWeek(week)
        val sqlState = SQLState(week).apply {
            picks.add(UserPicksDTO(UserDTO("Seamus")).apply {
                picks.add(PickDTO("GB@CHI", "CHI"))
                picks.add(PickDTO("SEA@PHI", "PHI"))
            })
        }
        sqlState.mockSQLState(mockStatement)

        val results = UserPickQuery(mockConnection).get(env)

        assertShallowPicksEquality(sqlState, results)
    }

    private fun assertShallowPicksEquality(sqlState: SQLState, results: List<UserPicksDTO>) {
        assertEquals(sqlState.picks.map { x -> x.user.name }, results.map { x -> x.user.name })

        for ((index, _) in sqlState.picks.withIndex()) {
            assertEquals(sqlState.picks[index].picks.map { x -> x.game }, results[index].picks.map { x -> x.game })
            assertEquals(sqlState.picks[index].picks.map { x -> x.pick }, results[index].picks.map { x -> x.pick })
        }
    }
}


