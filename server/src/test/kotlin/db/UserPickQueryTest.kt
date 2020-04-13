@file:Suppress("SqlResolve")

package db

import dto.PickDTO
import dto.UserDTO
import dto.UserPicksDTO
import getEnvForWeek
import io.mockk.MockKAdditionalAnswerScope
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
        val expectedPicks: ArrayList<UserPicksDTO> = ArrayList(1)
        expectedPicks.add(UserPicksDTO(UserDTO("Seamus")))
        expectedPicks[0].picks.add(PickDTO("GB@CHI", "CHI"))

        setupMocksForPicks(expectedPicks, setupResultMockForManyUsersOnePick, week)
        val env = getEnvForWeek(week)

        val results = UserPickQuery(mockConnection).get(env)

        assertEquals(expectedPicks.map { x -> x.user.name }, results.map { x -> x.user.name })
        assertEquals(expectedPicks[0].picks.map { x -> x.game }, results[0].picks.map { x -> x.game })
        assertEquals(expectedPicks[0].picks.map { x -> x.pick }, results[0].picks.map { x -> x.pick })
    }

    @Test
    fun getReturnsUserPicksWithOneUserAndOnePickForWeekWithWeek7() {
        val week = "7"
        val expectedPicks: ArrayList<UserPicksDTO> = ArrayList(1)
        expectedPicks.add(UserPicksDTO(UserDTO("Seamus")))
        expectedPicks[0].picks.add(PickDTO("GB@CHI", "CHI"))

        setupMocksForPicks(expectedPicks, setupResultMockForManyUsersOnePick, week)
        val env = getEnvForWeek(week)

        val results = UserPickQuery(mockConnection).get(env)

        assertEquals(expectedPicks.map { x -> x.user.name }, results.map { x -> x.user.name })
        assertEquals(expectedPicks[0].picks.map { x -> x.game }, results[0].picks.map { x -> x.game })
        assertEquals(expectedPicks[0].picks.map { x -> x.pick }, results[0].picks.map { x -> x.pick })
    }

    @Test
    fun getReturnsUserPicksWithTwoUsersAndOnePickForWeekWithWeek0() {
        val week = "0"
        val expectedPicks: ArrayList<UserPicksDTO> = ArrayList(1)
        expectedPicks.add(UserPicksDTO(UserDTO("Seamus")))
        expectedPicks[0].picks.add(PickDTO("GB@CHI", "CHI"))
        expectedPicks.add(UserPicksDTO(UserDTO("Sereres")))
        expectedPicks[1].picks.add(PickDTO("SEA@PHI", "PHI"))

        setupMocksForPicks(expectedPicks, setupResultMockForManyUsersOnePick, week)
        val env = getEnvForWeek(week)

        val results = UserPickQuery(mockConnection).get(env)

        assertEquals(expectedPicks.map { x -> x.user.name }, results.map { x -> x.user.name })
        assertEquals(expectedPicks[0].picks.map { x -> x.game }, results[0].picks.map { x -> x.game })
        assertEquals(expectedPicks[0].picks.map { x -> x.pick }, results[0].picks.map { x -> x.pick })
        assertEquals(expectedPicks[1].picks.map { x -> x.game }, results[1].picks.map { x -> x.game })
        assertEquals(expectedPicks[1].picks.map { x -> x.pick }, results[1].picks.map { x -> x.pick })
    }


    @Test
    fun getReturnsUserPicksWithOneUserAndTwoPicksForWeekWithWeek0() {
        val week = "0"
        val expectedPicks: ArrayList<UserPicksDTO> = ArrayList(1)
        expectedPicks.add(UserPicksDTO(UserDTO("Seamus")))
        expectedPicks[0].picks.add(PickDTO("GB@CHI", "CHI"))
        expectedPicks[0].picks.add(PickDTO("SEA@PHI", "PHI"))

        setupMocksForPicks(expectedPicks, setMockResultsForOneUserManyPicks, week)
        val env = getEnvForWeek(week)

        val results = UserPickQuery(mockConnection).get(env)

        assertEquals(expectedPicks.map { x -> x.user.name }, results.map { x -> x.user.name })
        assertEquals(expectedPicks[0].picks.map { x -> x.game }, results[0].picks.map { x -> x.game })
        assertEquals(expectedPicks[0].picks.map { x -> x.pick }, results[0].picks.map { x -> x.pick })
    }


    private fun setupMockForQueryWithWeek(mockResultSet: ResultSet, week: String) {
        val queryString = "SELECT name, game, pick FROM userpicks WHERE week = '$week'"
        every { mockStatement.executeQuery(queryString) } returns mockResultSet
    }


    private fun setupMocksForPicks(
        expectedPicks: ArrayList<UserPicksDTO>,
        mockSetter: (ResultSet, ArrayList<UserPicksDTO>) -> MockKAdditionalAnswerScope<String, String>,
        week: String
    ) {
        val mockResultSet = mockkClass(ResultSet::class)
        mockSetter(mockResultSet, expectedPicks)
        setupMockForQueryWithWeek(mockResultSet, week)
    }

    private val setupResultMockForManyUsersOnePick = { mockResultSet: ResultSet,
                                                       expectedPicks: ArrayList<UserPicksDTO>
        ->
        mockNextReturnTimes(mockResultSet, expectedPicks.size)
        every { mockResultSet.getString("name") } returnsMany expectedPicks.map { pick -> pick.user.name }
        every { mockResultSet.getString("game") } returnsMany expectedPicks.map { pick -> pick.picks[0].game }
        every { mockResultSet.getString("pick") } returnsMany expectedPicks.map { pick -> pick.picks[0].pick }
    }

    private var setMockResultsForOneUserManyPicks = { mockResultSet: ResultSet,
                                                      expectedPicks: ArrayList<UserPicksDTO>
        ->
        mockNextReturnTimes(mockResultSet, expectedPicks[0].picks.size)
        every { mockResultSet.getString("name") } returns expectedPicks[0].user.name
        every { mockResultSet.getString("game") } returnsMany expectedPicks[0].picks.map { pick -> pick.game}
        every { mockResultSet.getString("pick") } returnsMany expectedPicks[0].picks.map { pick -> pick.pick}
    }
}


