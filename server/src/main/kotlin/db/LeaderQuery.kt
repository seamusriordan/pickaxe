package db

import dto.LeaderDTO

class LeaderQuery(private var weeksQuery: WeeksQuery, private var weekTotalQuery: UserWeekTotalQuery) {
    fun get(): List<LeaderDTO> {
        val firstWeek = weeksQuery.get().first().name
        val leaders = weekTotalQuery.get(firstWeek).map {
            LeaderDTO(it.user.name)
        }

        leaders.forEach {
            val sum = weeksQuery.get()
                .map { week -> weekTotalQuery.get(week.name).first().total }
                .reduce { total, weeklyTotal -> total + weeklyTotal }

            it.correctWeeks = sum
            it.correctPicks = sum
        }
        return leaders
    }
}