package db

import dto.WeekDTO
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import java.time.OffsetDateTime

class CurrentWeekQuery(private val weeksQuery: WeeksQuery, private val gamesQuery: GamesQuery) : DataFetcher<WeekDTO> {
    override fun get(environment: DataFetchingEnvironment?): WeekDTO {
        val sortedWeeks = weeksQuery.get()
            .filter { hasNonnullGameTime(it.name, gamesQuery) }
            .sortedBy { firstNonnullGameTime(it.name, gamesQuery) }

        val weeksWithGamesLessThanSeveralHoursAgo = sortedWeeks.filter {
            gamesQuery.getGamesForWeek(it.name).filter { game -> game.gameTime != null }
                .any { game -> isLessThanXHoursBeforeNow(game.gameTime, 18) }
        }.toMutableList()

        weeksWithGamesLessThanSeveralHoursAgo.add(sortedWeeks.last())
        return weeksWithGamesLessThanSeveralHoursAgo.first()
    }

    private fun isLessThanXHoursBeforeNow(time: OffsetDateTime?, hours: Long): Boolean {
        return time!!.plusHours(hours).isAfter(OffsetDateTime.now())
    }

    private fun hasNonnullGameTime(week: String, gamesQuery: GamesQuery): Boolean {
        return gamesQuery.getGamesForWeek(week).any { it.gameTime != null }
    }

    private fun firstNonnullGameTime(week: String, gamesQuery: GamesQuery): OffsetDateTime {
        return gamesQuery.getGamesForWeek(week).first { it.gameTime != null }.gameTime!!
    }
}
