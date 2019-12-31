import graphql.ExecutionInput
import graphql.ExecutionResult
import graphql.GraphQL
import graphql.schema.DataFetcher
import graphql.schema.StaticDataFetcher
import graphql.schema.idl.RuntimeWiring
import io.javalin.Javalin
import io.javalin.core.JavalinConfig
import io.javalin.http.Handler
import io.javalin.http.staticfiles.Location
import io.javalin.plugin.json.JavalinJackson.toJson
import io.mockk.every
import io.mockk.mockkClass
import io.mockk.slot
import io.mockk.spyk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*

internal class ServerTest {
    private val mockedServer = mock(Javalin::class.java)

    private lateinit var sampleWiringMap: HashMap<String, HashMap<String, DataFetcher<Any>>>
    private val sampleSchema = "type Query {user: User, id: Int} type User { name: String }"
    private val queryBody = "{\"operationName\":\"Query\",\"variables\":{},\"query\":\"query Query {\\n  id\\n}\\n\"}"


    @BeforeEach
    fun beforeEach() {
        reset(mockedServer)

        sampleWiringMap = HashMap()

        val field: HashMap<String, DataFetcher<Any>> = HashMap()
        val field2: HashMap<String, DataFetcher<Any>> = HashMap()

        field["user"] = DataFetcher<Any> { null }
        field["id"] = DataFetcher<Any> { 44 }
        field2["name"] = DataFetcher<Any> { "Jefphffgh" }

        sampleWiringMap["Query"] = field
        sampleWiringMap["User"] = field2
    }

    @Test
    fun addsStaticFilesWithArbitraryPath() {
        mockedServer.config = mock(JavalinConfig::class.java)
        val path = "some_path"

        addStaticFileServing(mockedServer, path)

        verify(mockedServer.config).addStaticFiles(path, Location.EXTERNAL)
    }

    @Test
    fun addsStaticFilesWithPathHtml() {
        mockedServer.config = mock(JavalinConfig::class.java)
        val path = "html"

        addStaticFileServing(mockedServer, path)

        verify(mockedServer.config).addStaticFiles(path, Location.EXTERNAL)
    }

    @Test
    fun handlesOptionsMethod() {
        val serverSpy = spyk(Javalin.create())

        var contextHandler = slot<Handler>();

        addGraphQLOptionServe(serverSpy)


        io.mockk.verify { serverSpy.options("/pickaxe/graphql/", capture(contextHandler)) };

        val mockContext = mockkClass(io.javalin.http.Context::class)

        every { mockContext.header(any(), any()) } returns mockContext

        contextHandler.captured.handle(mockContext)

        io.mockk.verify { mockContext.header("Access-Control-Allow-Origin", "*") }
        io.mockk.verify { mockContext.header("Access-Control-Allow-Methods", "OPTIONS, POST, GET") }
        io.mockk.verify { mockContext.header("Access-Control-Allow-Headers", "*") }
        io.mockk.verify { mockContext.header("Access-Control-Max-Age", "86400") }
    }

    @Test
    fun generateTypeDefinitionFromRegistryForSimpleSchema() {
        val simpleSchema = "type Query {username: String}"

        val typeDefReg = generateTypeDefinitionRegistry(simpleSchema)

        assertEquals("Query", typeDefReg.types()["Query"]?.name)
        assertEquals(
            "TypeName{name='String'}",
            typeDefReg.types()["Query"]?.children?.get(0)?.children?.get(0).toString()
        )
    }

    @Test
    fun generateTypeDefinitionFromRegistryForTwoTypeSchema() {
        val typeDefReg = generateTypeDefinitionRegistry(sampleSchema)

        assertEquals("Query", typeDefReg.types()["Query"]?.name)
        assertEquals(
            "TypeName{name='User'}",
            typeDefReg.types()["Query"]?.children?.get(0)?.children?.get(0).toString()
        )
    }

    @Test
    fun generateRuntimeWiringFromWiringMapWithOneField() {
        val wiringMap: HashMap<String, HashMap<String, DataFetcher<Any>>> = HashMap()

        val field: HashMap<String, DataFetcher<Any>> = HashMap()
        field["aFieldName1"] = DataFetcher<Any> { }

        wiringMap["aType"] = field

        val wiring: RuntimeWiring = generateRuntimeWiring(wiringMap)

        val wiringFieldFetcher = wiring.dataFetchers["aType"]?.get("aFieldName1")

        assertSame(wiringFieldFetcher, field["aFieldName1"])
    }

    @Test
    fun generateRuntimeWiringFromWiringMapWithTwoTypesWithVariousFields() {
        val wiring: RuntimeWiring = generateRuntimeWiring(sampleWiringMap)

        assertSame(wiring.dataFetchers["Query"]?.get("User"), sampleWiringMap["Query"]?.get("User"))
        assertSame(wiring.dataFetchers["User"]?.get("name"), sampleWiringMap["User"]?.get("name"))
    }

    @Test
    fun makeGraphQLEngineFromTypeDefinitionRegistryAndRuntimeWiring() {
        val registry = generateTypeDefinitionRegistry(sampleSchema)
        val wiring = generateRuntimeWiring(sampleWiringMap)
        val engine: GraphQL = generateGraphQLFromRegistryAndWiring(registry, wiring)

        val result = engine.execute("query Query {id}")

        assertEquals(44, result.getData<Map<String, Any>>()["id"])
    }

    @Test
    fun makeGraphQLEngineFromTypeDefinitionRegistryAndRuntimeWiringWithChangedIdResult() {
        val registry = generateTypeDefinitionRegistry(sampleSchema)

        val newWiringMap = HashMap(sampleWiringMap)
        newWiringMap["Query"]?.put("id", StaticDataFetcher(-23902))

        val wiring = generateRuntimeWiring(sampleWiringMap)
        val engine: GraphQL = generateGraphQLFromRegistryAndWiring(registry, wiring)

        val result = engine.execute("query Query {id}")

        assertEquals(-23902, result.getData<Map<String, Any>>()["id"])
    }

    @Test
    fun extractExecutionInputFromPostBodyForSimpleQuery() {
        val mockContext = mockkClass(io.javalin.http.Context::class)

        every { mockContext.body() } returns queryBody;

        val expectedResult = 44
        val registry = generateTypeDefinitionRegistry(sampleSchema)
        val wiring = generateRuntimeWiring(sampleWiringMap)

        val engine: GraphQL = generateGraphQLFromRegistryAndWiring(registry, wiring)
        val input = extractExecutionInput(mockContext)
        val result = engine.execute(input)

        assertEquals(expectedResult, result.getData<Map<String, Int>>()["id"])
    }


    @Test
    fun handlesPostMethod() {
        val specificationMap = HashMap<String, String>();

        specificationMap["Derp derp"] = "A derpty do"

        val serverSpy = spyk(Javalin.create())
        val mockExecutionResult = mockkClass(ExecutionResult::class)
        every {mockExecutionResult.toSpecification()} returns specificationMap as Map<String, Any>?;

        val graphqlMock = mockkClass(GraphQL::class)
        every {graphqlMock.execute(any<ExecutionInput>())} returns mockExecutionResult

        val mockContext = mockkClass(io.javalin.http.Context::class)
        every { mockContext.body() } returns queryBody
        every { mockContext.header(any(), any()) } returns mockContext
        every { mockContext.result(any<String>()) } returns mockContext

        var contextHandler = slot<Handler>();


        addGraphQLPostServe(serverSpy, graphqlMock)

        io.mockk.verify { serverSpy.post("/pickaxe/graphql/", capture(contextHandler)) };

        contextHandler.captured.handle(mockContext)

        val expectedInput = extractExecutionInput(mockContext);
        val capturedInput = slot<ExecutionInput>();

        io.mockk.verify { mockContext.body() }
        io.mockk.verify { graphqlMock.execute(capture(capturedInput)) }


        assertEquals(expectedInput.query, capturedInput.captured.query)
        assertEquals(expectedInput.operationName, capturedInput.captured.operationName)

        io.mockk.verify { mockContext.header("Access-Control-Allow-Origin", "*") }
        io.mockk.verify { mockContext.result(toJson(specificationMap)) }
    }
}