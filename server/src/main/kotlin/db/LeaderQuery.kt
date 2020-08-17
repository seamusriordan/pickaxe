package db

import dto.GameDTO
import dto.LeaderDTO
import dto.UserWeekTotalDTO
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
            val gamesForWeek = gamesQuery.getGamesForWeek(week.name)

            tallyResultsForWeek(leaders, gamesForWeek, weekResults)
        }
        return leaders
    }

    private fun tallyResultsForWeek(
        leaders: List<LeaderDTO>,
        gamesForWeek: List<GameDTO>,
        weekResults: List<UserWeekTotalDTO>
    ) {
        leaders.forEach { leader -> tallyResultsForUser(leader, weekResults, gamesForWeek) }
    }

    private fun tallyResultsForUser(
        leader: LeaderDTO,
        weekResults: List<UserWeekTotalDTO>,
        gamesForWeek: List<GameDTO>
    ) {
        val leaderResult = getResultsForLeader(leader, weekResults)
        if (weekIsComplete(gamesForWeek) && hasMostCorrectPicks(leaderResult, weekResults)) {
            leader.correctWeeks += 1
        }
        leader.correctPicks += leaderResult.total
    }

    private fun weekIsComplete(gamesForWeek: List<GameDTO>): Boolean {
        return gamesForWeek.isNotEmpty() && hasNoIncompleteGames(gamesForWeek)
    }

    private fun hasMostCorrectPicks(
        leaderResult: UserWeekTotalDTO,
        weekResults: List<UserWeekTotalDTO>
    ): Boolean {
        return leaderResult.total == getMostCorrectPicks(weekResults)
    }

    private fun getMostCorrectPicks(weekResults: List<UserWeekTotalDTO>): Int? {
        return weekResults.map { results -> results.total }.max()
    }

    private fun getResultsForLeader(
        leader: LeaderDTO,
        weekResults: List<UserWeekTotalDTO>
    ) = weekResults.first { it.user.name == leader.name }

    private fun hasNoIncompleteGames(games: List<GameDTO>) = !weekHasIncompleteGame(games)

    private fun weekHasIncompleteGame(games: List<GameDTO>): Boolean {
        return games
            .map { it.result == null }
            .contains(true)
    }

    override fun get(environment: DataFetchingEnvironment?): List<LeaderDTO> {
        return get()
    }
}