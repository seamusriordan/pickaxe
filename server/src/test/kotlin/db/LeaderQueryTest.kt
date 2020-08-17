package db

import dto.*
import io.mockk.every
import io.mockk.mockkClass
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class LeaderQueryTest {
    private lateinit var mockWeeksQuery: WeeksQuery
    private lateinit var mockGamesQuery: GamesQuery
    private lateinit var mockWeekTotalQuery: UserWeekTotalQuery
    private lateinit var leaderQuery: LeaderQuery
    private lateinit var defaultGame: GameDTO

    private var defaultWeek = "Week 1"

    @BeforeEach
    fun setup() {
        mockWeeksQuery = mockkClass(WeeksQuery::class)

        mockGamesQuery = mockkClass(GamesQuery::class)
        defaultGame = GameDTO("GB@CHI", defaultWeek).apply {
            result = "CHI"
        }
        every { mockGamesQuery.getGamesForWeek(defaultWeek) } returns listOf(defaultGame)

        mockWeekTotalQuery = mockkClass(UserWeekTotalQuery::class)
        leaderQuery = LeaderQuery(mockWeeksQuery, mockGamesQuery, mockWeekTotalQuery)
    }

    @Test
    fun oneUserWithAllCorrect() {
        val user = UserDTO("Daan")

        every { mockWeeksQuery.get() } returns listOf(WeekDTO(defaultWeek))
        every { mockGamesQuery.getGamesForWeek(defaultWeek) } returns listOf(defaultGame)

        every { mockWeekTotalQuery.get(defaultWeek) } returns
                listOf(UserWeekTotalDTO(user).apply {
                    games = mutableListOf(defaultGame)
                })


        val leader: List<LeaderDTO> = leaderQuery.get()

        assertEquals(user.name, leader.first().name)
        assertEquals(1, leader.first().correctWeeks)
        assertEquals(1, leader.first().correctPicks)
    }

    @Test
    fun oneUserWithNoneCorrect() {
        val week = "Week 1"
        val user = UserDTO("Daav")

        every { mockWeeksQuery.get() } returns listOf(WeekDTO(week))

        every { mockWeekTotalQuery.get(week) } returns
                listOf(UserWeekTotalDTO(user).apply {
                    games = mutableListOf()
                })


        val leader: List<LeaderDTO> = leaderQuery.get()

        assertEquals(user.name, leader.first().name)
        assertEquals(1, leader.first().correctWeeks)
        assertEquals(0, leader.first().correctPicks)
    }

    @Test
    fun oneUserWithTwoPicksAndOneCorrect() {
        val user = UserDTO("Daav")
        val games = mutableListOf(
            GameDTO("GB@CHI", defaultWeek).apply {
                result = "CHI"
            },
            GameDTO("TB@NE", defaultWeek).apply {
                result = "TB"
            }
        )

        every { mockWeeksQuery.get() } returns listOf(WeekDTO(defaultWeek))
        every { mockGamesQuery.getGamesForWeek(defaultWeek) } returns games

        every { mockWeekTotalQuery.get(defaultWeek) } returns
                listOf(UserWeekTotalDTO(user).apply {
                   this.games = mutableListOf(
                        GameDTO("GB@CHI", defaultWeek),
                        GameDTO("TB@NE", defaultWeek)
                    )
                })

        val leader: List<LeaderDTO> = leaderQuery.get()

        assertEquals(user.name, leader.first().name)
        assertEquals(1, leader.first().correctWeeks)
        assertEquals(2, leader.first().correctPicks)
    }

    @Test
    fun oneUserWithNoneCorrectForWeek2() {
        val week = "Week 2"
        val user = UserDTO("Daav")

        every { mockWeeksQuery.get() } returns listOf(WeekDTO(week))
        every { mockGamesQuery.getGamesForWeek(week) } returns listOf(defaultGame)


        every { mockWeekTotalQuery.get(week) } returns
                listOf(UserWeekTotalDTO(user).apply {
                    games = mutableListOf()
                })

        val leader: List<LeaderDTO> = leaderQuery.get()

        assertEquals(user.name, leader.first().name)
        assertEquals(1, leader.first().correctWeeks)
        assertEquals(0, leader.first().correctPicks)
    }

    @Test
    fun twoUsersWithNoCorrectPicks() {
        val week = "Week 2"
        val users = listOf(UserDTO("Daav"), UserDTO("Bef"))

        every { mockWeeksQuery.get() } returns listOf(WeekDTO(week))
        every { mockGamesQuery.getGamesForWeek(week) } returns listOf(defaultGame)


        every { mockWeekTotalQuery.get(week) } returns
                listOf(
                    UserWeekTotalDTO(users[0]).apply {
                        games = mutableListOf()
                    },
                    UserWeekTotalDTO(users[1]).apply {
                        games = mutableListOf()
                    }
                )


        val leaders: List<LeaderDTO> = leaderQuery.get()

        assertEquals(2, leaders.size)
        assertEquals(users[1].name, leaders[1].name)
        assertEquals(1, leaders[1].correctWeeks)
        assertEquals(0, leaders[1].correctPicks)
    }

    @Test
    fun twoUsersWithDifferentPicksInOneWeek() {
        val week = "Week 2"
        val users = listOf(UserDTO("Daav"), UserDTO("Bef"))

        every { mockWeeksQuery.get() } returns listOf(WeekDTO(week))
        every { mockGamesQuery.getGamesForWeek(week) } returns listOf(defaultGame)


        every { mockWeekTotalQuery.get(week) } returns
                listOf(
                    UserWeekTotalDTO(users[0]).apply {
                        games = mutableListOf(GameDTO("GB@CHI", week))
                    },
                    UserWeekTotalDTO(users[1]).apply {
                        games = mutableListOf()
                    }
                )

        val leaders: List<LeaderDTO> = leaderQuery.get()

        assertEquals(2, leaders.size)
        assertEquals(users[0].name, leaders[0].name)
        assertEquals(1, leaders[0].correctWeeks)
        assertEquals(1, leaders[0].correctPicks)

        assertEquals(users[1].name, leaders[1].name)
        assertEquals(0, leaders[1].correctWeeks)
        assertEquals(0, leaders[1].correctPicks)
    }

    @Test
    fun oneUserWithCorrectPicksOverTwoWeeks() {
        val weeks = listOf(WeekDTO("Week 1"), WeekDTO("Week 2"))
        val user = UserDTO("Daav")

        val game = GameDTO("GB@CHI", weeks[0].name).apply {
            result = "CHI"
        }

        every { mockGamesQuery.getGamesForWeek(weeks[1].name) } returns listOf(defaultGame)


        every { mockWeeksQuery.get() } returns weeks

        every { mockWeekTotalQuery.get(weeks[0].name) } returns
                listOf(UserWeekTotalDTO(user).apply {
                    games = mutableListOf(game)
                })
        every { mockWeekTotalQuery.get(weeks[1].name) } returns
                listOf(UserWeekTotalDTO(user).apply {
                    games = mutableListOf(game)
                })


        val leader: List<LeaderDTO> = leaderQuery.get()

        assertEquals(user.name, leader.first().name)
        assertEquals(2, leader.first().correctWeeks)
        assertEquals(2, leader.first().correctPicks)
    }

    @Test
    fun incompleteWeeksAreNotConsideredForWinning() {
        val user = UserDTO("Daan")
        val unfinishedGame = GameDTO("GB@CHI", defaultWeek)

        every { mockWeeksQuery.get() } returns listOf(WeekDTO(defaultWeek))
        every { mockGamesQuery.getGamesForWeek(defaultWeek) } returns listOf(unfinishedGame)

        every { mockWeekTotalQuery.get(defaultWeek) } returns
                listOf(UserWeekTotalDTO(user).apply {
                    games = mutableListOf(defaultGame)
                })


        val leader: List<LeaderDTO> = leaderQuery.get()

        assertEquals(user.name, leader.first().name)
        assertEquals(0, leader.first().correctWeeks)
        assertEquals(1, leader.first().correctPicks)
    }

    @Test
    fun `Weeks with no games are not considered for winning`() {
        val user = UserDTO("Daan")

        every { mockWeeksQuery.get() } returns listOf(WeekDTO(defaultWeek))
        every { mockGamesQuery.getGamesForWeek(defaultWeek) } returns listOf()

        every { mockWeekTotalQuery.get(defaultWeek) } returns
                listOf(UserWeekTotalDTO(user).apply {
                    games = mutableListOf(defaultGame)
                })


        val leader: List<LeaderDTO> = leaderQuery.get()

        assertEquals(user.name, leader.first().name)
        assertEquals(0, leader.first().correctWeeks)
    }
}