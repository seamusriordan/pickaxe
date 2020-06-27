package services.utils

import db.*
import dto.*
import graphql.schema.DataFetchingEnvironment
import io.mockk.every
import io.mockk.mockkClass
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import services.VegasPicksApi
import services.utils.VegasUpdateUtils.Companion.updateVegasPicks
import java.time.OffsetDateTime
import java.util.*
import kotlin.collections.HashMap

class VegasUpdateUtilsTest {
    private val mockCurrentWeekQuery = mockkClass(CurrentWeekQuery::class)
    private val mockPicksQuery = mockkClass(UserPickQuery::class)
    private val mockGamesQuery = mockkClass(GamesQuery::class)
    private val mockPickMutator = mockkClass(UpdatePickMutator::class)
    private val mockGameMutator = mockkClass(GameMutator::class)
    private val mockVegasPicksApi = mockkClass(VegasPicksApi::class)


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

    private val pickMutatorEnvs = mutableListOf<DataFetchingEnvironment>()
    private val gameMutations = mutableListOf<GameDTO>()


    init {
        every { mockPickMutator.get(capture(pickMutatorEnvs)) } returns true
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

        Assertions.assertEquals(1, pickMutatorEnvs.size)
        val mutatorEnv = pickMutatorEnvs.first()
        Assertions.assertEquals("Vegas", mutatorEnv.arguments["name"])
        val userPick = mutatorEnv.arguments["userPick"] as HashMap<*, *>
        Assertions.assertEquals(defaultWeek, userPick["week"])
        Assertions.assertEquals(defaultGame, userPick["game"])
        Assertions.assertEquals(defaultExpectedPick, userPick["pick"]!!)
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

        val mutatorEnv = pickMutatorEnvs.first()
        val userPick = mutatorEnv.arguments["userPick"] as HashMap<*, *>
        Assertions.assertEquals(week, userPick["week"])
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

        val mutatorEnv = pickMutatorEnvs.first()
        val userPick = mutatorEnv.arguments["userPick"] as HashMap<*, *>
        Assertions.assertEquals(game, userPick["game"])
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

        val mutatorEnv = pickMutatorEnvs.first()
        val userPick = mutatorEnv.arguments["userPick"] as HashMap<*, *>
        Assertions.assertEquals(expectedPick, userPick["pick"])
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

        Assertions.assertEquals(2, pickMutatorEnvs.size)
        val mutatorEnv = pickMutatorEnvs[1]
        val userPick = mutatorEnv.arguments["userPick"] as HashMap<*, *>
        Assertions.assertEquals(defaultGame, userPick["game"])
        Assertions.assertEquals(defaultExpectedPick, userPick["pick"]!!)
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

        Assertions.assertEquals(0, pickMutatorEnvs.size)
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

        Assertions.assertEquals(0, pickMutatorEnvs.size)
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

        Assertions.assertEquals(0, pickMutatorEnvs.size)
    }
}