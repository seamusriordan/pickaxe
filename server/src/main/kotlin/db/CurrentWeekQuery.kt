package db

import dto.WeekDTO
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import java.time.OffsetDateTime

class CurrentWeekQuery(private val weeksQuery: WeeksQuery, private val gamesQuery: GamesQuery) : DataFetcher<WeekDTO> {
    override fun get(environment: DataFetchingEnvironment?): WeekDTO {
        val weeks = weeksQuery.get()
        val weeksWithGamesLessThanSeveralHoursAgo = weeks.filter {
            gamesQuery.getGamesForWeek(it.name).filter { game -> game.gameTime != null }
                .any { game -> isLessThanXHoursBeforeNow(game.gameTime, 18) }
        }.toMutableList()
        weeksWithGamesLessThanSeveralHoursAgo.add(weeks.last())
        return weeksWithGamesLessThanSeveralHoursAgo.first()
    }

    private fun isLessThanXHoursBeforeNow(time: OffsetDateTime?, hours: Long): Boolean{
        return time!!.plusHours(hours).isAfter(OffsetDateTime.now())
    }
}
