package services.utils

import db.GameMutator
import db.GamesQuery
import db.WeeksQuery
import dto.GameDTO
import dto.WeekDTO
import io.mockk.every
import io.mockk.mockkClass
import io.mockk.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import services.NflApi
import services.utils.GameUpdateUtils.Companion.hasImmanentGamesMissingId
import services.utils.GameUpdateUtils.Companion.reloadGamesForWeek
import services.utils.GameUpdateUtils.Companion.updateDetailsForFinalGame
import java.io.FileNotFoundException
import java.time.OffsetDateTime
import java.util.*

class GameUpdateUtilsTest {
    private val gameTimeInFarPast = OffsetDateTime.now().minusYears(1)

    private val defaultGame: GameDTO = GameDTO("GB@CHI", "Week 7").apply {
        gameTime = gameTimeInFarPast
        result = "CHI"
        id = UUID.randomUUID()
        spread = -7.0
    }

    private val mockNflApi = mockkClass(NflApi::class)
    private val mockMutator = mockkClass(GameMutator::class)

    @Test
    fun loadInfoForWeekMovesGameFromNflApiToDatabase() {
        val gamesToDb = mutableListOf<GameDTO>()

        val week = WeekDTO("Week 7")
        val dbGame = defaultGame

        every { mockNflApi.getWeek(any()) } returns listOf(dbGame)

        every { mockMutator.putInDatabase(game = capture(gamesToDb)) } returns Unit

        reloadGamesForWeek(week, mockNflApi, mockMutator)

        verify(exactly = 1) { mockNflApi.getWeek(week) }
        Assertions.assertEquals(1, gamesToDb.size)
        val gameToDb = gamesToDb[0]
        Assertions.assertEquals(dbGame.name, gameToDb.name)
        Assertions.assertEquals(dbGame.gameTime, gameToDb.gameTime)
        Assertions.assertEquals(dbGame.id, gameToDb.id)
        Assertions.assertEquals(dbGame.result, gameToDb.result)
        Assertions.assertEquals(dbGame.spread, gameToDb.spread)
    }


    @Test
    fun loadInfoForWeekWithTwoGamesMovesBothFromNflApiToDatabase() {
        val gamesToDb = mutableListOf<GameDTO>()

        val week = WeekDTO("Week 7")
        val dbGame1 = GameDTO("GB@CHI", "Week 7")
        val dbGame2 = GameDTO("TB@NE", "Week 7").apply {
            gameTime = gameTimeInFarPast.plusHours(1)
            result = "TB"
            id = UUID.randomUUID()
            spread = 15.0
        }

        every { mockNflApi.getWeek(any()) } returns listOf(dbGame1, dbGame2)
        every { mockMutator.putInDatabase(game = capture(gamesToDb)) } returns Unit

        reloadGamesForWeek(week, mockNflApi, mockMutator)

        verify(exactly = 1) { mockNflApi.getWeek(week) }
        Assertions.assertEquals(2, gamesToDb.size)
        val secondGameToDb = gamesToDb[1]
        Assertions.assertEquals(dbGame2.name, secondGameToDb.name)
        Assertions.assertEquals(dbGame2.gameTime, secondGameToDb.gameTime)
        Assertions.assertEquals(dbGame2.id, secondGameToDb.id)
        Assertions.assertEquals(dbGame2.result, secondGameToDb.result)
        Assertions.assertEquals(dbGame2.spread, secondGameToDb.spread)
    }

    @Test
    fun loadInfoForWeekAndFileNotFoundDoesNothing() {
        every { mockNflApi.getWeek(any()) } throws FileNotFoundException("Mock failure")
        every { mockMutator.putInDatabase(any()) } returns Unit

        reloadGamesForWeek(WeekDTO("Week -1"), mockNflApi, mockMutator)

        verify(exactly = 0) { mockMutator.putInDatabase(any()) }
    }

    @Test
    fun updateGameDetailsForGameWithoutResultMovesGameFromNflToDb() {
        val baseGame: GameDTO = GameDTO("GB@CHI", "Week 7").apply {
            gameTime = gameTimeInFarPast
            result = null
            id = UUID.randomUUID()
            spread = -7.0
        }
        every { mockNflApi.getGame(baseGame) } returns defaultGame
        every { mockMutator.putInDatabase(any()) } returns Unit

        updateDetailsForFinalGame(baseGame, mockNflApi, mockMutator)

        verify(exactly = 1) { mockNflApi.getGame(baseGame) }
        verify(exactly = 1) { mockMutator.putInDatabase(defaultGame) }
    }

    @Test
    fun updateGameDetailsForGameWithoutIdDoesNothing() {
        val baseGame: GameDTO = GameDTO("GB@CHI", "Week 7").apply {
            gameTime = gameTimeInFarPast
            result = null
            id = null
            spread = -7.0
        }
        every { mockNflApi.getGame(any()) } returns defaultGame
        every { mockMutator.putInDatabase(any()) } returns Unit

        updateDetailsForFinalGame(baseGame, mockNflApi, mockMutator)

        verify(exactly = 0) { mockNflApi.getGame(any()) }
        verify(exactly = 0) { mockMutator.putInDatabase(any()) }
    }

    @Test
    fun updateGameDetailsForGameWithIdAndResultDoesNothing() {
        val baseGame: GameDTO = GameDTO("GB@CHI", "Week 7").apply {
            gameTime = gameTimeInFarPast
            result = "CHI"
            id = UUID.randomUUID()
            spread = -7.0
        }
        every { mockNflApi.getGame(any()) } returns baseGame
        every { mockMutator.putInDatabase(any()) } returns Unit

        updateDetailsForFinalGame(baseGame, mockNflApi, mockMutator)

        verify(exactly = 0) { mockNflApi.getGame(any()) }
        verify(exactly = 0) { mockMutator.putInDatabase(any()) }
    }

    @Test
    fun updateGameDetailsForGameWithoutResultAndAlmostTwoHoursAfterStartDoesNothing() {
        val baseGame: GameDTO = GameDTO("GB@CHI", "Week 7").apply {
            gameTime = OffsetDateTime.now().minusHours(2).plusMinutes(10)
            result = null
            id = UUID.randomUUID()
            spread = -7.0
        }
        every { mockNflApi.getGame(any()) } returns baseGame
        every { mockMutator.putInDatabase(any()) } returns Unit

        updateDetailsForFinalGame(baseGame, mockNflApi, mockMutator)

        verify(exactly = 0) { mockNflApi.getGame(any()) }
        verify(exactly = 0) { mockMutator.putInDatabase(any()) }
    }

    @Test
    fun updateGameDetailsForGameWithoutGameTimeDoesNothing() {
        val baseGame: GameDTO = GameDTO("GB@CHI", "Week 7").apply {
            gameTime = null
            result = null
            id = UUID.randomUUID()
            spread = -7.0
        }
        every { mockNflApi.getGame(any()) } returns baseGame
        every { mockMutator.putInDatabase(any()) } returns Unit

        updateDetailsForFinalGame(baseGame, mockNflApi, mockMutator)

        verify(exactly = 0) { mockNflApi.getGame(any()) }
        verify(exactly = 0) { mockMutator.putInDatabase(any()) }
    }

    @Test
    fun updateGameDetailsAndNflApiThrowsFileNotFoundSkipsPutInDatabase() {
        val baseGame: GameDTO = GameDTO("GB@CHI", "Week 7").apply {
            gameTime = gameTimeInFarPast
            result = null
            id = UUID.randomUUID()
            spread = -7.0
        }
        every { mockNflApi.getGame(any()) } throws FileNotFoundException("Mock failure")

        updateDetailsForFinalGame(baseGame, mockNflApi, mockMutator)

        verify(exactly = 1) { mockNflApi.getGame(any()) }
        verify(exactly = 0) { mockMutator.putInDatabase(any()) }
    }

    @Test
    fun hasGamesMissingIdReturnsFalseWhenOnlyGameIsFinal() {
        val mockGamesQuery = mockkClass(GamesQuery::class)
        val mockWeeksQuery = mockkClass(WeeksQuery::class)
        every { mockWeeksQuery.get() } returns listOf(WeekDTO("Week 1"))
        every { mockGamesQuery.getGamesForWeek("Week 1") } returns listOf(
            GameDTO("GB@CHI", "Week 1").apply {
                result = "CHI"
            }
        )

        val result = hasImmanentGamesMissingId(mockWeeksQuery, mockGamesQuery)

        Assertions.assertFalse(result)
    }

    @Test
    fun hasGamesMissingIdReturnsTrueWhenFirstGameInSecondWeekHasNoId() {
        val mockGamesQuery = mockkClass(GamesQuery::class)
        val mockWeeksQuery = mockkClass(WeeksQuery::class)
        every { mockWeeksQuery.get() } returns listOf(WeekDTO("Week 1"), WeekDTO("Week 2"))
        every { mockGamesQuery.getGamesForWeek("Week 1") } returns listOf(
            GameDTO("GB@CHI", "Week 1").apply {
                result = "CHI"
                id = UUID.randomUUID()
            }
        )
        every { mockGamesQuery.getGamesForWeek("Week 2") } returns listOf(
            GameDTO("DET@CHI", "Week 2").apply {
                gameTime = OffsetDateTime.now().minusDays(1)
            }
        )

        val result = hasImmanentGamesMissingId(mockWeeksQuery, mockGamesQuery)

        Assertions.assertTrue(result)
    }

    @Test
    fun hasGamesMissingIdReturnsTrueWhenMissingIdAndTenMinutesBeforeGame() {
        val mockGamesQuery = mockkClass(GamesQuery::class)
        val mockWeeksQuery = mockkClass(WeeksQuery::class)
        every { mockWeeksQuery.get() } returns listOf(WeekDTO("Week 1"))
        every { mockGamesQuery.getGamesForWeek("Week 1") } returns listOf(
            GameDTO("GB@CHI", "Week 1").apply {
                gameTime = OffsetDateTime.now().plusMinutes(10)
            }
        )

        val result = hasImmanentGamesMissingId(mockWeeksQuery, mockGamesQuery)

        Assertions.assertTrue(result)
    }

    @Test
    fun hasGamesMissingIdResultAndGametimeReturnsFalse() {
        val mockGamesQuery = mockkClass(GamesQuery::class)
        val mockWeeksQuery = mockkClass(WeeksQuery::class)
        every { mockWeeksQuery.get() } returns listOf(WeekDTO("Week 1"))
        every { mockGamesQuery.getGamesForWeek("Week 1") } returns listOf(
            GameDTO("GB@CHI", "Week 1")
        )

        val result = hasImmanentGamesMissingId(mockWeeksQuery, mockGamesQuery)

        Assertions.assertFalse(result)
    }

    @Test
    fun hasGamesMissingIdReturnsFalseWhenMissingIdAndFifteenMinutesAfterGame() {
        val mockGamesQuery = mockkClass(GamesQuery::class)
        val mockWeeksQuery = mockkClass(WeeksQuery::class)
        every { mockWeeksQuery.get() } returns listOf(WeekDTO("Week 1"))
        every { mockGamesQuery.getGamesForWeek("Week 1") } returns listOf(
            GameDTO("GB@CHI", "Week 1").apply {
                gameTime = OffsetDateTime.now().plusMinutes(105)
            }
        )

        val result = hasImmanentGamesMissingId(mockWeeksQuery, mockGamesQuery)

        Assertions.assertFalse(result)
    }

    @Test
    fun hasGamesMissingIdReturnsTrueWhenSecondGameInSecondWeekHasNoId() {
        val mockGamesQuery = mockkClass(GamesQuery::class)
        val mockWeeksQuery = mockkClass(WeeksQuery::class)
        every { mockWeeksQuery.get() } returns listOf(WeekDTO("Week 1"), WeekDTO("Week 2"))
        every { mockGamesQuery.getGamesForWeek("Week 1") } returns listOf(
            GameDTO("GB@CHI", "Week 1").apply {
                result = "CHI"
                gameTime = OffsetDateTime.now().minusDays(1)
                id = UUID.randomUUID()
            }
        )
        every { mockGamesQuery.getGamesForWeek("Week 2") } returns listOf(
            GameDTO("DET@CHI", "Week 2").apply {
                gameTime = OffsetDateTime.now().minusDays(1)
                id = UUID.randomUUID()
            },
            GameDTO("NE@NYJ", "Week 2").apply {
                gameTime = OffsetDateTime.now().minusDays(1)
            }
        )

        val result = hasImmanentGamesMissingId(mockWeeksQuery, mockGamesQuery)

        Assertions.assertTrue(result)
    }
}