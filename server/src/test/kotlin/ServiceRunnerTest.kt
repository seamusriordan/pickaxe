import db.GameMutator
import dto.GameDTO
import dto.WeekDTO
import io.mockk.every
import io.mockk.mockkClass
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import services.NflApi
import services.ServiceRunner
import java.io.FileNotFoundException
import java.time.OffsetDateTime
import java.util.*

class ServiceRunnerTest {

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

        ServiceRunner.reloadGamesForWeek(week, mockNflApi, mockMutator)

        verify(exactly = 1) { mockNflApi.getWeek(week) }
        assertEquals(1, gamesToDb.size)
        val gameToDb = gamesToDb[0]
        assertEquals(dbGame.name, gameToDb.name)
        assertEquals(dbGame.gameTime, gameToDb.gameTime)
        assertEquals(dbGame.id, gameToDb.id)
        assertEquals(dbGame.result, gameToDb.result)
        assertEquals(dbGame.spread, gameToDb.spread)
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

        ServiceRunner.reloadGamesForWeek(week, mockNflApi, mockMutator)

        verify(exactly = 1) { mockNflApi.getWeek(week) }
        assertEquals(2, gamesToDb.size)
        val secondGameToDb = gamesToDb[1]
        assertEquals(dbGame2.name, secondGameToDb.name)
        assertEquals(dbGame2.gameTime, secondGameToDb.gameTime)
        assertEquals(dbGame2.id, secondGameToDb.id)
        assertEquals(dbGame2.result, secondGameToDb.result)
        assertEquals(dbGame2.spread, secondGameToDb.spread)
    }

    @Test
    fun loadInfoForWeekAndFileNotFoundDoesNothing() {
        every { mockNflApi.getWeek(any()) }  throws FileNotFoundException("Mock failure")
        every { mockMutator.putInDatabase(any()) } returns Unit

        ServiceRunner.reloadGamesForWeek(WeekDTO("Week -1"), mockNflApi, mockMutator)

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

        ServiceRunner.updateDetailsForFinalGame(baseGame, mockNflApi, mockMutator)

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

        ServiceRunner.updateDetailsForFinalGame(baseGame, mockNflApi, mockMutator)

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

        ServiceRunner.updateDetailsForFinalGame(baseGame, mockNflApi, mockMutator)

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

        ServiceRunner.updateDetailsForFinalGame(baseGame, mockNflApi, mockMutator)

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

        ServiceRunner.updateDetailsForFinalGame(baseGame, mockNflApi, mockMutator)

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

        ServiceRunner.updateDetailsForFinalGame(baseGame, mockNflApi, mockMutator)

        verify(exactly = 1) { mockNflApi.getGame(any()) }
        verify(exactly = 0) { mockMutator.putInDatabase(any()) }
    }

}