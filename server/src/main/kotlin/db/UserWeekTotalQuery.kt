package db

import dto.UserWeekTotalDTO
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import java.sql.Connection

class UserWeekTotalQuery(val connection: Connection) : DataFetcher<List<UserWeekTotalDTO>> {
    override fun get(environment: DataFetchingEnvironment?): List<UserWeekTotalDTO> {
        val userResults = UserQuery(connection).getActiveUsers()

        val results = ArrayList<UserWeekTotalDTO>(0)

        for(userResult in userResults){
            results.add(UserWeekTotalDTO(userResult))
        }

        return results
    }
}