import dto.GameDTO
import dto.UserDTO
import dto.UserPicksDTO
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

fun setupSQLQueryForUsers(users: ArrayList<UserDTO>): ResultSet {
    val mockResultSet = mockkClass(ResultSet::class)
    mockNextReturnTimes(mockResultSet, users.size)

    every {
        mockResultSet.getString("name")
    } returnsMany users.map { user -> user.name }
    return mockResultSet
}

fun setupSQLQueryForGames(games: ArrayList<GameDTO>): ResultSet {
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
    } returns 0.0

    every {
        mockResultSet.getString("result")
    } returnsMany games.map { game -> game.result }

    every {
        mockResultSet.wasNull()
    } returns false

    return mockResultSet
}

fun setupSQLQueryForPicks(userPicks: ArrayList<UserPicksDTO>): ResultSet {
    val mockResultSet = mockkClass(ResultSet::class)
    mockNextReturnTimes(mockResultSet, totalNumberOfPicks(userPicks))

    for (userPick in userPicks) {
        setupSQLQueryForUser(userPick, mockResultSet)
    }

    return mockResultSet
}

private fun totalNumberOfPicks(userPicks: ArrayList<UserPicksDTO>) =
    userPicks.sumBy { picks -> picks.picks.size }

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