package db

import dto.LeaderDTO

class LeaderQuery(private var weeksQuery: WeeksQuery, private var weekTotalQuery: UserWeekTotalQuery) {
    fun get(): List<LeaderDTO> {
        val firstWeek = weeksQuery.get().first().name
        val leaders = weekTotalQuery.get(firstWeek).map {
            LeaderDTO(it.user.name)
        }

        leaders.forEach { leader ->
            val sum = weeksQuery.get()
                .map { week ->
                    weekTotalQuery.get(week.name).first { it.user.name == leader.name }.total
                }
                .reduce { total, weeklyTotal -> total + weeklyTotal }
            leader.correctWeeks = sum
            leader.correctPicks = sum
        }
        return leaders
    }
}