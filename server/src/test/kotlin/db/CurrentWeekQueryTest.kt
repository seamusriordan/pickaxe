package db

import dto.GameDTO
import dto.WeekDTO
import graphql.schema.DataFetchingEnvironment
import graphql.schema.DataFetchingEnvironmentImpl
import io.mockk.every
import io.mockk.mockkClass
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime

class CurrentWeekQueryTest {
    private lateinit var mockWeeksQuery: WeeksQuery
    private lateinit var mockGamesQuery: GamesQuery
    private lateinit var env: DataFetchingEnvironment

    @BeforeEach
    fun beforeEach() {
        mockWeeksQuery = mockkClass(WeeksQuery::class)
        mockGamesQuery = mockkClass(GamesQuery::class)
        env = DataFetchingEnvironmentImpl.newDataFetchingEnvironment().build()
    }

    @Test
    fun getReturnsCurrentWeek0WhenOnlyWeekIs0() {
        val expectedWeek = WeekDTO("0")
        every {mockWeeksQuery.get()} returns listOf(expectedWeek)
        every { mockGamesQuery.getGamesForWeek(expectedWeek.name)} returns listOf(
            GameDTO("GB@CHI", expectedWeek.name).apply {
                gameTime = OffsetDateTime.now().plusDays(1)
            }
        )

        val results = CurrentWeekQuery(mockWeeksQuery, mockGamesQuery).get(env)

        Assertions.assertEquals(expectedWeek.name, results.name)
    }

    @Test
    fun getReturnsCurrentWeek1WhenWeek0GameTimeIsNull() {
        val unexpectedWeek = WeekDTO("0")
        val expectedWeek = WeekDTO("1")
        every {mockWeeksQuery.get()} returns listOf(unexpectedWeek, expectedWeek)
        every { mockGamesQuery.getGamesForWeek(unexpectedWeek.name)} returns listOf(
            GameDTO("GB@CHI", unexpectedWeek.name)
        )
        every { mockGamesQuery.getGamesForWeek(expectedWeek.name)} returns listOf(
            GameDTO("ARI@MIN", expectedWeek.name).apply {
                gameTime = OffsetDateTime.now()
            }
        )

        val results = CurrentWeekQuery(mockWeeksQuery, mockGamesQuery).get(env)

        Assertions.assertEquals(expectedWeek.name, results.name)
    }


    @Test
    fun getReturnsCurrentWeek44WhenYearOld() {
        val expectedWeek = WeekDTO("44")
        every {mockWeeksQuery.get()} returns listOf(WeekDTO("Week 5"), expectedWeek)
        every { mockGamesQuery.getGamesForWeek("Week 5")} returns listOf(
            GameDTO("SEA@PHI", expectedWeek.name).apply {
                gameTime = OffsetDateTime.now().minusYears(1).minusDays(1)
            }
        )
        every { mockGamesQuery.getGamesForWeek(expectedWeek.name)} returns listOf(
            GameDTO("GB@CHI", expectedWeek.name).apply {
                gameTime = OffsetDateTime.now().minusYears(1)
            }
        )

        val results = CurrentWeekQuery(mockWeeksQuery, mockGamesQuery).get(env)

        Assertions.assertEquals(expectedWeek.name, results.name)
    }

    @Test
    fun getReturnsCurrentWeek44WhenYearOldAndOutOfOrder() {
        val expectedWeek = WeekDTO("44")
        every {mockWeeksQuery.get()} returns listOf(expectedWeek, WeekDTO("Week 5"))
        every { mockGamesQuery.getGamesForWeek("Week 5")} returns listOf(
            GameDTO("SEA@PHI", expectedWeek.name).apply {
                gameTime = OffsetDateTime.now().minusYears(1).minusDays(1)
            }
        )
        every { mockGamesQuery.getGamesForWeek(expectedWeek.name)} returns listOf(
            GameDTO("GB@CHI", expectedWeek.name).apply {
                gameTime = OffsetDateTime.now().minusYears(1)
            }
        )

        val results = CurrentWeekQuery(mockWeeksQuery, mockGamesQuery).get(env)

        Assertions.assertEquals(expectedWeek.name, results.name)
    }

    @Test
    fun week1WhenGameTimeIs18HoursAfterWeek0() {
        val unexpectedWeek = WeekDTO("0")
        val expectedWeek = WeekDTO("1")
        every {mockWeeksQuery.get()} returns listOf(unexpectedWeek, expectedWeek)
        every { mockGamesQuery.getGamesForWeek(unexpectedWeek.name)} returns listOf(
            GameDTO("LAR@DEN", expectedWeek.name).apply {
                gameTime = OffsetDateTime.now().minusHours(18)
            }
        )
        every { mockGamesQuery.getGamesForWeek(expectedWeek.name)} returns listOf(
            GameDTO("GB@CHI", expectedWeek.name).apply {
                gameTime = OffsetDateTime.now().plusDays(1)
            }
        )

        val results = CurrentWeekQuery(mockWeeksQuery, mockGamesQuery).get(env)

        Assertions.assertEquals(expectedWeek.name, results.name)
    }

    @Test
    fun week0WhenGameTimeIsBeforeSecondWeek0Game() {
        val expectedWeek = WeekDTO("0")
        val unexpectedWeek = WeekDTO("1")
        every {mockWeeksQuery.get()} returns listOf(expectedWeek, unexpectedWeek)
        every { mockGamesQuery.getGamesForWeek(expectedWeek.name)} returns listOf(
            GameDTO("LAR@DEN", expectedWeek.name).apply {
                gameTime = OffsetDateTime.now().minusHours(18)
            },
            GameDTO("CAR@WAs", expectedWeek.name).apply {
                gameTime = OffsetDateTime.now().plusHours(18)
            }
        )
        every { mockGamesQuery.getGamesForWeek(unexpectedWeek.name)} returns listOf(
            GameDTO("GB@CHI", unexpectedWeek.name).apply {
                gameTime = OffsetDateTime.now().plusDays(1)
            }
        )

        val results = CurrentWeekQuery(mockWeeksQuery, mockGamesQuery).get(env)

        Assertions.assertEquals(expectedWeek.name, results.name)
    }
}