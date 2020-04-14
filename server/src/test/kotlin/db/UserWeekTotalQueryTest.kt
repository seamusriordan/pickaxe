package db

import dto.GameDTO
import dto.PickDTO
import dto.UserDTO
import dto.UserPicksDTO
import getEnvForWeek
import io.mockk.every
import io.mockk.mockkClass
import io.mockk.mockkStatic
import mockNextReturnTimes
import mockStatementToReturnGameResultSet
import mockStatementToReturnPickResultSet
import mockStatementToReturnUserResultSet
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import setupSQLQueryForGames
import setupSQLQueryForPicks
import setupSQLQueryForUsers
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
        val week = "Week 0"
        val env = getEnvForWeek(week)
        val expectedUsers = arrayListOf(UserDTO("Dave"))

        val mockResultSet = setupSQLQueryForUsers(expectedUsers)
        mockStatementToReturnUserResultSet(mockStatement, mockResultSet)

        val mockGameSet = setupSQLQueryForGames(arrayListOf())
        mockStatementToReturnGameResultSet(mockStatement, mockGameSet, week)

        val mockPickSet = setupSQLQueryForPicks(arrayListOf())
        mockStatementToReturnPickResultSet(mockStatement, mockPickSet, week)

        val query = UserWeekTotalQuery(mockConnection).get(env)

        assertEquals(
            expectedUsers.map { user -> user.name },
            query.map { result -> result.user.name }
        )
        assertEquals(expectedUsers.size, query.size)
    }

    @Test
    fun oneUserWithOneCorrectPickHasGameAndTotalInResponse() {
        val week = "Week 0"
        val env = getEnvForWeek(week)
        val expectedUsers = arrayListOf(UserDTO("Dave"))
        val gameName = "GB@CHI"
        val pick = "CHI"

        val expectedGames = arrayListOf(
            GameDTO(gameName, week).apply {
                result = pick
            }
        )
        val expectedPicks = arrayListOf(
            UserPicksDTO(expectedUsers[0]).apply {
                picks.add(PickDTO(gameName, pick))
            })

        val mockUserSet = setupSQLQueryForUsers(expectedUsers)
        mockStatementToReturnUserResultSet(mockStatement, mockUserSet)

        val mockGameSet = setupSQLQueryForGames(expectedGames)
        mockStatementToReturnGameResultSet(mockStatement, mockGameSet, week)

        val mockPickSet = setupSQLQueryForPicks(expectedPicks)
        mockStatementToReturnPickResultSet(mockStatement, mockPickSet, week)


        val query = UserWeekTotalQuery(mockConnection).get(env)

        assertEquals(1, query[0].games.size)
        assertEquals(gameName, query[0].games[0].name)
        assertEquals(week, query[0].games[0].week)
    }

    @Test
    fun oneUserWithWeek4CorrectPickHasGameAndTotalInResponse() {
        val week = "Week 4"
        val env = getEnvForWeek(week)
        val expectedUsers = arrayListOf(UserDTO("Dave"))
        val gameName = "GB@CHI"
        val pick = "CHI"

        val expectedGames = arrayListOf(
            GameDTO(gameName, week).apply {
                result = pick
            }
        )
        val expectedPicks = arrayListOf(
            UserPicksDTO(expectedUsers[0]).apply {
                picks.add(PickDTO(gameName, pick))
            })

        val mockUserSet = setupSQLQueryForUsers(expectedUsers)
        mockStatementToReturnUserResultSet(mockStatement, mockUserSet)

        val mockGameSet = setupSQLQueryForGames(expectedGames)
        mockStatementToReturnGameResultSet(mockStatement, mockGameSet, week)

        val mockPickSet = setupSQLQueryForPicks(expectedPicks)
        mockStatementToReturnPickResultSet(mockStatement, mockPickSet, week)


        val query = UserWeekTotalQuery(mockConnection).get(env)

        assertEquals(1, query[0].games.size)
        assertEquals(gameName, query[0].games[0].name)
        assertEquals(week, query[0].games[0].week)
    }

    @Test
    fun oneUserWithWrongPickDoesNotHaveTotalInResponse() {
        val week = "Week 4"
        val env = getEnvForWeek(week)
        val expectedUsers = arrayListOf(UserDTO("Dave"))
        val gameName = "GB@CHI"

        val expectedGames = arrayListOf(
            GameDTO(gameName, week).apply {
                result = "CHI"
            }
        )
        val expectedPicks = arrayListOf(
            UserPicksDTO(expectedUsers[0]).apply {
                picks.add(PickDTO(gameName, "No one"))
            })

        val mockUserSet = setupSQLQueryForUsers(expectedUsers)
        mockStatementToReturnUserResultSet(mockStatement, mockUserSet)

        val mockGameSet = setupSQLQueryForGames(expectedGames)
        mockStatementToReturnGameResultSet(mockStatement, mockGameSet, week)

        val mockPickSet = setupSQLQueryForPicks(expectedPicks)
        mockStatementToReturnPickResultSet(mockStatement, mockPickSet, week)


        val query = UserWeekTotalQuery(mockConnection).get(env)

        assertEquals(0, query[0].games.size)
    }

    @Test
    fun nullResultDoesNotMatchNullPick() {
        val week = "Week 4"
        val env = getEnvForWeek(week)

        val expectedUsers = arrayListOf(UserDTO("Dave"))
        val expectedGames = arrayListOf(GameDTO("GB@CHI", week))
        val expectedPicks = arrayListOf(UserPicksDTO(expectedUsers[0]))

        val mockUserSet = setupSQLQueryForUsers(expectedUsers)
        mockStatementToReturnUserResultSet(mockStatement, mockUserSet)

        val mockGameSet = setupSQLQueryForGames(expectedGames)
        mockStatementToReturnGameResultSet(mockStatement, mockGameSet, week)

        val mockPickSet = setupSQLQueryForPicks(expectedPicks)
        mockStatementToReturnPickResultSet(mockStatement, mockPickSet, week)


        val query = UserWeekTotalQuery(mockConnection).get(env)

        assertEquals(0, query[0].games.size)
    }


    @Test
    fun returnsListWithUsersWhenTwoUserInUserField() {
        val week = "Week 0"
        val env = getEnvForWeek(week)
        val expectedUsers = arrayListOf(UserDTO("Dave"), UserDTO("Jack"))

        val mockResultSet = setupSQLQueryForUsers(expectedUsers)
        mockStatementToReturnUserResultSet(mockStatement, mockResultSet)

        val mockGameSet = setupSQLQueryForGames(arrayListOf())
        mockStatementToReturnGameResultSet(mockStatement, mockGameSet, week)

        val mockPickSet = setupSQLQueryForPicks(arrayListOf())
        mockStatementToReturnPickResultSet(mockStatement, mockPickSet, week)

        val query = UserWeekTotalQuery(mockConnection).get(env)

        assertEquals(
            expectedUsers.map { user -> user.name },
            query.map { result -> result.user.name }
        )
        assertEquals(expectedUsers.size, query.size)
    }



}