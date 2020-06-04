package db

import dto.LeaderDTO

class LeaderQuery(userQuery: UserQuery, weeksQuery: WeeksQuery, userPickQuery: UserPickQuery) {
    fun get(): List<LeaderDTO> {
        return listOf(LeaderDTO("Daan").apply {
            correctPicks = 1
            correctWeeks = 1
        })
    }
}