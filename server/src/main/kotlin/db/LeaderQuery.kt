package db

import dto.LeaderDTO

class LeaderQuery(private var weeksQuery: WeeksQuery, private var weekTotalQuery: UserWeekTotalQuery) {
    fun get(): List<LeaderDTO> {
        val firstWeek = weeksQuery.get().first().name
        val firstWeekResult = weekTotalQuery.get(firstWeek).first()

        val sum = weeksQuery.get()
            .map { week -> weekTotalQuery.get(week.name).first().total }
            .reduce { total, weeklyTotal -> total + weeklyTotal }

        return listOf(LeaderDTO(firstWeekResult.user.name).apply {
            correctPicks = sum
            correctWeeks = sum
        })
    }
}