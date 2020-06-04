package db

import dto.*
import io.mockk.every
import io.mockk.mockkClass
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class LeaderQueryTest {
    @Test
    fun whenOneUserWithAllCorrectReturnWeeksWonAndTotalCorrectPicks() {
        val week = "Week 1"
        val user = UserDTO("Daan")
        val game = GameDTO("GB@CHI", week).apply {
            result = "CHI"
        }

        val mockWeekTotalQuery = mockkClass(UserWeekTotalQuery::class)

        every { mockWeekTotalQuery.get(week) } returns
                listOf(UserWeekTotalDTO(user).apply {
                    games = mutableListOf(game)
                })

        val leaderQuery = LeaderQuery(mockWeekTotalQuery)

        val leader: List<LeaderDTO> = leaderQuery.get()

        assertEquals(user.name, leader.first().name)
        assertEquals(1, leader.first().correctWeeks)
        assertEquals(1, leader.first().correctPicks)
    }
}