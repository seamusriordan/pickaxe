import com.fasterxml.jackson.databind.type.MapType
import com.fasterxml.jackson.databind.type.TypeFactory
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import graphql.ExecutionInput
import graphql.GraphQL
import graphql.execution.ExecutionId
import io.javalin.http.Context
import io.javalin.plugin.json.JavalinJson
import io.javalin.websocket.WsContext

fun postHandler(graphQL: GraphQL, wsContexts: ArrayList<WsContext>): (Context) -> Unit {
    return { ctx ->
        val executionInput = extractExecutionInputFromContext(ctx)
        val executionResult = graphQL.execute(executionInput)

        ctx.header("Access-Control-Allow-Origin", "*")
        ctx.result(JavalinJson.toJson(executionResult.toSpecification()))

        if(executionInput.operationName!!.contentEquals("Mutation")){
            wsContexts.toMutableList().map {
                @Suppress("SENSELESS_COMPARISON")
                if (it.session == null || it.session.isOpen)
                    it.send("Hi")
            }
        }
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

    val executionInput = ExecutionInput.newExecutionInput()
        .query(query["query"] as String)


    if (query["operationName"] != null) executionInput.operationName(query["operationName"] as String)
    @Suppress("UNCHECKED_CAST")
    if (query["variables"] != null) executionInput.variables(query["variables"] as Map<String, Any>)
    @Suppress("UNCHECKED_CAST")
    if (query["context"] != null) executionInput.context(query["context"] as Map<String, Any>)
    if (query["root"] != null) executionInput.root(query["root"])
    if (query["executionId"] != null) executionInput.executionId(query["executionId"] as ExecutionId)

    return executionInput.build()
}