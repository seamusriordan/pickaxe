package db

import dto.GameDTO
import dto.UserWeekTotalDTO
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import java.sql.Connection

class UserWeekTotalQuery(val connection: Connection) : DataFetcher<List<UserWeekTotalDTO>> {
    override fun get(environment: DataFetchingEnvironment?): List<UserWeekTotalDTO> {
        val userResults = UserQuery(connection).getActiveUsers()

        val results = ArrayList<UserWeekTotalDTO>(0)

        for(userResult in userResults){
            val weeklyTotal = UserWeekTotalDTO(userResult)
            weeklyTotal.games.add(GameDTO("GB@CHI", "Week 0"))
            results.add(weeklyTotal)
        }

        return results
    }
}