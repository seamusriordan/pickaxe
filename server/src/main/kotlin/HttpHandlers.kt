import com.fasterxml.jackson.databind.type.MapType
import com.fasterxml.jackson.databind.type.TypeFactory
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import graphql.ExecutionInput
import graphql.GraphQL
import io.javalin.http.Context
import io.javalin.plugin.json.JavalinJson

fun postHandler(graphQL: GraphQL): (Context) -> Unit {
    return { ctx ->
        val executionInput = extractExecutionInputFromContext(ctx)
        val executionResult = graphQL.execute(executionInput)

        ctx.header("Access-Control-Allow-Origin", "*")
        ctx.result(JavalinJson.toJson(executionResult.toSpecification()))
    }
}

fun optionsHandler(): (Context) -> Unit {
    return { ctx ->
        ctx.header("Access-Control-Allow-Origin", "*")
        ctx.header("Access-Control-Allow-Methods", "OPTIONS, POST, GET")
        ctx.header("Access-Control-Allow-Headers", "*")
        ctx.header("Access-Control-Max-Age", "86400")
    }
}

fun extractExecutionInputFromContext(ctx: Context): ExecutionInput {
    val mapTypeReference: MapType =
        TypeFactory.defaultInstance()
            .constructMapType(HashMap::class.java, String::class.java, Any::class.java)

    val mapper = jacksonObjectMapper()
    val query = mapper.readValue<HashMap<String, Any>>(ctx.body(), mapTypeReference)

    println(query["query"])
    println(query["variables"])

    return ExecutionInput.newExecutionInput()
        .query(query["query"] as String)
        .variables(query["variables"] as Map<String, Any>)
        .operationName(query["operationName"] as String).build()
}