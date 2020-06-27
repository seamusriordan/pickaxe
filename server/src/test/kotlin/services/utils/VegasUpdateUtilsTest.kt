package services.utils

import db.CurrentWeekQuery
import db.GameMutator
import db.GamesQuery
import db.UpdatePickMutator
import dto.GameDTO
import dto.PickWithSpreadDTO
import dto.WeekDTO
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



    @Test
    fun oneVegasPickUpdatesPickForCurrentWeek() {
        val mockCurrentWeekQuery = mockkClass(CurrentWeekQuery::class)
        val mockGamesQuery = mockkClass(GamesQuery::class)
        val mockPickMutator = mockkClass(UpdatePickMutator::class)
        val mockGameMutator = mockkClass(GameMutator::class)
        val mockVegasPicksApi = mockkClass(VegasPicksApi::class)

        val pickMutatorEnvs = mutableListOf<DataFetchingEnvironment>()
        val gameMutations = mutableListOf<GameDTO>()

        val week = "Week 0"
        val game = "DET@CHI"
        val expectedPick = "CHI"
        every { mockCurrentWeekQuery.getCurrentWeek() } returns WeekDTO(week)
        every { mockGamesQuery.getGamesForWeek(week) } returns listOf(
            GameDTO(game, week).apply {
                gameTime = OffsetDateTime.now().plusDays(1)
                id = UUID.randomUUID()
            }
        )

        every { mockPickMutator.get(capture(pickMutatorEnvs)) } returns true
        every { mockGameMutator.putInDatabase(capture(gameMutations)) } returns Unit
        every { mockVegasPicksApi.getVegasPicks() } returns listOf(
            PickWithSpreadDTO(game, expectedPick, -7.0)
        )

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
        Assertions.assertEquals(week, userPick["week"])
        Assertions.assertEquals(game, userPick["game"])
        Assertions.assertEquals(expectedPick, userPick["pick"]!!)
    }

    @Test
    fun oneVegasPickUpdatesWithCurrentWeek() {
        val mockCurrentWeekQuery = mockkClass(CurrentWeekQuery::class)
        val mockGamesQuery = mockkClass(GamesQuery::class)
        val mockPickMutator = mockkClass(UpdatePickMutator::class)
        val mockGameMutator = mockkClass(GameMutator::class)
        val mockVegasPicksApi = mockkClass(VegasPicksApi::class)

        val pickMutatorEnvs = mutableListOf<DataFetchingEnvironment>()
        val gameMutations = mutableListOf<GameDTO>()

        val week = "Week 1"
        val game = "DET@CHI"
        val expectedPick = "CHI"
        every { mockCurrentWeekQuery.getCurrentWeek() } returns WeekDTO(week)
        every { mockGamesQuery.getGamesForWeek(week) } returns listOf(
            GameDTO(game, week).apply {
                gameTime = OffsetDateTime.now().plusDays(1)
                id = UUID.randomUUID()
            }
        )

        every { mockPickMutator.get(capture(pickMutatorEnvs)) } returns true
        every { mockGameMutator.putInDatabase(capture(gameMutations)) } returns Unit
        every { mockVegasPicksApi.getVegasPicks() } returns listOf(
            PickWithSpreadDTO(game, expectedPick, -7.0)
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
        val mockCurrentWeekQuery = mockkClass(CurrentWeekQuery::class)
        val mockGamesQuery = mockkClass(GamesQuery::class)
        val mockPickMutator = mockkClass(UpdatePickMutator::class)
        val mockGameMutator = mockkClass(GameMutator::class)
        val mockVegasPicksApi = mockkClass(VegasPicksApi::class)

        val pickMutatorEnvs = mutableListOf<DataFetchingEnvironment>()
        val gameMutations = mutableListOf<GameDTO>()

        val week = "Week 1"
        val game = "TB@NE"
        val expectedPick = "TB"
        every { mockCurrentWeekQuery.getCurrentWeek() } returns WeekDTO(week)
        every { mockGamesQuery.getGamesForWeek(week) } returns listOf(
            GameDTO(game, week).apply {
                gameTime = OffsetDateTime.now().plusDays(1)
                id = UUID.randomUUID()
            }
        )

        every { mockPickMutator.get(capture(pickMutatorEnvs)) } returns true
        every { mockGameMutator.putInDatabase(capture(gameMutations)) } returns Unit
        every { mockVegasPicksApi.getVegasPicks() } returns listOf(
            PickWithSpreadDTO(game, expectedPick, -7.0)
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
        val mockCurrentWeekQuery = mockkClass(CurrentWeekQuery::class)
        val mockGamesQuery = mockkClass(GamesQuery::class)
        val mockPickMutator = mockkClass(UpdatePickMutator::class)
        val mockGameMutator = mockkClass(GameMutator::class)
        val mockVegasPicksApi = mockkClass(VegasPicksApi::class)

        val pickMutatorEnvs = mutableListOf<DataFetchingEnvironment>()
        val gameMutations = mutableListOf<GameDTO>()

        val week = "Week 1"
        val game = "TB@NE"
        val expectedPick = "TB"
        every { mockCurrentWeekQuery.getCurrentWeek() } returns WeekDTO(week)
        every { mockGamesQuery.getGamesForWeek(week) } returns listOf(
            GameDTO(game, week).apply {
                gameTime = OffsetDateTime.now().plusDays(1)
                id = UUID.randomUUID()
            }
        )

        every { mockPickMutator.get(capture(pickMutatorEnvs)) } returns true
        every { mockGameMutator.putInDatabase(capture(gameMutations)) } returns Unit
        every { mockVegasPicksApi.getVegasPicks() } returns listOf(
            PickWithSpreadDTO(game, expectedPick, -7.0)
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
        val mockCurrentWeekQuery = mockkClass(CurrentWeekQuery::class)
        val mockGamesQuery = mockkClass(GamesQuery::class)
        val mockPickMutator = mockkClass(UpdatePickMutator::class)
        val mockGameMutator = mockkClass(GameMutator::class)
        val mockVegasPicksApi = mockkClass(VegasPicksApi::class)

        val pickMutatorEnvs = mutableListOf<DataFetchingEnvironment>()
        val gameMutations = mutableListOf<GameDTO>()

        val week = "Week 1"
        val game = "TB@NE"
        val expectedPick = "TB"
        every { mockCurrentWeekQuery.getCurrentWeek() } returns WeekDTO(week)
        val expectedGameTime = OffsetDateTime.now().plusDays(1)
        val randomUUID = UUID.randomUUID()
        every { mockGamesQuery.getGamesForWeek(week) } returns listOf(
            GameDTO(game, week).apply {
                gameTime = expectedGameTime
                id = randomUUID
            }
        )

        every { mockPickMutator.get(capture(pickMutatorEnvs)) } returns true
        every { mockGameMutator.putInDatabase(capture(gameMutations)) } returns Unit
        val expectedSpread = -7.0
        every { mockVegasPicksApi.getVegasPicks() } returns listOf(
            PickWithSpreadDTO(game, expectedPick, expectedSpread)
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
        Assertions.assertEquals(game, gameMutation.name)
        Assertions.assertEquals(week, gameMutation.week)
        Assertions.assertEquals(expectedSpread, gameMutation.spread)
        Assertions.assertEquals(randomUUID, gameMutation.id)
    }

    @Test
    fun oneVegasPickUpdatesSpreadForVaryingSpread() {
        val mockCurrentWeekQuery = mockkClass(CurrentWeekQuery::class)
        val mockGamesQuery = mockkClass(GamesQuery::class)
        val mockPickMutator = mockkClass(UpdatePickMutator::class)
        val mockGameMutator = mockkClass(GameMutator::class)
        val mockVegasPicksApi = mockkClass(VegasPicksApi::class)

        val pickMutatorEnvs = mutableListOf<DataFetchingEnvironment>()
        val gameMutations = mutableListOf<GameDTO>()

        val week = "Week 1"
        val game = "TB@NE"
        val expectedPick = "TB"
        every { mockCurrentWeekQuery.getCurrentWeek() } returns WeekDTO(week)
        val expectedGameTime = OffsetDateTime.now().plusDays(1)
        val randomUUID = UUID.randomUUID()
        every { mockGamesQuery.getGamesForWeek(week) } returns listOf(
            GameDTO(game, week).apply {
                gameTime = expectedGameTime
                id = randomUUID
            }
        )

        every { mockPickMutator.get(capture(pickMutatorEnvs)) } returns true
        every { mockGameMutator.putInDatabase(capture(gameMutations)) } returns Unit
        val expectedSpread = -87.0
        every { mockVegasPicksApi.getVegasPicks() } returns listOf(
            PickWithSpreadDTO(game, expectedPick, expectedSpread)
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
        val mockCurrentWeekQuery = mockkClass(CurrentWeekQuery::class)
        val mockGamesQuery = mockkClass(GamesQuery::class)
        val mockPickMutator = mockkClass(UpdatePickMutator::class)
        val mockGameMutator = mockkClass(GameMutator::class)
        val mockVegasPicksApi = mockkClass(VegasPicksApi::class)

        val pickMutatorEnvs = mutableListOf<DataFetchingEnvironment>()
        val gameMutations = mutableListOf<GameDTO>()

        val week = "Week 0"
        val game = "DET@CHI"
        val expectedPick = "CHI"
        every { mockCurrentWeekQuery.getCurrentWeek() } returns WeekDTO(week)
        every { mockGamesQuery.getGamesForWeek(week) } returns listOf(
            GameDTO("TB@NE", week).apply {
                gameTime = OffsetDateTime.now().plusDays(1)
                id = UUID.randomUUID()
            },
            GameDTO(game, week).apply {
                gameTime = OffsetDateTime.now().plusDays(1)
                id = UUID.randomUUID()
            }
        )

        every { mockPickMutator.get(capture(pickMutatorEnvs)) } returns true
        every { mockGameMutator.putInDatabase(capture(gameMutations)) } returns Unit
        every { mockVegasPicksApi.getVegasPicks() } returns listOf(
            PickWithSpreadDTO("TB@NE", "TB", -7.0),
            PickWithSpreadDTO(game, expectedPick, 3.0)
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
        Assertions.assertEquals(game, userPick["game"])
        Assertions.assertEquals(expectedPick, userPick["pick"]!!)
    }

    @Test
    fun vegasPickWithoutMatchingGameInDbIsNotMade() {
        val mockCurrentWeekQuery = mockkClass(CurrentWeekQuery::class)
        val mockGamesQuery = mockkClass(GamesQuery::class)
        val mockPickMutator = mockkClass(UpdatePickMutator::class)
        val mockGameMutator = mockkClass(GameMutator::class)
        val mockVegasPicksApi = mockkClass(VegasPicksApi::class)

        val pickMutatorEnvs = mutableListOf<DataFetchingEnvironment>()
        val gameMutations = mutableListOf<GameDTO>()

        val week = "Week 0"
        val game = "DET@CHI"
        val expectedPick = "CHI"
        every { mockCurrentWeekQuery.getCurrentWeek() } returns WeekDTO(week)
        every { mockGamesQuery.getGamesForWeek(week) } returns listOf()

        every { mockPickMutator.get(capture(pickMutatorEnvs)) } returns true
        every { mockGameMutator.putInDatabase(capture(gameMutations)) } returns Unit
        every { mockVegasPicksApi.getVegasPicks() } returns listOf(
            PickWithSpreadDTO(game, expectedPick, 3.0)
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
    fun vegasPicksForGamesAboutToStartAreNotUpdated() {
        val mockCurrentWeekQuery = mockkClass(CurrentWeekQuery::class)
        val mockGamesQuery = mockkClass(GamesQuery::class)
        val mockPickMutator = mockkClass(UpdatePickMutator::class)
        val mockGameMutator = mockkClass(GameMutator::class)
        val mockVegasPicksApi = mockkClass(VegasPicksApi::class)

        val pickMutatorEnvs = mutableListOf<DataFetchingEnvironment>()
        val gameMutations = mutableListOf<GameDTO>()

        val week = "Week 0"
        val game = "DET@CHI"
        val expectedPick = "CHI"
        every { mockCurrentWeekQuery.getCurrentWeek() } returns WeekDTO(week)
        every { mockGamesQuery.getGamesForWeek(week) } returns listOf(
            GameDTO(game, week).apply {
                gameTime = OffsetDateTime.now().plusMinutes(14)
                id = UUID.randomUUID()
            }
        )

        every { mockPickMutator.get(capture(pickMutatorEnvs)) } returns true
        every { mockGameMutator.putInDatabase(capture(gameMutations)) } returns Unit
        every { mockVegasPicksApi.getVegasPicks() } returns listOf(
            PickWithSpreadDTO("TB@NE", "TB", -7.0),
            PickWithSpreadDTO(game, expectedPick, 3.0)
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
        val mockCurrentWeekQuery = mockkClass(CurrentWeekQuery::class)
        val mockGamesQuery = mockkClass(GamesQuery::class)
        val mockPickMutator = mockkClass(UpdatePickMutator::class)
        val mockGameMutator = mockkClass(GameMutator::class)
        val mockVegasPicksApi = mockkClass(VegasPicksApi::class)

        val pickMutatorEnvs = mutableListOf<DataFetchingEnvironment>()
        val gameMutations = mutableListOf<GameDTO>()

        val week = "Week 0"
        val game = "DET@CHI"
        val expectedPick = "CHI"
        every { mockCurrentWeekQuery.getCurrentWeek() } returns WeekDTO(week)
        every { mockGamesQuery.getGamesForWeek(week) } returns listOf(
            GameDTO(game, week).apply {
                gameTime = null
                id = UUID.randomUUID()
            }
        )

        every { mockPickMutator.get(capture(pickMutatorEnvs)) } returns true
        every { mockGameMutator.putInDatabase(capture(gameMutations)) } returns Unit
        every { mockVegasPicksApi.getVegasPicks() } returns listOf(
            PickWithSpreadDTO("TB@NE", "TB", -7.0),
            PickWithSpreadDTO(game, expectedPick, 3.0)
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