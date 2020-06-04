package db

import dto.LeaderDTO
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment

class LeaderQuery(
    private var weeksQuery: WeeksQuery,
    private var gamesQuery: GamesQuery,
    private var weekTotalQuery: UserWeekTotalQuery
) :
    DataFetcher<List<LeaderDTO>> {
    fun get(): List<LeaderDTO> {
        val firstWeek = weeksQuery.get().first().name
        val leaders = weekTotalQuery.get(firstWeek).map {
            LeaderDTO(it.user.name)
        }

        weeksQuery.get().forEach { week ->
            val weekResults = weekTotalQuery.get(week.name)
            val mostWon = weekResults.map { results -> results.total }.max()

            val weekIsComplete =
                !gamesQuery
                .getGamesForWeek(week.name)
                .map { it.result == null }
                    .contains(true)

            leaders.forEach { leader ->
                val leaderResult = weekResults.first { it.user.name == leader.name }
                if (leaderResult.total == mostWon && weekIsComplete) {
                    leader.correctWeeks += 1
                }
                leader.correctPicks += leaderResult.total
            }
        }
        return leaders
    }

    override fun get(environment: DataFetchingEnvironment?): List<LeaderDTO> {
        return get()
    }
}