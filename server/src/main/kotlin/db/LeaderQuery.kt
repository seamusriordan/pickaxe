package db

import dto.LeaderDTO

class LeaderQuery(private var weeksQuery: WeeksQuery, private var weekTotalQuery: UserWeekTotalQuery) {
    fun get(): List<LeaderDTO> {
        val week = weeksQuery.get().first().name
        val firstWeekResult = weekTotalQuery.get(week).first()
        return listOf(LeaderDTO(firstWeekResult.user.name).apply {
            correctPicks = firstWeekResult.total
            correctWeeks = firstWeekResult.total
        })
    }
}