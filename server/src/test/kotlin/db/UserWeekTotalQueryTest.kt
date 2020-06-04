package db

import SQLState
import dto.*
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
        val week = "Week 0"
        val env = getEnvForWeek(week)
        val expectedUsers = arrayListOf(UserDTO("Dave"))
        val sqlState = SQLState(week).apply {
            users = expectedUsers
        }
        sqlState.mockSQLState(mockStatement)

        val query = UserWeekTotalQuery(mockConnection).get(env)

        assertEquals(
            expectedUsers.map { user -> user.name },
            query.map { result -> result.user.name }
        )
        assertEquals(expectedUsers.size, query.size)
    }

    @Test
    fun returnsListWithUsersWhenOneUserInUserFieldWithDifferentWeek() {
        val week = "Week 4"
        val env = getEnvForWeek(week)
        val expectedUsers = arrayListOf(UserDTO("Darve"))
        val sqlState = SQLState(week).apply {
            users = expectedUsers
        }
        sqlState.mockSQLState(mockStatement)

        val query = UserWeekTotalQuery(mockConnection).get(env)

        assertEquals(
            expectedUsers.map { user -> user.name },
            query.map { result -> result.user.name }
        )
        assertEquals(expectedUsers.size, query.size)
    }

    @Test
    fun returnsSameListWithStringArugmentForWeek() {
        val week = "Week 0"
        val expectedUsers = arrayListOf(UserDTO("Dave"))
        val sqlState = SQLState(week).apply {
            users = expectedUsers
        }
        sqlState.mockSQLState(mockStatement)

        val query = UserWeekTotalQuery(mockConnection).get(week)

        assertEquals(
            expectedUsers.map { user -> user.name },
            query.map { result -> result.user.name }
        )
        assertEquals(expectedUsers.size, query.size)
    }

    @Test
    fun oneUserWithOneCorrectPickHasGameAndTotalInResponse() {
        val week = "Week 0"
        val gameName = "GB@CHI"
        val pick = "CHI"
        val sqlState = SQLState(week).apply {
            users.add(UserDTO("Dave"))
            games.add(GameDTO(gameName, week).apply {
                result = pick
            })
            picks.add(UserPicksDTO(users[0]).apply {
                picks.add(PickDTO(gameName, pick))
            })
        }
        sqlState.mockSQLState(mockStatement)

        val query = UserWeekTotalQuery(mockConnection).get(week)

        assertQueryResultIsGameAndWeek(query, gameName, week)
    }

    @Test
    fun correctPickWithDifferentCaseSensitivityMatches() {
        val week = "Week 0"
        val gameName = "GB@CHI"
        val sqlState = SQLState(week).apply {
            users.add(UserDTO("DaeV"))
            games.add(GameDTO(gameName, week).apply {
                result = "CHI"
            })
            picks.add(UserPicksDTO(users[0]).apply {
                picks.add(PickDTO(gameName, "ChI"))
            })
        }
        sqlState.mockSQLState(mockStatement)

        val query = UserWeekTotalQuery(mockConnection).get(week)

        assertQueryResultIsGameAndWeek(query, gameName, week)
    }

    @Test
    fun correctPickWithLeadingWhitespaceMatches() {
        val week = "Week 0"
        val gameName = "GB@CHI"
        val sqlState = SQLState(week).apply {
            users.add(UserDTO(" DaeV"))
            games.add(GameDTO(gameName, week).apply {
                result = "CHI"
            })
            picks.add(UserPicksDTO(users[0]).apply {
                picks.add(PickDTO(gameName, " ChI"))
            })
        }
        sqlState.mockSQLState(mockStatement)

        val query = UserWeekTotalQuery(mockConnection).get(week)

        assertQueryResultIsGameAndWeek(query, gameName, week)
    }

    @Test
    fun oneUserWithWeek4CorrectPickHasGameAndTotalInResponse() {
        val week = "Week 4"
        val gameName = "GB@CHI"
        val pick = "CHI"
        val sqlState = SQLState(week).apply {
            users.add(UserDTO("Dave"))
            games.add(GameDTO(gameName, week).apply {
                result = pick
            })
            picks.add(UserPicksDTO(users[0]).apply {
                picks.add(PickDTO(gameName, pick))
            })
        }
        sqlState.mockSQLState(mockStatement)

        val query = UserWeekTotalQuery(mockConnection).get(week)

        assertQueryResultIsGameAndWeek(query, gameName, week)
    }

    @Test
    fun oneUserWithWrongPickDoesNotHaveTotalInResponse() {
        val week = "Week 4"
        val gameName = "GB@CHI"
        val sqlState = SQLState(week).apply {
            users.add(UserDTO("Dave"))
            games.add(GameDTO(gameName, week).apply {
                result = "CHI"
            })
            picks.add(UserPicksDTO(users[0]).apply {
                picks.add(PickDTO(gameName, "No one"))
            })
        }
        sqlState.mockSQLState(mockStatement)

        val query = UserWeekTotalQuery(mockConnection).get(week)

        assertEquals(0, query[0].games.size)
    }

    @Test
    fun nullResultDoesNotMatchNullPick() {
        val week = "Week 4"
        val sqlState = SQLState(week).apply {
            users.add(UserDTO("Dave"))
            games.add(GameDTO("GB@CHI", week))
            picks.add(UserPicksDTO(users[0]))
        }
        sqlState.mockSQLState(mockStatement)

        val query = UserWeekTotalQuery(mockConnection).get(week)

        assertEquals(0, query[0].games.size)
    }

    @Test
    fun returnsListWithUsersWhenTwoUserInUserField() {
        val week = "Week 0"
        val sqlState = SQLState(week).apply {
            users.add(UserDTO("Dave"))
            users.add(UserDTO("Jack"))
        }
        sqlState.mockSQLState(mockStatement)

        val query = UserWeekTotalQuery(mockConnection).get(week)

        assertEquals(
            sqlState.users.map { user -> user.name },
            query.map { result -> result.user.name }
        )
        assertEquals(sqlState.users.size, query.size)
    }


    private fun assertQueryResultIsGameAndWeek(
        query: List<UserWeekTotalDTO>,
        gameName: String,
        week: String
    ) {
        assertEquals(1, query[0].games.size)
        assertEquals(gameName, query[0].games[0].name)
        assertEquals(week, query[0].games[0].week)
    }
}
