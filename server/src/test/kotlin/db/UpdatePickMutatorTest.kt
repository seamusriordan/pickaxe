@file:Suppress("UNCHECKED_CAST")

package db

import graphql.schema.DataFetchingEnvironment
import graphql.schema.DataFetchingEnvironmentImpl
import io.mockk.every
import io.mockk.mockkClass
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.sql.Connection
import java.sql.DriverManager
import java.sql.Statement

class UpdatePickMutatorTest {
    private lateinit var updatePickMutator: UpdatePickMutator

    private lateinit var mockStatement: Statement
    private lateinit var mockConnection: Connection

    @BeforeEach
    fun setup() {
        mockkStatic("java.sql.DriverManager")
        mockConnection = mockkClass(Connection::class)
        mockStatement = mockkClass(Statement::class)
        every { mockStatement.executeUpdate(any()) } returns 1


        every { DriverManager.getConnection(any()) } returns null
        every { DriverManager.getConnection(any(), any()) } returns mockConnection
        every { mockConnection.createStatement() } returns mockStatement

        updatePickMutator = UpdatePickMutator(mockConnection)
    }

    @Test
    fun pickForFirstGameCanBeSetByFetchingEnvironment() {
        val passedArguments
                = generatePickArguments("Person", "0", "GB@CHI", "Different")
        val userPick = passedArguments["userPick"] as HashMap<String, String>
        val env = buildEnvForArguments(passedArguments)
        val expectedQuery =
            buildInsertString(passedArguments, userPick)

        val result = updatePickMutator.get(env)

        verify { mockStatement.executeUpdate(expectedQuery) }
        assertTrue(result)
    }

    @Test
    fun pickForSecondGameCanBeSetByFetchingEnvironment() {
        val passedArguments
                = generatePickArguments("Person", "0", "BUF@NE", "Very Different")
        val userPick = passedArguments["userPick"] as HashMap<String, String>
        val env = buildEnvForArguments(passedArguments)
        val expectedQuery =
            buildInsertString(passedArguments, userPick)

        val result = updatePickMutator.get(env)

        verify { mockStatement.executeUpdate(expectedQuery) }
        assertTrue(result)
    }

    @Test
    fun pickForThirdGameWithSecondUserCanBeSetByFetchingEnvironment() {
        val passedArguments
                = generatePickArguments("Person2", "4", "SEA@PHI", "PHI")
        val userPick = passedArguments["userPick"] as HashMap<String, String>
        val env = buildEnvForArguments(passedArguments)
        val expectedQuery =
            buildInsertString(passedArguments, userPick)
        val result = updatePickMutator.get(env)

        verify { mockStatement.executeUpdate(expectedQuery) }
        assertTrue(result)
    }

    private fun buildInsertString(
        passedArguments: HashMap<String, Any>,
        userPick: HashMap<String, String>
    ): String {
        val name = passedArguments["name"]
        val week = userPick["week"]
        val game = userPick["game"]
        val pick = userPick["pick"]
        return "INSERT INTO userpicks VALUES ('$name', '$week', '$game', '$pick') " +
                "ON CONFLICT (name, week, game) DO UPDATE SET pick = '$pick'"
    }

    private fun generatePickArguments(name: String, week: String, game: String, pick: String): HashMap<String, Any> {
        val passedArguments: HashMap<String, Any> = HashMap()
        passedArguments["name"] = name
        passedArguments["userPick"] = generatePick(week, game, pick)
        return passedArguments
    }

    private fun generatePick(week: String, game: String, pick: String): HashMap<String, Any> {
        val userPick = HashMap<String, Any>()
        userPick["week"] = week
        userPick["game"] = game
        userPick["pick"] = pick
        return userPick
    }


    private fun buildEnvForArguments(passedArguments: HashMap<String, Any>): DataFetchingEnvironment {
        return DataFetchingEnvironmentImpl
            .newDataFetchingEnvironment()
            .arguments(passedArguments)
            .build()
    }
}