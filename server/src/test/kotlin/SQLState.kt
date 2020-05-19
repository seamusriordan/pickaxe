import dto.GameDTO
import dto.UserDTO
import dto.UserPicksDTO
import dto.WeekDTO
import io.mockk.every
import io.mockk.mockkClass
import java.sql.ResultSet
import java.sql.Statement
import java.time.OffsetDateTime
import java.util.*
import kotlin.collections.ArrayList

class SQLState(private val week: String = "0") {
    var users: ArrayList<UserDTO> = arrayListOf()
    var games: ArrayList<GameDTO> = arrayListOf()
    var picks: ArrayList<UserPicksDTO> = arrayListOf()
    var weeks: ArrayList<WeekDTO> = arrayListOf()

    fun mockSQLState(
        statement: Statement
    ) {
        val mockUserSet = setupSQLQueryForUsers(users)
        mockStatementToReturnUserResultSet(
            statement, mockUserSet
        )

        val mockGameSet = setupSQLQueryForGamesWithNonNullFields(games)
        mockStatementToReturnGameResultSet(statement, mockGameSet, week)

        val mockPickSet = setupSQLQueryForPicks(picks)
        mockStatementToReturnPickResultSet(statement, mockPickSet, week)

        val mockWeekSet = setupSQLQueryForWeeks(weeks)
        mockStatementToReturnWeekResultSet(statement, mockWeekSet)
    }

}

fun mockNextReturnTimes(mockResultSet: ResultSet, times: Int) {
    val trues = ArrayList<Boolean>(0)
    for (i in 1..times) {
        trues.add(true)
    }

    every {
        mockResultSet.next()
    } returnsMany trues + listOf(false)
}

fun mockStatementToReturnUserResultSet(statement: Statement, results: ResultSet) {
    val queryString = "SELECT name FROM users WHERE active = TRUE"
    every { statement.executeQuery(queryString) } returns results
}

fun mockStatementToReturnGameResultSet(statement: Statement, results: ResultSet, week: String) {
    val queryString = "SELECT game, week, id, gametime, result, spread FROM games WHERE week = '$week'"
    every { statement.executeQuery(queryString) } returns results
}

fun mockStatementToReturnPickResultSet(statement: Statement, results: ResultSet, week: String) {
    val queryString = "SELECT name, game, pick FROM userpicks WHERE week = '$week'"
    every { statement.executeQuery(queryString) } returns results
}

fun mockStatementToReturnWeekResultSet(statement: Statement, results: ResultSet) {
    every { statement.executeQuery("SELECT name, week_type, week, week_order FROM weeks") } returns results

}

fun setupSQLQueryForUsers(users: List<UserDTO>): ResultSet {
    val mockResultSet = mockkClass(ResultSet::class)
    mockNextReturnTimes(mockResultSet, users.size)

    every {
        mockResultSet.getString("name")
    } returnsMany users.map { user -> user.name }
    return mockResultSet
}

fun setupSQLQueryForGamesWithNonNullFields(games: List<GameDTO>): ResultSet {
    val mockResultSet = mockkClass(ResultSet::class)
    mockNextReturnTimes(mockResultSet, games.size)

    every {
        mockResultSet.getString("game")
    } returnsMany games.map { game -> game.name }

    every {
        mockResultSet.getString("week")
    } returnsMany games.map { game -> game.week }

    every {
        mockResultSet.getObject("id", UUID::class.java)
    } returnsMany games.map { game -> game.id }

    every {
        mockResultSet.getObject("gametime", OffsetDateTime::class.java)
    } returnsMany games.map { game -> game.gameTime }

    every {
        mockResultSet.getDouble("spread")
    } returnsMany games.map {
        if (it.spread == null) {
            0.0
        } else {
            it.spread!!
        }
    }

    every {
        mockResultSet.getString("result")
    } returnsMany games.map { game -> game.result }

    every {
        mockResultSet.wasNull()
    } returns false

    return mockResultSet
}

fun setupSQLQueryForPicks(allUserPicks: List<UserPicksDTO>): ResultSet {
    val resultSet = mockkClass(ResultSet::class)
    mockNextReturnTimes(resultSet, totalNumberOfPicks(allUserPicks))

    val names = ArrayList<String>()
    val games = ArrayList<String>()
    val picks = ArrayList<String>()
    for (userPicks in allUserPicks) {
        for (i in 0 until userPicks.picks.size) {
            names.add(userPicks.user.name)
        }
        for (pick in userPicks.picks) {
            games.add(pick.game)
            picks.add(pick.pick)
        }
    }

    every {
        resultSet.getString("name")
    } returnsMany names

    every {
        resultSet.getString("game")
    } returnsMany games

    every {
        resultSet.getString("pick")
    } returnsMany picks

    return resultSet
}

fun setupSQLQueryForWeeks(weeks: List<WeekDTO>): ResultSet {
    val mockResultSet = mockkClass(ResultSet::class)
    mockNextReturnTimes(mockResultSet, weeks.size)
    every {
        mockResultSet.getString("name")
    } returnsMany weeks.map { week -> week.name }

    every {
        mockResultSet.getString("week_type")
    } returnsMany weeks.map { week -> week.weekType!! }

    every {
        mockResultSet.getInt("week")
    } returnsMany weeks.map { week -> week.week!! }

    every {
        mockResultSet.getInt("week_order")
    } returnsMany weeks.map { week -> week.weekOrder!! }

    return mockResultSet
}

private fun totalNumberOfPicks(userPicks: List<UserPicksDTO>) =
    userPicks.sumBy { picks -> picks.picks.size }