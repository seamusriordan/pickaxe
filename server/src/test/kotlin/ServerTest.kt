import graphql.GraphQL
import graphql.schema.StaticDataFetcher
import graphql.schema.idl.RuntimeWiring
import io.javalin.Javalin
import io.javalin.core.JavalinConfig
import io.javalin.http.staticfiles.Location
import io.javalin.websocket.*
import io.mockk.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.function.Consumer


internal class ServerTest {
    private lateinit var serverSpy: Javalin

    @BeforeEach
    fun setUp() {
        mockkStatic("HttpHandlersKt")
        serverSpy = spyk(Javalin.create { config ->
            run {
                config.logIfServerNotStarted = false
            }
        })
    }

    @Test
    fun addsStaticFilesWithPathHtml() {
        val configMock = mockkClass(JavalinConfig::class)
        serverSpy.config = configMock
        every { configMock.addStaticFiles(any(), any(), any()) } returns configMock

        addStaticFileServing(serverSpy)

        verify { configMock.addStaticFiles("/pickaxe", "/html", Location.CLASSPATH) }
    }

    @Test
    fun handlesOptionsMethod() {
        addGraphQLOptionServe(serverSpy)

        verify { serverSpy.options("/pickaxe/graphql/", any()) }
        verify(exactly = 1) { optionsHandler() }

        unmockkAll()
    }

    @Test
    fun handlesCallback() {
        addCallbackHandler(serverSpy, mockk())
        verify { serverSpy.get("/pickaxe/callback", any(), any()) }
        verify(exactly = 1) { callbackHandler(any()) }

        unmockkAll()
    }

    @Test
    fun handlesAuth() {
        addAuthorizeHandler(serverSpy, mockk())
        verify { serverSpy.get("/pickaxe/authorize", any(), any()) }
        verify(exactly = 1) { authorizeHandler(any()) }

        unmockkAll()
    }

    @Test
    fun makeGraphQLEngineFromTypeDefinitionRegistryAndRuntimeWiring() {
        val registry = sampleTypeDefinitionRegistry()
        val wiring = sampleRuntimeWiring()

        val engine: GraphQL = generateGraphQLFromRegistryAndWiring(registry, wiring)
        val result = engine.execute("query Query {id}")

        assertEquals(44, result.getData<Map<String, Any>>()["id"])
    }

    @Test
    fun makeGraphQLEngineFromTypeDefinitionRegistryAndRuntimeWiringWithChangedIdResult() {
        val registry = sampleTypeDefinitionRegistry()
        val modifiedId = -23902
        val wiring = generateModifiedWiringWithId(modifiedId)

        val engine: GraphQL = generateGraphQLFromRegistryAndWiring(registry, wiring)
        val result = engine.execute("query Query {id}")

        assertEquals(modifiedId, result.getData<Map<String, Any>>()["id"])
    }

    @Test
    fun handlesPostMethod() {
        val graphQLMock = mockkClass(GraphQL::class)

        addGraphQLPostServe(serverSpy, graphQLMock, ArrayList(0))

        verify { serverSpy.post("/pickaxe/graphql/", any()) }
        verify(exactly = 1) { postHandler(graphQLMock, ArrayList(0)) }

        unmockkAll()
    }

    @Test
    fun opensWebsocket() {
        addNotificationWebSocket(serverSpy, ArrayList(0))

        verify { serverSpy.ws("/pickaxe/updateNotification", any()) }
    }

    @Test
    fun websocketHandlerAddsContextToPassedListOnConnect() {
        val contextList = ArrayList<WsContext?>(0)
        addNotificationWebSocket(serverSpy, contextList)

        val wsHandlerConsumer = slot<Consumer<WsHandler>>()
        verify { serverSpy.ws("/pickaxe/updateNotification", capture(wsHandlerConsumer)) }

        val wsHandler = stubWsHandler()
        wsHandlerConsumer.captured.accept(wsHandler)

        val connectHandler = slot<WsConnectHandler>()
        verify { wsHandler.onConnect(capture(connectHandler)) }
        val mockContext = mockkClass(WsConnectContext::class)

        connectHandler.captured.handleConnect(mockContext)

        assertEquals(mockContext, contextList[0])
    }

    @Test
    fun websocketHandlerRemovesContextToPassedListOnClose() {
        val contextList = ArrayList<WsContext?>(0)
        val mockContext = mockkClass(WsCloseContext::class)
        contextList.add(mockContext)

        addNotificationWebSocket(serverSpy, contextList)

        val wsHandlerConsumer = slot<Consumer<WsHandler>>()
        verify { serverSpy.ws("/pickaxe/updateNotification", capture(wsHandlerConsumer)) }

        val wsHandler = stubWsHandler()
        wsHandlerConsumer.captured.accept(wsHandler)

        val closeHandler = slot<WsCloseHandler>()
        verify { wsHandler.onClose(capture(closeHandler)) }

        closeHandler.captured.handleClose(mockContext)

        assertEquals(0, contextList.size)
    }

    @Test
    fun websocketHandlerRemovesNullsListOnClose() {
        val contextList = ArrayList<WsContext?>(0)
        val mockContext = mockkClass(WsCloseContext::class)
        contextList.add(null)
        contextList.add(mockContext)

        addNotificationWebSocket(serverSpy, contextList)

        val wsHandlerConsumer = slot<Consumer<WsHandler>>()
        verify { serverSpy.ws("/pickaxe/updateNotification", capture(wsHandlerConsumer)) }

        val wsHandler = stubWsHandler()
        wsHandlerConsumer.captured.accept(wsHandler)

        val closeHandler = slot<WsCloseHandler>()
        verify { wsHandler.onClose(capture(closeHandler)) }

        closeHandler.captured.handleClose(mockContext)

        assertEquals(0, contextList.size)
    }

    @Test
    fun websocketHandlerRemovesSpecificContextToPassedListOnClose() {
        val contextList = ArrayList<WsContext?>(0)
        val mockContext1 = mockkClass(WsCloseContext::class)
        val mockContext2 = mockkClass(WsCloseContext::class)
        contextList.add(mockContext1)
        contextList.add(mockContext2)

        addNotificationWebSocket(serverSpy, contextList)

        val wsHandlerConsumer = slot<Consumer<WsHandler>>()
        verify { serverSpy.ws("/pickaxe/updateNotification", capture(wsHandlerConsumer)) }

        val wsHandler = stubWsHandler()
        wsHandlerConsumer.captured.accept(wsHandler)

        val closeHandler = slot<WsCloseHandler>()
        verify { wsHandler.onClose(capture(closeHandler)) }

        closeHandler.captured.handleClose(mockContext1)

        assertEquals(1, contextList.size)
        assertEquals(mockContext2, contextList[0])
    }

    @Test
    fun addGraphQLPostServePassesWsContextToHandler() {
        val graphQLMock = mockkClass(GraphQL::class)

        val listWithContext = ArrayList<WsContext?>(0)
        listWithContext.add(mockkClass(WsContext::class))

        addGraphQLPostServe(serverSpy, graphQLMock, listWithContext)

        verify { serverSpy.post("/pickaxe/graphql/", any()) }
        verify(exactly = 1) { postHandler(graphQLMock, listWithContext) }

        unmockkAll()
    }

    private fun stubWsHandler(): WsHandler {
        val wsHandler = mockkClass(WsHandler::class)
        every { wsHandler.onConnect(any()) } returns Unit
        every { wsHandler.onClose(any()) } returns Unit
        return wsHandler
    }

    private fun generateModifiedWiringWithId(modifiedId: Int): RuntimeWiring {
        val modifiedWiringMap = HashMap(sampleRuntimeWiringMap())
        modifiedWiringMap["Query"]?.put("id", StaticDataFetcher(modifiedId))
        return generateRuntimeWiringForTest(modifiedWiringMap)
    }
}