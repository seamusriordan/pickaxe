package db

import dto.LeaderDTO

class LeaderQuery(weekTotalQuery: UserWeekTotalQuery) {
    fun get(): List<LeaderDTO> {
        return listOf(LeaderDTO("Daan").apply {
            correctPicks = 1
            correctWeeks = 1
        })
    }
}