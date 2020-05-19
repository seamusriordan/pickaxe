package db

import dto.WeekDTO
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment

class CurrentWeekQuery(private val weeksQuery: WeeksQuery, private val gamesQuery: GamesQuery) : DataFetcher<WeekDTO> {
    override fun get(environment: DataFetchingEnvironment?): WeekDTO {
        return weeksQuery.get().first()
    }
}
