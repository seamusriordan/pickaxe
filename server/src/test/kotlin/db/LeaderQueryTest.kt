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

        val mockUserQuery = mockkClass(UserQuery::class)
        every { mockUserQuery.getActiveUsers() } returns listOf(user)

        val mockWeeksQuery = mockkClass(WeeksQuery::class)
        every { mockWeeksQuery.get() } returns listOf(WeekDTO(week))

        val mockGamesQuery = mockkClass(GamesQuery::class)
        every { mockGamesQuery.getGamesForWeek(week) } returns listOf(game)

        val mockPickQuery = mockkClass(UserPickQuery::class)
        every { mockPickQuery.getPicksForWeek(week) } returns listOf(
            UserPicksDTO(user).apply {
                picks.add(
                    PickDTO(game.name, game.result!!)
                )
            }
        )

        val leaderQuery = LeaderQuery(mockUserQuery, mockWeeksQuery, mockPickQuery)

        val leader: List<LeaderDTO> = leaderQuery.get()

        assertEquals(user.name, leader.first().name)
        assertEquals(1, leader.first().correctWeeks)
        assertEquals(1, leader.first().correctPicks)
    }
}