package services.utils

import db.*
import dto.*
import io.mockk.every
import io.mockk.mockkClass
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import services.VegasPicksApi
import services.utils.VegasUpdateUtils.Companion.updateVegasPicks
import java.time.OffsetDateTime
import java.util.*

class VegasUpdateUtilsTest {
    private val mockCurrentWeekQuery = mockkClass(CurrentWeekQuery::class)
    private val mockPicksQuery = mockkClass(UserPickQuery::class)
    private val mockGamesQuery = mockkClass(GamesQuery::class)
    private val mockPickMutator = mockkClass(UpdatePickMutator::class)
    private val mockGameMutator = mockkClass(GameMutator::class)
    private val mockVegasPicksApi = mockkClass(VegasPicksApi::class)

    private val mutatorUser = mutableListOf<UserDTO>()
    private val mutatorWeek = mutableListOf<WeekDTO>()
    private val mutatorPick = mutableListOf<PickDTO>()

    private val rngUserName = "RNG"
    private val defaultWeek = "Week 0"
    private val defaultGame = "DET@CHI"
    private val defaultExpectedPick = "CHI"
    private val defaultSpread = -7.0

    private val defaultGameDTO = GameDTO(defaultGame, defaultWeek).apply {
        gameTime = OffsetDateTime.now().plusDays(1)
        id = UUID.randomUUID()
    }

    private val emptyExistingPicks = UserPicksDTO(UserDTO(rngUserName))

    private val gameMutations = mutableListOf<GameDTO>()


    init {
        every { mockPickMutator.updatePick(
            capture(mutatorUser),
            capture(mutatorWeek),
            capture(mutatorPick)
        )} returns true

        every { mockGameMutator.putInDatabase(capture(gameMutations)) } returns Unit


        every { mockCurrentWeekQuery.getCurrentWeek() } returns WeekDTO(defaultWeek)
        every { mockGamesQuery.getGamesForWeek(defaultWeek) } returns listOf(
            defaultGameDTO
        )
        every { mockPicksQuery.getPicksForWeek(defaultWeek) } returns listOf(
            emptyExistingPicks
        )

        every { mockVegasPicksApi.getVegasPicks() } returns listOf(
            PickWithSpreadDTO(defaultGame, defaultExpectedPick, defaultSpread)
        )
    }


    @Test
    fun oneVegasPickUpdatesPickForCurrentWeek() {
        updateVegasPicks(
            mockCurrentWeekQuery,
            mockGamesQuery,
            mockGameMutator,
            mockPickMutator,
            mockVegasPicksApi
        )


        Assertions.assertEquals(1, mutatorUser.size)
        Assertions.assertEquals("Vegas", mutatorUser.first().name)
        Assertions.assertEquals(defaultWeek, mutatorWeek.first().name)
        Assertions.assertEquals(defaultGame, mutatorPick.first().game)
        Assertions.assertEquals(defaultExpectedPick, mutatorPick.first().pick)
    }

    @Test
    fun oneVegasPickUpdatesWithCurrentWeek() {
        val week = "Week 1"
        every { mockCurrentWeekQuery.getCurrentWeek() } returns WeekDTO(week)
        every { mockGamesQuery.getGamesForWeek(week) } returns listOf(
            defaultGameDTO
        )

        updateVegasPicks(
            mockCurrentWeekQuery,
            mockGamesQuery,
            mockGameMutator,
            mockPickMutator,
            mockVegasPicksApi
        )

        Assertions.assertEquals(week, mutatorWeek.first().name)
    }

    @Test
    fun oneVegasPickUpdatesPickWithGame() {
        val game = "TB@NE"
        val expectedPick = "TB"
        every { mockGamesQuery.getGamesForWeek(defaultWeek) } returns listOf(
            GameDTO(game, defaultWeek).apply {
                gameTime = OffsetDateTime.now().plusDays(1)
                id = UUID.randomUUID()
            }
        )
        every { mockVegasPicksApi.getVegasPicks() } returns listOf(
            PickWithSpreadDTO(game, expectedPick, defaultSpread)
        )

        updateVegasPicks(
            mockCurrentWeekQuery,
            mockGamesQuery,
            mockGameMutator,
            mockPickMutator,
            mockVegasPicksApi
        )

        Assertions.assertEquals(game, mutatorPick.first().game)
    }

    @Test
    fun oneVegasPickUpdatesPickWithPick() {
        val game = "TB@NE"
        val expectedPick = "TB"
        every { mockGamesQuery.getGamesForWeek(defaultWeek) } returns listOf(
            GameDTO(game, defaultWeek).apply {
                gameTime = OffsetDateTime.now().plusDays(1)
                id = UUID.randomUUID()
            }
        )
        every { mockVegasPicksApi.getVegasPicks() } returns listOf(
            PickWithSpreadDTO(game, expectedPick, defaultSpread)
        )

        updateVegasPicks(
            mockCurrentWeekQuery,
            mockGamesQuery,
            mockGameMutator,
            mockPickMutator,
            mockVegasPicksApi
        )

        Assertions.assertEquals(expectedPick, mutatorPick.first().pick)
    }

    @Test
    fun oneVegasPickUpdatesSpreadForGame() {
        updateVegasPicks(
            mockCurrentWeekQuery,
            mockGamesQuery,
            mockGameMutator,
            mockPickMutator,
            mockVegasPicksApi
        )

        Assertions.assertEquals(1, gameMutations.size)
        val gameMutation = gameMutations.first()
        Assertions.assertEquals(defaultGame, gameMutation.name)
        Assertions.assertEquals(defaultWeek, gameMutation.week)
        Assertions.assertEquals(defaultSpread, gameMutation.spread)
        Assertions.assertEquals(defaultGameDTO.id, gameMutation.id)
    }

    @Test
    fun oneVegasPickUpdatesSpreadForVaryingSpread() {
        val expectedSpread = -87.0
        every { mockVegasPicksApi.getVegasPicks() } returns listOf(
            PickWithSpreadDTO(defaultGame, defaultExpectedPick, expectedSpread)
        )

        updateVegasPicks(
            mockCurrentWeekQuery,
            mockGamesQuery,
            mockGameMutator,
            mockPickMutator,
            mockVegasPicksApi
        )

        Assertions.assertEquals(1, gameMutations.size)
        val gameMutation = gameMutations.first()
        Assertions.assertEquals(expectedSpread, gameMutation.spread)
    }

    @Test
    fun twoVegasPicksUpdatesPick() {
        every { mockGamesQuery.getGamesForWeek(defaultWeek) } returns listOf(
            GameDTO("TB@NE", defaultWeek).apply {
                gameTime = OffsetDateTime.now().plusDays(1)
                id = UUID.randomUUID()
            },
            defaultGameDTO
        )
        every { mockVegasPicksApi.getVegasPicks() } returns listOf(
            PickWithSpreadDTO("TB@NE", "TB", -7.0),
            PickWithSpreadDTO(defaultGame, defaultExpectedPick, 3.0)
        )

        updateVegasPicks(
            mockCurrentWeekQuery,
            mockGamesQuery,
            mockGameMutator,
            mockPickMutator,
            mockVegasPicksApi
        )

        Assertions.assertEquals(2, mutatorUser.size)
        Assertions.assertEquals("Vegas", mutatorUser[1].name)
        Assertions.assertEquals(defaultGame, mutatorPick[1].game)
        Assertions.assertEquals(defaultExpectedPick, mutatorPick[1].pick)
    }

    @Test
    fun vegasPickWithoutMatchingGameInDbIsNotMade() {
        every { mockGamesQuery.getGamesForWeek(defaultWeek) } returns listOf()

        updateVegasPicks(
            mockCurrentWeekQuery,
            mockGamesQuery,
            mockGameMutator,
            mockPickMutator,
            mockVegasPicksApi
        )

        Assertions.assertEquals(0, mutatorUser.size)
    }

    @Test
    fun vegasPicksForGamesAboutToStartAreNotUpdated() {
        every { mockGamesQuery.getGamesForWeek(defaultWeek) } returns listOf(
            defaultGameDTO.apply {
                gameTime = OffsetDateTime.now().plusMinutes(14)
            }
        )

        updateVegasPicks(
            mockCurrentWeekQuery,
            mockGamesQuery,
            mockGameMutator,
            mockPickMutator,
            mockVegasPicksApi
        )

        Assertions.assertEquals(0, mutatorUser.size)
    }

    @Test
    fun vegasPicksForGamesWithoutGameTimeAreNotUpdated() {
        every { mockGamesQuery.getGamesForWeek(defaultWeek) } returns listOf(
            defaultGameDTO.apply {
                gameTime = null
            }
        )

        updateVegasPicks(
            mockCurrentWeekQuery,
            mockGamesQuery,
            mockGameMutator,
            mockPickMutator,
            mockVegasPicksApi
        )

        Assertions.assertEquals(0, mutatorUser.size)
    }
}