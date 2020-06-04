package db

import dto.LeaderDTO

class LeaderQuery(private var weeksQuery: WeeksQuery, private var weekTotalQuery: UserWeekTotalQuery) {
    fun get(): List<LeaderDTO> {
        val firstWeek = weeksQuery.get().first().name
        val leaders = weekTotalQuery.get(firstWeek).map {
            LeaderDTO(it.user.name)
        }

        weeksQuery.get().forEach { week ->
            val weekResults = weekTotalQuery.get(week.name)
            val mostWon = weekResults.map { results -> results.total }.max()

            leaders.forEach{ leader ->
                val leaderResult = weekResults.first { it.user.name == leader.name }
                if(leaderResult.total == mostWon) {
                    leader.correctWeeks += 1
                }
                leader.correctPicks += leaderResult.total
            }
        }
        return leaders
    }
}