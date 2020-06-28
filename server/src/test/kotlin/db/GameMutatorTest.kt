package db

import dto.GameDTO
import io.mockk.every
import io.mockk.mockkClass
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.sql.Connection
import java.sql.DriverManager
import java.sql.Statement
import java.time.OffsetDateTime
import java.util.*

class GameMutatorTest {
    private lateinit var mockStatement: Statement
    private lateinit var mockConnection: Connection

    private lateinit var gameMutator: GameMutator

    @BeforeEach
    fun setup() {
        mockkStatic("java.sql.DriverManager")
        mockConnection = mockkClass(Connection::class)
        mockStatement = mockkClass(Statement::class)
        every { mockStatement.executeUpdate(any()) } returns 1


        every { DriverManager.getConnection(any()) } returns null
        every { DriverManager.getConnection(any(), any()) } returns mockConnection
        every { mockConnection.createStatement() } returns mockStatement

        gameMutator = GameMutator(mockConnection)
    }

    @Test
    fun instantiates() {
        GameMutator(mockConnection)
    }

    @Test
    fun updateFinishedGamePutsInDatabase() {
        val game = GameDTO("GB@CHI", "Week 1").apply {
            result = "CHI"
            id = UUID.randomUUID()
            gameTime = OffsetDateTime.now()
        }
        val expectedQuery = buildInsertStringWithFinalResult(game)

        gameMutator.putInDatabase(game)

        verify {
            mockStatement.executeUpdate(expectedQuery)
        }
    }

    @Test
    fun updateFinishedGamePutsInDatabaseWithGameTBatNE() {
        val game = GameDTO("TB@NE", "Week 1").apply {
            result = "NE"
            id = UUID.randomUUID()
            gameTime = OffsetDateTime.now()
        }
        val expectedQuery = buildInsertStringWithFinalResult(game)

        gameMutator.putInDatabase(game)

        verify {
            mockStatement.executeUpdate(expectedQuery)
        }
    }

    @Test
    fun updateFinishedGamePutsInDatabaseWithWeek2() {
        val game = GameDTO("TB@NE", "Week 2").apply {
            result = "NE"
            id = UUID.randomUUID()
            gameTime = OffsetDateTime.now()
        }
        val expectedQuery = buildInsertStringWithFinalResult(game)

        gameMutator.putInDatabase(game)

        verify {
            mockStatement.executeUpdate(expectedQuery)
        }
    }

    @Test
    fun updateFinishedGamePutsInDatabaseWithGametimeFewDaysAgo() {
        val game = GameDTO("TB@NE", "Week 2").apply {
            result = "NE"
            id = UUID.randomUUID()
            gameTime = OffsetDateTime.now().minusDays(10)
        }
        val expectedQuery = buildInsertStringWithFinalResult(game)

        gameMutator.putInDatabase(game)

        verify {
            mockStatement.executeUpdate(expectedQuery)
        }
    }

    @Test
    fun updateUnfinishedGamePutsInDatabase() {
        val game = GameDTO("GB@CHI", "Week 1").apply {
            id = UUID.randomUUID()
            gameTime = OffsetDateTime.now()
        }
        val expectedQuery = buildInsertStringNoResult(game)

        gameMutator.putInDatabase(game)

        verify {
            mockStatement.executeUpdate(expectedQuery)
        }
    }

    @Test
    fun updateUnfinishedGamePutsInDatabaseWithGameTBatNE() {
        val game = GameDTO("TB@NE", "Week 1").apply {
            id = UUID.randomUUID()
            gameTime = OffsetDateTime.now()
        }
        val expectedQuery = buildInsertStringNoResult(game)

        gameMutator.putInDatabase(game)

        verify {
            mockStatement.executeUpdate(expectedQuery)
        }
    }


    @Test
    fun updateUnfinishedGamePutsInDatabaseWithWeek4() {
        val game = GameDTO("TB@NE", "Week 4").apply {
            id = UUID.randomUUID()
            gameTime = OffsetDateTime.now()
        }
        val expectedQuery = buildInsertStringNoResult(game)

        gameMutator.putInDatabase(game)

        verify {
            mockStatement.executeUpdate(expectedQuery)
        }
    }

    @Test
    fun updateUnfinishedGameWithSpreadPutsInDatabase() {
        val game = GameDTO("TB@NE", "Week 4").apply {
            id = UUID.randomUUID()
            gameTime = OffsetDateTime.now()
            spread = -7.0
        }
        val expectedQuery = buildInsertStringSpreadAndNoResult(game)

        gameMutator.putInDatabase(game)

        verify {
            mockStatement.executeUpdate(expectedQuery)
        }
    }

    @Test
    fun updateWithoutGameDetailsId() {
        val game = GameDTO("BAL@CIN", "Week 12").apply {
            id = null
            gameTime = OffsetDateTime.now()
        }
        val expectedQuery = buildInsertStringNoID(game)

        gameMutator.putInDatabase(game)

        verify {
            mockStatement.executeUpdate(expectedQuery)
        }
    }


    private fun buildInsertStringWithFinalResult(
        game: GameDTO
    ): String {
        return "INSERT INTO games(game, week, gametime, final, result, id) VALUES ('${game.name}', '${game.week}', '${game.gameTime}', true, '${game.result}', '${game.id}') " +
                "ON CONFLICT (game, week) DO UPDATE SET (gametime, final, result, id) = ('${game.gameTime}', true, '${game.result}', '${game.id}')"
    }

    private fun buildInsertStringNoResult(
        game: GameDTO
    ): String {
        return "INSERT INTO games(game, week, gametime, final, id) VALUES ('${game.name}', '${game.week}', '${game.gameTime}', false, '${game.id}') " +
                "ON CONFLICT (game, week) DO UPDATE SET (gametime, final, id) = ('${game.gameTime}', false, '${game.id}')"
    }

    private fun buildInsertStringSpreadAndNoResult(
        game: GameDTO
    ): String {
        return "INSERT INTO games(game, week, gametime, final, spread, id) VALUES ('${game.name}', '${game.week}', '${game.gameTime}', false, '${game.spread}', '${game.id}') " +
                "ON CONFLICT (game, week) DO UPDATE SET (gametime, final, spread, id) = ('${game.gameTime}', false, '${game.spread}', '${game.id}')"
    }

    private fun buildInsertStringNoID(
        game: GameDTO
    ): String {
        return "INSERT INTO games(game, week, gametime, final) VALUES ('${game.name}', '${game.week}', '${game.gameTime}', false) " +
                "ON CONFLICT (game, week) DO UPDATE SET (gametime, final) = ('${game.gameTime}', false)"
    }
}