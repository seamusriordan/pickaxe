import graphql.GraphQL
import graphql.schema.StaticDataFetcher
import graphql.schema.idl.RuntimeWiring
import io.javalin.Javalin
import io.javalin.core.JavalinConfig
import io.javalin.http.staticfiles.Location
import io.mockk.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test


internal class ServerTest {
    @Test
    fun addsStaticFilesWithPathHtml() {
        val serverSpy = spyk(Javalin.create())
        val configMock = mockkClass(JavalinConfig::class)
        serverSpy.config = configMock
        every {configMock.addStaticFiles(any(), any()) } returns configMock

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
        mockkStatic("HttpHandlersKt")

        addGraphQLPostServe(serverSpy, graphQLMock)

        verify { serverSpy.post("/pickaxe/graphql/", any()) }
        verify(exactly = 1) { postHandler(graphQLMock) }
    }

    private fun generateModifiedWiringWithId(modifiedId: Int): RuntimeWiring {
        val modifiedWiringMap = HashMap(sampleRuntimeWiringMap())
        modifiedWiringMap["Query"]?.put("id", StaticDataFetcher(modifiedId))
        return generateRuntimeWiringForTest(modifiedWiringMap)
    }
}
