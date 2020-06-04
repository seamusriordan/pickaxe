@file:Suppress("SqlResolve")

package db

import dto.PickDTO
import dto.UserDTO
import dto.UserPicksDTO
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import java.sql.Connection
import java.sql.ResultSet

class UserPickQuery(private val connection: Connection) : DataFetcher<List<UserPicksDTO>> {
    override fun get(environment: DataFetchingEnvironment): List<UserPicksDTO> {

        val week = environment.arguments["week"] as String

        return getPicksForWeek(week)
    }

    fun getPicksForWeek(week: String): List<UserPicksDTO> {
        val statement = connection.createStatement()
        val queryString = "SELECT name, game, pick FROM userpicks WHERE week = '$week'"
        val queryResult = statement.executeQuery(queryString)

        return buildUserPicksForQueryResult(queryResult)
    }

    private fun buildUserPicksForQueryResult(queryResult: ResultSet): ArrayList<UserPicksDTO> {
        val allUserPicks = ArrayList<UserPicksDTO>(0)
        while (queryResult.next()) {
            val name = queryResult.getString("name")
            val userPicks = getNewOrExistingPicksForUser(allUserPicks, name)
            addQueryResultToUserPicks(queryResult, userPicks)
        }
        return allUserPicks
    }

    private fun getNewOrExistingPicksForUser(
        allUserPicks: ArrayList<UserPicksDTO>,
        name: String
    ): UserPicksDTO {
        var userPicks = getExistingPicksForUser(allUserPicks, name)
        if (userPicks == null) {
            userPicks = UserPicksDTO(UserDTO(name))
            allUserPicks.add(userPicks)
        }
        return userPicks
    }

    private fun getExistingPicksForUser(
        allUserPicks: ArrayList<UserPicksDTO>,
        name: String
    ): UserPicksDTO? {
        return allUserPicks.find { existingResult ->
            existingResult.user.name.contains(name)
        }
    }

    private fun addQueryResultToUserPicks(resultSet: ResultSet, result: UserPicksDTO) {
        val game = resultSet.getString("game")
        val pick = resultSet.getString("pick")
        result.picks.add(PickDTO(game, pick))
    }
}