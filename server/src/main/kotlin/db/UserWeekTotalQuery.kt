package db

import dto.UserDTO
import dto.UserWeekTotalDTO
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import java.sql.Connection

class UserWeekTotalQuery(connection: Connection) : DataFetcher<List<UserWeekTotalDTO>> {
    override fun get(environment: DataFetchingEnvironment?): List<UserWeekTotalDTO> {
        val results = ArrayList<UserWeekTotalDTO>(0)
        val total = UserWeekTotalDTO(UserDTO("Dave"))
        results.add(total)
        return results
    }
}