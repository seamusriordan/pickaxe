import graphql.GraphQL
import graphql.schema.idl.SchemaGenerator
import io.javalin.http.Context
import io.mockk.every
import io.mockk.mockkClass
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class HttpHandlersTest {
    private val idQueryBody = "{\"operationName\":\"Query\",\"variables\":{},\"query\":\"query Query {\\n  id\\n}\\n\"}"
    private val userQueryBody = "{\"operationName\":\"Query\",\"variables\":{},\"query\":\"query Query {\\n  user {\\n\\n  name}\\n}\\n\"}"
    private val mutationQueryBody7 = "{\"operationName\":\"Mutation\",\"variables\":{ \"id\": 7 },\"query\":\"mutation Mutation {\\n  mutate(\$id: Int) }\\n\"}"
    private val mutationQueryBody0 = "{\"operationName\":\"Mutation\",\"variables\":{ \"id\": 0 },\"query\":\"mutation Mutation {\\n  mutate(\$id: Int) }\\n\"}"

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
    fun optionsHandlerSetsCorrectHeaders(){
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
        val mockContext = mockkClass(Context::class)
        every { mockContext.body() } returns idQueryBody
        every { mockContext.header(any(), any()) } returns mockContext
        every { mockContext.result(any<String>()) } returns mockContext
        val resultSlot = slot<String>()

        postHandler(sampleGraphQL())(mockContext)

        verify { mockContext.header("Access-Control-Allow-Origin", "*") }
        verify { mockContext.result(capture(resultSlot)) }
        val expectedResult = "{\"data\":{\"id\":44}}"
        assertEquals(expectedResult, resultSlot.captured)
    }
}