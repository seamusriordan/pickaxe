package db

import dto.GameDTO
import dto.WeekDTO
import graphql.schema.DataFetchingEnvironment
import graphql.schema.DataFetchingEnvironmentImpl
import io.mockk.every
import io.mockk.mockkClass
import io.mockk.mockkStatic
import mockNextReturnTimes
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.Statement
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
}