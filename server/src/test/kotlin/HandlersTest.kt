import graphql.GraphQL
import graphql.schema.idl.SchemaGenerator
import io.javalin.http.Context
import io.mockk.every
import io.mockk.mockkClass
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class HandlersTest {
    private val queryBody = "{\"operationName\":\"Query\",\"variables\":{},\"query\":\"query Query {\\n  id\\n}\\n\"}"

    @Test
    fun extractExecutionInputFromPostBodyForSimpleQuery() {
        val mockContext = mockkClass(Context::class)
        every { mockContext.body() } returns queryBody
        val engine: GraphQL = sampleGraphQL()

        val input = extractExecutionInput(mockContext)
        val result = engine.execute(input)

        val expectedResult = 44
       assertEquals(expectedResult, result.getData<Map<String, Int>>()["id"])
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
        every { mockContext.body() } returns queryBody
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