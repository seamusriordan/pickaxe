import graphql.GraphQL
import graphql.schema.StaticDataFetcher
import graphql.schema.idl.RuntimeWiring
import io.javalin.Javalin
import io.javalin.core.JavalinConfig
import io.javalin.http.staticfiles.Location
import io.javalin.websocket.*
import io.mockk.every
import io.mockk.mockkClass
import io.mockk.spyk
import io.mockk.verify
import io.mockk.mockkStatic
import io.mockk.slot
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.function.Consumer


internal class ServerTest {
    @Test
    fun addsStaticFilesWithPathHtml() {
        val serverSpy = spyk(Javalin.create())
        val configMock = mockkClass(JavalinConfig::class)
        serverSpy.config = configMock
        every { configMock.addStaticFiles(any(), any()) } returns configMock

        addStaticFileServing(serverSpy)

        verify { configMock.addStaticFiles("html", Location.EXTERNAL) }
    }

    @Test
    fun handlesOptionsMethod() {
        val serverSpy = spyk(Javalin.create())
        mockkStatic("HttpHandlersKt")

        addGraphQLOptionServe(serverSpy)

        verify { serverSpy.options("/pickaxe/graphql/", any()) }
        verify(exactly = 1) { optionsHandler() }
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
        val serverSpy = spyk(Javalin.create())
        val graphQLMock = mockkClass(GraphQL::class)

        addGraphQLPostServe(serverSpy, graphQLMock, ArrayList(0))

        verify { serverSpy.post("/pickaxe/graphql/", any()) }
        verify(exactly = 1) { postHandler(graphQLMock) }
    }

    @Test
    fun opensWebsocket() {
        val serverSpy = spyk(Javalin.create())

        addNotificationWebSocket(serverSpy, ArrayList(0))

        verify { serverSpy.ws("/pickaxe/updateNotification", any()) }
    }

    @Test
    fun websocketHandlerAddsContextToPassedListOnConnect() {
        val serverSpy = spyk(Javalin.create())
        val contextList = ArrayList<WsContext>(0)
        addNotificationWebSocket(serverSpy, contextList)

        val wsHandlerConsumer = slot<Consumer<WsHandler>>()
        verify { serverSpy.ws("/pickaxe/updateNotification", capture(wsHandlerConsumer)) }

        val wsHandler = mockkClass(WsHandler::class)
        every { wsHandler.onConnect(any()) } returns Unit
        every { wsHandler.onClose(any()) } returns Unit
        wsHandlerConsumer.captured.accept(wsHandler)

        val connectHandler = slot<WsConnectHandler>()
        verify {wsHandler.onConnect(capture(connectHandler))}
        val mockContext = mockkClass(WsConnectContext::class)

        connectHandler.captured.handleConnect(mockContext)

        assertEquals(mockContext, contextList[0])
    }

    @Test
    fun websocketHandlerRemovesContextToPassedListOnClose() {
        val serverSpy = spyk(Javalin.create())
        val contextList = ArrayList<WsContext>(0)
        val mockContext = mockkClass(WsCloseContext::class)
        contextList.add(mockContext)

        addNotificationWebSocket(serverSpy, contextList)

        val wsHandlerConsumer = slot<Consumer<WsHandler>>()
        verify { serverSpy.ws("/pickaxe/updateNotification", capture(wsHandlerConsumer)) }

        val wsHandler = mockkClass(WsHandler::class)
        every { wsHandler.onConnect(any()) } returns Unit
        every { wsHandler.onClose(any()) } returns Unit
        wsHandlerConsumer.captured.accept(wsHandler)

        val closeHandler = slot<WsCloseHandler>()
        verify {wsHandler.onClose(capture(closeHandler))}

        closeHandler.captured.handleClose(mockContext)

        assertEquals(0, contextList.size)
    }

    private fun generateModifiedWiringWithId(modifiedId: Int): RuntimeWiring {
        val modifiedWiringMap = HashMap(sampleRuntimeWiringMap())
        modifiedWiringMap["Query"]?.put("id", StaticDataFetcher(modifiedId))
        return generateRuntimeWiringForTest(modifiedWiringMap)
    }
}
