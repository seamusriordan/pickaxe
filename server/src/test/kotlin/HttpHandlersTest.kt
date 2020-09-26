import com.auth0.AuthenticationController
import com.auth0.Tokens
import com.nhaarman.mockitokotlin2.*
import graphql.GraphQL
import graphql.schema.idl.SchemaGenerator
import io.javalin.http.Context
import io.javalin.http.RedirectResponse
import io.javalin.websocket.WsContext
import org.apache.commons.codec.digest.DigestUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentCaptor
import org.mockito.internal.verification.Times
import java.util.concurrent.Future
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession


class HttpHandlersTest {
    private val idQueryBody = "{\"operationName\":\"Query\",\"variables\":{},\"query\":\"query Query {\\n  id\\n}\\n\"}"
    private val nullOpBody = "{\"operationName\":null,\"variables\":{},\"query\":\"query Query {\\n  id\\n}\\n\"}"
    private val userQueryBody =
        "{\"operationName\":\"Query\",\"variables\":{},\"query\":\"query Query {\\n  user {\\n\\n  name}\\n}\\n\"}"
    private val mutationQueryBody0 =
        "{\"operationName\":\"Mutation\",\"variables\":{ \"id\": 0 },\"query\":\"mutation Mutation(\$id: Int){\\n  mutate(id: \$id) }\\n\"}"
    private val mutationQueryBody7 =
        "{\"operationName\":\"Mutation\",\"variables\":{ \"id\": 7 },\"query\":\"mutation Mutation(\$id: Int){\\n  mutate(id: \$id) }\\n\"}"

    lateinit var sampleEngine: GraphQL
    @BeforeEach
    fun setup() {
        sampleEngine = sampleGraphQL()
    }

    @Test
    fun extractExecutionInputFromPostBodyForIdQuery() {
        val mockContext = mock<Context> {
            on { body() } doReturn idQueryBody
        }

        val input = extractExecutionInputFromContext(mockContext)
        val result = sampleEngine.execute(input)

        val expectedResult = 44
        assertEquals(expectedResult, result.getData<Map<String, Int>>()["id"])
    }

    @Test
    fun extractExecutionInputFromPostBodyForUserWithNameQuery() {
        val mockContext = mock<Context> {
            on { body() } doReturn userQueryBody
        }

        val input = extractExecutionInputFromContext(mockContext)
        val result = sampleEngine.execute(input)

        val expectedResult = "JImm"
        assertEquals(expectedResult, result.getData<Map<String, Map<String, String>>>()["user"]?.get("name"))
    }

    @Test
    fun extractExecutionInputVariablesFromMutationBodyForId7() {
        val mockContext = mock<Context> {
            on { body() } doReturn mutationQueryBody7
        }

        val input = extractExecutionInputFromContext(mockContext)

        assertEquals("{id=7}", input.variables.toString())
    }

    @Test
    fun extractExecutionInputVariablesFromMutationBodyForId0() {
        val mockContext = mock<Context> {
            on { body() } doReturn mutationQueryBody0
        }

        val input = extractExecutionInputFromContext(mockContext)

        assertEquals("{id=0}", input.variables.toString())
    }

    @Test
    fun extractExecutionInputOperationFromMutationQuery() {
        val mockContext = mock<Context> {
            on { body() } doReturn mutationQueryBody7
        }

        val input = extractExecutionInputFromContext(mockContext)

        assertEquals("Mutation", input.operationName)
    }


    @Test
    fun optionsHandlerSetsCorrectHeaders() {
        val mockContext = mock<Context> {
            on { header(any(), any()) } doReturn mock
        }

        optionsHandler()(mockContext)

        verify(mockContext).header("Access-Control-Allow-Origin", "*")
        verify(mockContext).header("Access-Control-Allow-Methods", "OPTIONS, POST, GET")
        verify(mockContext).header("Access-Control-Allow-Headers", "*")
        verify(mockContext).header("Access-Control-Max-Age", "86400")
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
        val resultSlot = ArgumentCaptor.forClass(String::class.java)

        postHandler(sampleGraphQL(), ArrayList(0))(mockContext)

        verify(mockContext).header("Access-Control-Allow-Origin", "*")
        verify(mockContext).result(capture<String>(resultSlot))
        val expectedResult = "{\"data\":{\"id\":44}}"
        assertEquals(expectedResult, resultSlot.value)
    }

    @Test
    fun `null Operation Name Doesnt Crash Server`() {
        val mockContext = createMockNullOpContext()

        postHandler(sampleGraphQL(), ArrayList(0))(mockContext)

        verify(mockContext).body()
    }

    @Test
    fun postHandlerWithOneOpenContextSendsMessage() {
        System.setProperty("skip_ws_session_null_check", "true")
        val mockContext = createMockMutationContext()

        val wsContexts = ArrayList<WsContext?>(0)
        val openWsContext = createWsContext()
        wsContexts.add(openWsContext)

        postHandler(sampleGraphQL(), wsContexts)(mockContext)

        verify(openWsContext).send(any<String>())
    }

    private fun createMockNullOpContext(): Context {
        return mock {
            on { body() } doReturn nullOpBody
            on { header(any(), any()) } doReturn mock
            on { result(any<String>()) } doReturn mock
        }
    }

    private fun createMockQueryContext(): Context {
        return mock {
            on { body() } doReturn idQueryBody
            on { header(any(), any()) } doReturn mock
            on { result(any<String>()) } doReturn mock
        }
    }

    private fun createMockMutationContext(): Context {
        return mock {
            on { body() } doReturn mutationQueryBody0
            on { header(any(), any()) } doReturn mock
            on { result(any<String>()) } doReturn mock
        }
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

        postHandler(sampleEngine, wsContexts)(mockContext)

        verify(openWsContext1).send(any<String>())
        verify(openWsContext2).send(any<String>())
    }

    @Test
    fun postHandlerForQueryWithTwoOpenContextSendsNoMessage() {
        val mockContext = createMockQueryContext()

        val wsContexts = ArrayList<WsContext?>(0)
        val openWsContext1 = createWsContext()
        val openWsContext2 = createWsContext()
        wsContexts.add(openWsContext1)
        wsContexts.add(openWsContext2)

        postHandler(sampleEngine, wsContexts)(mockContext)

        verify(openWsContext1, Times(0)).send(any<String>())
        verify(openWsContext1, Times(0)).send(any<String>())
    }

    @Test
    fun `callback adds known hash for token`() {
        val mockContext = mock<Context> {
            on { cookie(any<Cookie>()) } doReturn mock
        }
        val mockAuthController = mock<AuthenticationController>()
        val accessManager = PickaxeAccessManager(mockAuthController)
        val mockTokens = mock<Tokens>()
        whenever(mockAuthController.handle(any(), eq(null))).thenReturn(mockTokens)
        val accessTokenString = "fakeaccesstoken"
        whenever(mockTokens.accessToken).thenReturn(accessTokenString)
        whenever(mockTokens.idToken).thenReturn("fakeidtoken")

        callbackHandler(accessManager)(mockContext)

        assertEquals(1, accessManager.authHashes.size)
        assertEquals(DigestUtils.md5Hex(accessTokenString), accessManager.authHashes.first())
    }

    @Test
    fun `callback added hash varies with token`() {
        val mockContext = mock<Context> {
            on { cookie(any<Cookie>()) } doReturn mock
        }

        val mockAuthController = mock<AuthenticationController>()
        val accessManager = PickaxeAccessManager(mockAuthController)
        val mockTokens = mock<Tokens>()
        whenever(mockAuthController.handle(any(), eq(null))).thenReturn(mockTokens)
        val accessTokenString = "different access token"
        whenever(mockTokens.accessToken).thenReturn(accessTokenString)
        whenever(mockTokens.idToken).thenReturn("fakeidtoken")

        callbackHandler(accessManager)(mockContext)

        assertEquals(1, accessManager.authHashes.size)
        assertEquals(DigestUtils.md5Hex(accessTokenString), accessManager.authHashes.first())
    }

    @Test
    fun `callback success adds cookie and redirects to main page`() {
        val cookieCaptor: ArgumentCaptor<Cookie> = ArgumentCaptor.forClass(Cookie::class.java)
        val redirectCaptor: ArgumentCaptor<String> = ArgumentCaptor.forClass(String::class.java)

        val mockContext = mock<Context> {
            on { cookie(capture(cookieCaptor)) } doReturn mock
        }
        doNothing().whenever(mockContext).redirect(capture(redirectCaptor), eq( HttpServletResponse.SC_MOVED_TEMPORARILY))


        val mockAuthController = mock<AuthenticationController>()
        val accessManager = PickaxeAccessManager(mockAuthController)
        val mockTokens = mock<Tokens>()
        whenever(mockAuthController.handle(any(), eq(null))).thenReturn(mockTokens)
        val accessTokenString = "different access token"
        whenever(mockTokens.accessToken).thenReturn(accessTokenString)
        whenever(mockTokens.idToken).thenReturn("fakeidtoken")

        callbackHandler(accessManager)(mockContext)

        val cookie = cookieCaptor.value
        assertEquals("pickaxe_auth", cookie?.name)
        assertTrue(accessManager.authHashes.contains(cookie?.value))
        assertEquals("http://localhost:8080/pickaxe", redirectCaptor.value)
    }


    @Test
    fun `callback ignore requestUrl from request in requestUrl in auth handler`() {
        val requestSlot = ArgumentCaptor.forClass(HttpServletRequest::class.java)

        val mockContext = mock<Context> {
            on { cookie(any<Cookie>()) } doReturn mock
        }

        val mockAuthController = mock<AuthenticationController>()
        val accessManager = PickaxeAccessManager(mockAuthController)
        val mockTokens = mock<Tokens>()
        whenever(mockAuthController.handle(capture(requestSlot), eq(null))).thenReturn(mockTokens)
        val accessTokenString = "different access token"
        whenever(mockTokens.accessToken).thenReturn(accessTokenString)
        whenever(mockTokens.idToken).thenReturn("fakeidtoken")

        callbackHandler(accessManager)(mockContext)

        assertEquals("http://localhost:8080/pickaxe/callback", requestSlot.value.requestURL.toString())
    }

    @Test
    fun `auth redirects`() {
        val auth0Domain = "fake-domain.fakeauth.com"
        val clientId = "fakeClientId"
        val redirectUri = "http://localhost:8080/pickaxe/callback"
        val authUrl =
            "https://$auth0Domain/authorize\\?redirect_uri=$redirectUri&client_id=$clientId&scope=openid&response_type=code&state="
        val authUriRegex = "^$authUrl".toRegex()
        val authController = AuthenticationController.newBuilder(
            auth0Domain, clientId, "fakeSecreet").build()

        val redirectSlot = ArgumentCaptor.forClass(String::class.java)
        val req = mock<HttpServletRequest>()
        val res = mock<HttpServletResponse>()

        doNothing().whenever(res).setHeader(eq("Location"), capture(redirectSlot))
        val mockSession = mock<HttpSession>()

        whenever(req.getSession(eq(true))).thenReturn(mockSession)



        val context = Context(req, res)
        val accessManager = PickaxeAccessManager(authController)

        assertThrows<RedirectResponse> {
            authorizeHandler(accessManager)(context)
        }

        assertTrue(redirectSlot.value.contains(authUriRegex))
        verify(res).status = 302
    }

    interface FutureVoid : Future<Void>

    private fun createWsContext(): WsContext {
        return mock {
            on { send(any<String>())} doReturn mock<FutureVoid>()
        }
    }
}