package db

import dto.*
import io.mockk.every
import io.mockk.mockkClass
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class LeaderQueryTest {
    @Test
    fun oneUserWithAllCorrect() {
        val week = "Week 1"
        val user = UserDTO("Daan")
        val game = GameDTO("GB@CHI", week).apply {
            result = "CHI"
        }

        val mockWeeksQuery = mockkClass(WeeksQuery::class)
        every {mockWeeksQuery.get()} returns listOf(WeekDTO(week))

        val mockWeekTotalQuery = mockkClass(UserWeekTotalQuery::class)
        every { mockWeekTotalQuery.get(week) } returns
                listOf(UserWeekTotalDTO(user).apply {
                    games = mutableListOf(game)
                })

        val leaderQuery = LeaderQuery(mockWeeksQuery, mockWeekTotalQuery)

        val leader: List<LeaderDTO> = leaderQuery.get()

        assertEquals(user.name, leader.first().name)
        assertEquals(1, leader.first().correctWeeks)
        assertEquals(1, leader.first().correctPicks)
    }

    @Test
    fun oneUserWithNoneCorrect() {
        val week = "Week 1"
        val user = UserDTO("Daav")

        val mockWeeksQuery = mockkClass(WeeksQuery::class)
        every {mockWeeksQuery.get()} returns listOf(WeekDTO(week))

        val mockWeekTotalQuery = mockkClass(UserWeekTotalQuery::class)
        every { mockWeekTotalQuery.get(week) } returns
                listOf(UserWeekTotalDTO(user).apply {
                    games = mutableListOf()
                })

        val leaderQuery = LeaderQuery(mockWeeksQuery, mockWeekTotalQuery)

        val leader: List<LeaderDTO> = leaderQuery.get()

        assertEquals(user.name, leader.first().name)
        assertEquals(0, leader.first().correctWeeks)
        assertEquals(0, leader.first().correctPicks)
    }

    @Test
    fun oneUserWithNoneCorrectForWeek2() {
        val week = "Week 2"
        val user = UserDTO("Daav")

        val mockWeeksQuery = mockkClass(WeeksQuery::class)
        every {mockWeeksQuery.get()} returns listOf(WeekDTO(week))

        val mockWeekTotalQuery = mockkClass(UserWeekTotalQuery::class)
        every { mockWeekTotalQuery.get(week) } returns
                listOf(UserWeekTotalDTO(user).apply {
                    games = mutableListOf()
                })

        val leaderQuery = LeaderQuery(mockWeeksQuery, mockWeekTotalQuery)

        val leader: List<LeaderDTO> = leaderQuery.get()

        assertEquals(user.name, leader.first().name)
        assertEquals(0, leader.first().correctWeeks)
        assertEquals(0, leader.first().correctPicks)
    }

    @Test
    fun oneUserWithCorrectPicksOverTwoWeeks() {
        val weeks =  listOf(WeekDTO("Week 1"), WeekDTO("Week 2"))
        val user = UserDTO("Daav")

        val game = GameDTO("GB@CHI", weeks[0].name).apply {
            result = "CHI"
        }

        val mockWeeksQuery = mockkClass(WeeksQuery::class)
        every {mockWeeksQuery.get()} returns weeks

        val mockWeekTotalQuery = mockkClass(UserWeekTotalQuery::class)
        every { mockWeekTotalQuery.get(weeks[0].name) } returns
                listOf(UserWeekTotalDTO(user).apply {
                    games = mutableListOf(game)
                })
        every { mockWeekTotalQuery.get(weeks[1].name) } returns
                listOf(UserWeekTotalDTO(user).apply {
                    games = mutableListOf(game)
                })


        val leaderQuery = LeaderQuery(mockWeeksQuery, mockWeekTotalQuery)

        val leader: List<LeaderDTO> = leaderQuery.get()

        assertEquals(user.name, leader.first().name)
        assertEquals(2, leader.first().correctWeeks)
        assertEquals(2, leader.first().correctPicks)
    }
}