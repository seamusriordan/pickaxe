import com.auth0.AuthenticationController
import com.auth0.Tokens
import graphql.GraphQL
import graphql.schema.idl.SchemaGenerator
import io.javalin.http.Context
import io.javalin.websocket.WsContext
import io.mockk.*
import org.apache.commons.codec.digest.DigestUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.concurrent.Future
import javax.servlet.http.Cookie

class HttpHandlersTest {
    private val idQueryBody = "{\"operationName\":\"Query\",\"variables\":{},\"query\":\"query Query {\\n  id\\n}\\n\"}"
    private val nullOpBody = "{\"operationName\":null,\"variables\":{},\"query\":\"query Query {\\n  id\\n}\\n\"}"
    private val userQueryBody =
        "{\"operationName\":\"Query\",\"variables\":{},\"query\":\"query Query {\\n  user {\\n\\n  name}\\n}\\n\"}"
    private val mutationQueryBody0 =
        "{\"operationName\":\"Mutation\",\"variables\":{ \"id\": 0 },\"query\":\"mutation Mutation(\$id: Int){\\n  mutate(id: \$id) }\\n\"}"
    private val mutationQueryBody7 =
        "{\"operationName\":\"Mutation\",\"variables\":{ \"id\": 7 },\"query\":\"mutation Mutation(\$id: Int){\\n  mutate(id: \$id) }\\n\"}"

    @Test
    fun extractExecutionInputFromPostBodyForIdQuery() {
        val mockContext = mockkClass(Context::class)
        every { mockContext.body() } returns idQueryBody
        val engine: GraphQL = sampleGraphQL()

        val input = extractExecutionInputFromContext(mockContext)
        val result = engine.execute(input)

        val expectedResult = 44
        assertEquals(expectedResult, result.getData<Map<String, Int>>()["id"])
    }

    @Test
    fun extractExecutionInputFromPostBodyForUserWithNameQuery() {
        val mockContext = mockkClass(Context::class)
        every { mockContext.body() } returns userQueryBody
        val engine: GraphQL = sampleGraphQL()

        val input = extractExecutionInputFromContext(mockContext)
        val result = engine.execute(input)

        val expectedResult = "JImm"
        assertEquals(expectedResult, result.getData<Map<String, Map<String, String>>>()["user"]?.get("name"))
    }

    @Test
    fun extractExecutionInputVariablesFromMutationBodyForId7() {
        val mockContext = mockkClass(Context::class)
        every { mockContext.body() } returns mutationQueryBody7
        sampleGraphQL()

        val input = extractExecutionInputFromContext(mockContext)

        assertEquals("{id=7}", input.variables.toString())
    }

    @Test
    fun extractExecutionInputVariablesFromMutationBodyForId0() {
        val mockContext = mockkClass(Context::class)
        every { mockContext.body() } returns mutationQueryBody0
        sampleGraphQL()

        val input = extractExecutionInputFromContext(mockContext)

        assertEquals("{id=0}", input.variables.toString())
    }

    @Test
    fun extractExecutionInputOperationFromMutationQuery() {
        val mockContext = mockkClass(Context::class)
        every { mockContext.body() } returns mutationQueryBody7
        sampleGraphQL()

        val input = extractExecutionInputFromContext(mockContext)

        assertEquals("Mutation", input.operationName)
    }


    @Test
    fun optionsHandlerSetsCorrectHeaders() {
        val mockContext = mockkClass(Context::class)
        every { mockContext.header(any(), any()) } returns mockContext

        optionsHandler()(mockContext)

        verify { mockContext.header("Access-Control-Allow-Origin", "*") }
        verify { mockContext.header("Access-Control-Allow-Methods", "OPTIONS, POST, GET") }
        verify { mockContext.header("Access-Control-Allow-Headers", "*") }
        verify { mockContext.header("Access-Control-Max-Age", "86400") }
    }

    private fun sampleGraphQL(): GraphQL {
        val registry = sampleTypeDefinitionRegistry()
        val wiring = sampleRuntimeWiring()

        val schemaGenerator = SchemaGenerator()
        val graphqlSchema = schemaGenerator.makeExecutableSchema(registry, wiring)

        return GraphQL.newGraphQL(graphqlSchema).build()
    }

    @Test
    fun postHandlerServesQueryResponse() {
        val mockContext = createMockQueryContext()
        val resultSlot = slot<String>()

        postHandler(sampleGraphQL(), ArrayList(0))(mockContext)

        verify { mockContext.header("Access-Control-Allow-Origin", "*") }
        verify { mockContext.result(capture(resultSlot)) }
        val expectedResult = "{\"data\":{\"id\":44}}"
        assertEquals(expectedResult, resultSlot.captured)
    }

    @Test
    fun nullOperationNameDoesntCrashServer() {
        val mockContext = createMockNullOpContext()

        postHandler(sampleGraphQL(), ArrayList(0))(mockContext)
    }

    @Test
    fun postHandlerWithOneOpenContextSendsMessage() {
        System.setProperty("skip_ws_session_null_check", "true")
        val mockContext = createMockMutationContext()

        val wsContexts = ArrayList<WsContext?>(0)
        val openWsContext = createWsContext()
        wsContexts.add(openWsContext)

        postHandler(sampleGraphQL(), wsContexts)(mockContext)

        verify { openWsContext.send(any<String>()) }
    }

    private fun createMockNullOpContext(): Context {
        val mockContext = mockkClass(Context::class)
        every { mockContext.body() } returns nullOpBody
        every { mockContext.header(any(), any()) } returns mockContext
        every { mockContext.result(any<String>()) } returns mockContext
        return mockContext
    }

    private fun createMockQueryContext(): Context {
        val mockContext = mockkClass(Context::class)
        every { mockContext.body() } returns idQueryBody
        every { mockContext.header(any(), any()) } returns mockContext
        every { mockContext.result(any<String>()) } returns mockContext
        return mockContext
    }

    private fun createMockMutationContext(): Context {
        val mockContext = mockkClass(Context::class)
        every { mockContext.body() } returns mutationQueryBody0
        every { mockContext.header(any(), any()) } returns mockContext
        every { mockContext.result(any<String>()) } returns mockContext
        return mockContext
    }

    @Test
    fun postHandlerForMutationWithTwoOpenContextSendsMessageToEach() {
        System.setProperty("skip_ws_session_null_check", "true")
        val mockContext = createMockMutationContext()

        val wsContexts = ArrayList<WsContext?>(0)
        val openWsContext1 = createWsContext()
        val openWsContext2 = createWsContext()
        wsContexts.add(openWsContext1)
        wsContexts.add(openWsContext2)

        postHandler(sampleGraphQL(), wsContexts)(mockContext)

        verify { openWsContext1.send(any<String>()) }
        verify { openWsContext2.send(any<String>()) }
    }

    @Test
    fun postHandlerForQueryWithTwoOpenContextSendsNoMessage() {
        val mockContext = createMockQueryContext()

        val wsContexts = ArrayList<WsContext?>(0)
        val openWsContext1 = createWsContext()
        val openWsContext2 = createWsContext()
        wsContexts.add(openWsContext1)
        wsContexts.add(openWsContext2)

        postHandler(sampleGraphQL(), wsContexts)(mockContext)

        verify(exactly = 0) { openWsContext1.send(any<String>()) }
        verify(exactly = 0) { openWsContext2.send(any<String>()) }
    }

    @Test
    fun `callback adds known hash for token`() {
        val mockContext = mockkClass(Context::class)

        val mockAuthController = mockk<AuthenticationController>()
        val accessManager = PickaxeAccessManager(mockAuthController)
        val mockTokens = mockk<Tokens>()
        every {mockAuthController.handle(any(), any())} returns mockTokens
        val accessTokenString = "fakeaccesstoken"
        every {mockTokens.accessToken} returns accessTokenString
        every {mockTokens.idToken} returns "fakeidtoken"

        callbackHandler(accessManager)(mockContext)

        assertEquals(1, accessManager.authHashes.size)
        assertEquals(DigestUtils.md5Hex(accessTokenString), accessManager.authHashes.first())
    }

    @Test
    fun `callback added hash varies with token`() {
        val mockContext = mockkClass(Context::class)

        val mockAuthController = mockk<AuthenticationController>()
        val accessManager = PickaxeAccessManager(mockAuthController)
        val mockTokens = mockk<Tokens>()
        every {mockAuthController.handle(any(), any())} returns mockTokens
        val accessTokenString = "different access token"
        every {mockTokens.accessToken} returns accessTokenString
        every {mockTokens.idToken} returns "fakeidtoken"

        callbackHandler(accessManager)(mockContext)

        assertEquals(1, accessManager.authHashes.size)
        assertEquals(DigestUtils.md5Hex(accessTokenString), accessManager.authHashes.first())
    }

    @Test
    fun `callback success adds cookie`() {
        val cookieSlot = slot<Cookie>()

        val mockContext = mockkClass(Context::class)
        every {mockContext.cookie(capture(cookieSlot))} returns mockContext

        val mockAuthController = mockk<AuthenticationController>()
        val accessManager = PickaxeAccessManager(mockAuthController)
        val mockTokens = mockk<Tokens>()
        every {mockAuthController.handle(any(), any())} returns mockTokens
        val accessTokenString = "different access token"
        every {mockTokens.accessToken} returns accessTokenString
        every {mockTokens.idToken} returns "fakeidtoken"

        callbackHandler(accessManager)(mockContext)

        val cookie = cookieSlot.captured
        assertEquals("pickaxe_auth", cookie.name)
        assertTrue(accessManager.authHashes.contains(cookie.value))
    }

    interface FutureVoid : Future<Void>

    private fun createWsContext(): WsContext {
        val openWsContext = mockkClass(WsContext::class)
        every { openWsContext.send(any<String>()) } answers { mockkClass(FutureVoid::class) }
        return openWsContext
    }
}