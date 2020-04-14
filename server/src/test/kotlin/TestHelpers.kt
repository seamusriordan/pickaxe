import dto.GameDTO
import dto.UserDTO
import dto.UserPicksDTO
import dto.WeekDTO
import graphql.schema.DataFetchingEnvironment
import graphql.schema.DataFetchingEnvironmentImpl
import io.mockk.every
import io.mockk.mockkClass
import java.sql.ResultSet
import java.sql.Statement

fun getEnvForWeek(week: String): DataFetchingEnvironment {
    val arguments = HashMap<String, Any>().apply {
        set("week", week)
    }
    return setupEnvForArguments(arguments)
}

private fun setupEnvForArguments(arguments: HashMap<String, Any>): DataFetchingEnvironment {
    return DataFetchingEnvironmentImpl
        .newDataFetchingEnvironment()
        .arguments(arguments)
        .build()
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
    val queryString = "SELECT game, week, result, spread FROM games WHERE week = '$week'"
    every { statement.executeQuery(queryString) } returns results
}

fun mockStatementToReturnPickResultSet(statement: Statement, results: ResultSet, week: String) {
    val queryString = "SELECT name, game, pick FROM userpicks WHERE week = '$week'"
    every { statement.executeQuery(queryString) } returns results
}

fun mockStatementToReturnWeekResultSet(statement: Statement, results: ResultSet) {
    every { statement.executeQuery("SELECT name, week_order FROM weeks") } returns results

}

fun setupSQLQueryForUsers(users: ArrayList<UserDTO>): ResultSet {
    val mockResultSet = mockkClass(ResultSet::class)
    mockNextReturnTimes(mockResultSet, users.size)

    every {
        mockResultSet.getString("name")
    } returnsMany users.map { user -> user.name }
    return mockResultSet
}

fun setupSQLQueryForGamesWithNonNullFields(games: ArrayList<GameDTO>): ResultSet {
    val mockResultSet = mockkClass(ResultSet::class)
    mockNextReturnTimes(mockResultSet, games.size)

    every {
        mockResultSet.getString("game")
    } returnsMany games.map { game -> game.name }

    every {
        mockResultSet.getString("week")
    } returnsMany games.map { game -> game.week }

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

fun setupSQLQueryForPicks(allUserPicks: ArrayList<UserPicksDTO>): ResultSet {
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

fun setupSQLQueryForWeeks(weeks: ArrayList<WeekDTO>): ResultSet {
    val mockResultSet = mockkClass(ResultSet::class)
    mockNextReturnTimes(mockResultSet, weeks.size)
    every {
        mockResultSet.getString("name")
    } returnsMany weeks.map { week -> week.name }

    every {
        mockResultSet.getInt("week_order")
    } returnsMany weeks.map { week -> week.weekOrder!! }

    return mockResultSet
}

private fun totalNumberOfPicks(userPicks: ArrayList<UserPicksDTO>) =
    userPicks.sumBy { picks -> picks.picks.size }