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
        val expectedUsers = arrayListOf(UserDTO("Dave"))

        val mockResultSet = setupSQLQueryForUsers(expectedUsers)
        mockStatementToReturnUserResultSet(mockStatement, mockResultSet)


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

        val mockPickSet = setupSQLQueryForPicks(expectedPicks, week)
        mockStatementToReturnPickResultSet(mockStatement, mockPickSet, week)


        val query = UserWeekTotalQuery(mockConnection).get(env)

        assertEquals(1, query[0].games.size)
        assertEquals(gameName, query[0].games[0].name)
        assertEquals(week, query[0].games[0].week)
    }


    @Test
    fun returnsListWithUsersWhenTwoUserInUserField() {
        val env = getEnvForWeek("Week 0")
        val expectedUsers = arrayListOf(UserDTO("Dave"), UserDTO("Jack"))

        val mockResultSet = setupSQLQueryForUsers(expectedUsers)
        mockStatementToReturnUserResultSet(mockStatement, mockResultSet)


        val query = UserWeekTotalQuery(mockConnection).get(env)

        assertEquals(
            expectedUsers.map { user -> user.name },
            query.map { result -> result.user.name }
        )
        assertEquals(expectedUsers.size, query.size)
    }

    private fun mockStatementToReturnUserResultSet(statement: Statement, results: ResultSet) {
        val queryString = "SELECT name FROM users WHERE active = TRUE"
        every { statement.executeQuery(queryString) } returns results
    }

    private fun mockStatementToReturnGameResultSet(statement: Statement, results: ResultSet, week: String) {
        val queryString = "SELECT game, week, result, spread FROM games WHERE week = '$week'"
        every { statement.executeQuery(queryString) } returns results
    }

    private fun mockStatementToReturnPickResultSet(statement: Statement, results: ResultSet, week: String) {
        val queryString = "SELECT name, game, pick FROM userpicks WHERE week = '$week'"
        every { statement.executeQuery(queryString) } returns results
    }

    private fun setupSQLQueryForUsers(users: ArrayList<UserDTO>): ResultSet {
        val mockResultSet = mockkClass(ResultSet::class)
        mockNextReturnTimes(mockResultSet, users.size)

        every {
            mockResultSet.getString("name")
        } returnsMany users.map { user -> user.name }
        return mockResultSet
    }

    private fun setupSQLQueryForGames(games: ArrayList<GameDTO>): ResultSet {
        val mockResultSet = mockkClass(ResultSet::class)
        mockNextReturnTimes(mockResultSet, games.size)

        every {
            mockResultSet.getString("name")
        } returnsMany games.map { game -> game.name }

        every {
            mockResultSet.getString("week")
        } returns "Week nonce"

        every {
            mockResultSet.getDouble("spread")
        } returns 0.0

        every {
            mockResultSet.getString("result")
        } returnsMany games.map { game -> game.result }

        return mockResultSet
    }

    private fun setupSQLQueryForPicks(userPicks: ArrayList<UserPicksDTO>, week: String): ResultSet {
        val mockResultSet = mockkClass(ResultSet::class)
        mockNextReturnTimes(mockResultSet, userPicks.size)

        for (userPick in userPicks) {
            setupSQLQueryForUser(userPick, mockResultSet)
        }

        return mockResultSet
    }

    private fun setupSQLQueryForUser(
        userPicks: UserPicksDTO,
        resultSet: ResultSet
    ) {
        val names = ArrayList<String>()
        for (i in 0 until userPicks.picks.size) {
            names.add(userPicks.user.name)
        }

        every {
            resultSet.getString("name")
        } returnsMany names

        every {
            resultSet.getString("game")
        } returnsMany userPicks.picks.map { pick -> pick.game }

        every {
            resultSet.getString("pick")
        } returnsMany userPicks.picks.map { pick -> pick.pick }
    }

}