package services.utils

import db.CurrentWeekQuery
import db.GamesQuery
import db.UpdatePickMutator
import db.UserPickQuery
import dto.*
import graphql.schema.DataFetchingEnvironment
import io.mockk.every
import io.mockk.mockkClass
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import services.RandomPickSelector
import services.utils.RngUpdateUtils.Companion.makeRngPicksForCurrentWeek
import java.time.OffsetDateTime
import java.util.*
import kotlin.collections.HashMap

class RngUpdateUtilsTest {
    private val mockCurrentWeekQuery = mockkClass(CurrentWeekQuery::class)
    private val mockPicksQuery = mockkClass(UserPickQuery::class)
    private val mockGamesQuery = mockkClass(GamesQuery::class)
    private val mockPickMutator = mockkClass(UpdatePickMutator::class)
    private val mockRandomPickSelector = mockkClass(RandomPickSelector::class)

    private val rngUserName = "RNG"
    private val defaultWeek = "Week 0"
    private val defaultGame = "DET@CHI"
    private val defaultExpectedPick = "CHI"

    private val defaultGameDTO = GameDTO(defaultGame, defaultWeek).apply {
        gameTime = OffsetDateTime.now().plusDays(1)
        id = UUID.randomUUID()
    }

    private val emptyExistingPicks = UserPicksDTO(UserDTO(rngUserName))

    private val mutatorEnvs = mutableListOf<DataFetchingEnvironment>()

    init {
        every { mockPickMutator.get(capture(mutatorEnvs)) } returns true

        every { mockCurrentWeekQuery.getCurrentWeek() } returns WeekDTO(defaultWeek)
        every { mockGamesQuery.getGamesForWeek(defaultWeek) } returns listOf(
            defaultGameDTO
        )
        every { mockPicksQuery.getPicksForWeek(defaultWeek) } returns listOf(
            emptyExistingPicks
        )
        every { mockRandomPickSelector.chooseRandomFor(defaultGame) } returns defaultExpectedPick
    }

    @Test
    fun noRngPicksStillMakesPicks() {
        every { mockPicksQuery.getPicksForWeek(defaultWeek) } returns listOf()

        makeRngPicksForCurrentWeek(
            mockCurrentWeekQuery,
            mockGamesQuery,
            mockPicksQuery,
            mockPickMutator,
            mockRandomPickSelector
        )

        Assertions.assertEquals(1, mutatorEnvs.size)
    }

    @Test
    fun randomNumberGeneratorGamesAreMakeForCurrentWeekWhenNotDoneAlready() {
        makeRngPicksForCurrentWeek(
            mockCurrentWeekQuery,
            mockGamesQuery,
            mockPicksQuery,
            mockPickMutator,
            mockRandomPickSelector
        )

        Assertions.assertEquals(1, mutatorEnvs.size)
        val mutatorEnv = mutatorEnvs.first()
        Assertions.assertEquals(rngUserName, mutatorEnv.arguments["name"])
        val userPick = mutatorEnv.arguments["userPick"] as HashMap<*, *>
        Assertions.assertEquals(defaultWeek, userPick["week"])
        Assertions.assertEquals(defaultGame, userPick["game"])
        Assertions.assertEquals(defaultExpectedPick, userPick["pick"]!!)
    }

    @Test
    fun randomNumberGeneratorGamesAreMakeForCurrentWeekWhenPickIsBlank() {
        every { mockPicksQuery.getPicksForWeek(defaultWeek) } returns listOf(
            UserPicksDTO(UserDTO(rngUserName)).apply {
                picks = arrayListOf(PickDTO(defaultGame, ""))
            }
        )

        makeRngPicksForCurrentWeek(
            mockCurrentWeekQuery,
            mockGamesQuery,
            mockPicksQuery,
            mockPickMutator,
            mockRandomPickSelector
        )

        Assertions.assertEquals(1, mutatorEnvs.size)
    }

    @Test
    fun randomNumberGeneratorGamesAreMakeForCurrentWeekWhenPickIsNotOneOfTheTeams() {
        every { mockPicksQuery.getPicksForWeek(defaultWeek) } returns listOf(
            UserPicksDTO(UserDTO(rngUserName)).apply {
                picks = arrayListOf(PickDTO(defaultGame, "SLDKJFLKw"))
            }
        )

        makeRngPicksForCurrentWeek(
            mockCurrentWeekQuery,
            mockGamesQuery,
            mockPicksQuery,
            mockPickMutator,
            mockRandomPickSelector
        )

        Assertions.assertEquals(1, mutatorEnvs.size)
    }

    @Test
    fun randomNumberGeneratorDoesNotMakePicksForGamesBefore15MinutesOfGameStart() {
        every { mockGamesQuery.getGamesForWeek(defaultWeek) } returns listOf(
            defaultGameDTO.apply {
                gameTime = OffsetDateTime.now().plusMinutes(14)
            }
        )

        makeRngPicksForCurrentWeek(
            mockCurrentWeekQuery,
            mockGamesQuery,
            mockPicksQuery,
            mockPickMutator,
            mockRandomPickSelector
        )

        Assertions.assertEquals(0, mutatorEnvs.size)
    }

    @Test
    fun randomNumberGeneratorDoesNotMakePicksForGamesWithoutGameTime() {
        every { mockGamesQuery.getGamesForWeek(defaultWeek) } returns listOf(
            defaultGameDTO.apply {
                gameTime = null
            }
        )

        makeRngPicksForCurrentWeek(
            mockCurrentWeekQuery,
            mockGamesQuery,
            mockPicksQuery,
            mockPickMutator,
            mockRandomPickSelector
        )

        Assertions.assertEquals(0, mutatorEnvs.size)
    }

    @Test
    fun randomNumberGeneratorPicksUseWeek() {
        val week = "Week 1"
        every { mockCurrentWeekQuery.getCurrentWeek() } returns WeekDTO(week)
        every { mockGamesQuery.getGamesForWeek(week) } returns listOf(
            GameDTO(defaultGame, week).apply {
                gameTime = OffsetDateTime.now().plusDays(1)
                id = UUID.randomUUID()
            }
        )
        every { mockPicksQuery.getPicksForWeek(week) } returns listOf(
            emptyExistingPicks
        )

        makeRngPicksForCurrentWeek(
            mockCurrentWeekQuery,
            mockGamesQuery,
            mockPicksQuery,
            mockPickMutator,
            mockRandomPickSelector
        )

        val mutatorEnv = mutatorEnvs.first()
        val userPick = mutatorEnv.arguments["userPick"] as HashMap<*, *>
        Assertions.assertEquals(week, userPick["week"])
    }

    @Test
    fun randomNumberGeneratorPicksUseGame() {
        val game = "TB@NE"
        every { mockGamesQuery.getGamesForWeek(defaultWeek) } returns listOf(
            GameDTO(game, defaultWeek).apply {
                gameTime = OffsetDateTime.now().plusDays(1)
                id = UUID.randomUUID()
            }
        )

        every { mockRandomPickSelector.chooseRandomFor("TB@NE") } returns "TB"

        makeRngPicksForCurrentWeek(
            mockCurrentWeekQuery,
            mockGamesQuery,
            mockPicksQuery,
            mockPickMutator,
            mockRandomPickSelector
        )

        val mutatorEnv = mutatorEnvs.first()
        val userPick = mutatorEnv.arguments["userPick"] as HashMap<*, *>
        Assertions.assertEquals(game, userPick["game"])
    }

    @Test
    fun randomNumberGeneratorPicksUseRandomNumberPickerForAwayTeam() {
        val expectedPick = "DET"
        every { mockRandomPickSelector.chooseRandomFor(defaultGame) } returns expectedPick

        makeRngPicksForCurrentWeek(
            mockCurrentWeekQuery,
            mockGamesQuery,
            mockPicksQuery,
            mockPickMutator,
            mockRandomPickSelector
        )

        val mutatorEnv = mutatorEnvs.first()
        val userPick = mutatorEnv.arguments["userPick"] as HashMap<*, *>
        Assertions.assertEquals(expectedPick, userPick["pick"])
    }

    @Test
    fun randomNumberGeneratorGamesAreMakeForCurrentWeekWithTwoGamesWhenNotDoneAlready() {
        every { mockGamesQuery.getGamesForWeek(defaultWeek) } returns listOf(
            GameDTO("TB@NE", defaultWeek).apply {
                gameTime = OffsetDateTime.now().plusDays(1)
                id = UUID.randomUUID()
            },
            GameDTO(defaultGame, defaultWeek).apply {
                gameTime = OffsetDateTime.now().plusDays(1)
                id = UUID.randomUUID()
            }
        )

        every { mockRandomPickSelector.chooseRandomFor("TB@NE") } returns "TB"

        makeRngPicksForCurrentWeek(
            mockCurrentWeekQuery,
            mockGamesQuery,
            mockPicksQuery,
            mockPickMutator,
            mockRandomPickSelector
        )

        Assertions.assertEquals(2, mutatorEnvs.size)
        val mutatorEnv = mutatorEnvs[1]
        Assertions.assertEquals("RNG", mutatorEnv.arguments["name"])
        val userPick = mutatorEnv.arguments["userPick"] as HashMap<*, *>
        Assertions.assertEquals(defaultGame, userPick["game"])
        Assertions.assertEquals(defaultExpectedPick, userPick["pick"]!!)
    }


    @Test
    fun randomNumberGeneratorSkipsPicksThatAreAlreadyMade() {
        val pickedGame = "TB@NE"
        every { mockGamesQuery.getGamesForWeek(defaultWeek) } returns listOf(
           defaultGameDTO,
            GameDTO(pickedGame, defaultWeek).apply {
                gameTime = OffsetDateTime.now().plusDays(1)
                id = UUID.randomUUID()
            }
        )
        every { mockPicksQuery.getPicksForWeek(defaultWeek) } returns listOf(
            UserPicksDTO(UserDTO(rngUserName)).apply {
                picks = arrayListOf(PickDTO(pickedGame, "NE"))
            }
        )
        every { mockRandomPickSelector.chooseRandomFor(pickedGame) } returns "TB"

        makeRngPicksForCurrentWeek(
            mockCurrentWeekQuery,
            mockGamesQuery,
            mockPicksQuery,
            mockPickMutator,
            mockRandomPickSelector
        )

        Assertions.assertEquals(1, mutatorEnvs.size)
        val mutatorEnv = mutatorEnvs.first()
        val userPick = mutatorEnv.arguments["userPick"] as HashMap<*, *>
        Assertions.assertEquals(defaultGame, userPick["game"])
    }

    @Test
    fun randomNumberGeneratorUsesOnlyRngPicks() {
        val pickedGame = "TB@NE"
        every { mockGamesQuery.getGamesForWeek(defaultWeek) } returns listOf(
            defaultGameDTO,
            GameDTO(pickedGame, defaultWeek).apply {
                gameTime = OffsetDateTime.now().plusDays(1)
                id = UUID.randomUUID()
            }
        )
        every { mockPicksQuery.getPicksForWeek(defaultWeek) } returns listOf(
            UserPicksDTO(UserDTO("Someone Else")).apply {
                picks = arrayListOf(PickDTO(pickedGame, "NE"))
            },
            emptyExistingPicks
        )
        every { mockRandomPickSelector.chooseRandomFor(pickedGame) } returns "TB"

        makeRngPicksForCurrentWeek(
            mockCurrentWeekQuery,
            mockGamesQuery,
            mockPicksQuery,
            mockPickMutator,
            mockRandomPickSelector
        )

        Assertions.assertEquals(2, mutatorEnvs.size)
    }

}