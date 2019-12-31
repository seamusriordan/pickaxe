import com.fasterxml.jackson.databind.type.MapType
import com.fasterxml.jackson.databind.type.TypeFactory
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import graphql.ExecutionInput
import graphql.GraphQL
import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.SchemaGenerator
import graphql.schema.idl.TypeDefinitionRegistry
import io.javalin.Javalin
import io.javalin.http.Context
import io.javalin.http.staticfiles.Location
import io.javalin.plugin.json.JavalinJson.toJson


fun main(args: Array<String>) {
    val typeDefinitionRegistry = pickaxeTypeDefinitionRegistry()
    val wiring = pickaxeRuntimeWiring()

    val graphQL = generateGraphQLFromRegistryAndWiring(typeDefinitionRegistry, wiring)

    val server = Javalin.create()
    addStaticFileServing(server, "html")
    addGraphQLPostServe(server, graphQL)
    addGraphQLOptionServe(server)

    server.start(8080)
}

fun generateGraphQLFromRegistryAndWiring(registry: TypeDefinitionRegistry, wiring: RuntimeWiring): GraphQL {
    val schemaGenerator = SchemaGenerator()
    val graphqlSchema = schemaGenerator.makeExecutableSchema(registry, wiring)

    return GraphQL.newGraphQL(graphqlSchema).build()
}

fun addStaticFileServing(server: Javalin, path: String) {
    server.config.addStaticFiles(path, Location.EXTERNAL)
    return
}

fun addGraphQLPostServe(server: Javalin, graphQL: GraphQL) {
    server.post("/pickaxe/graphql/") { ctx ->
        val executionInput = extractExecutionInput(ctx)
        val executionResult = graphQL.execute(executionInput)

        ctx.header("Access-Control-Allow-Origin", "*")
        ctx.result(toJson(executionResult.toSpecification()))
    }
    return
}

fun extractExecutionInput(ctx: Context): ExecutionInput {
    val mapTypeReference: MapType =
        TypeFactory.defaultInstance().constructMapType(HashMap::class.java, String::class.java, Any::class.java)

    val mapper = jacksonObjectMapper()
    val query = mapper.readValue<HashMap<String, Any>>(ctx.body(), mapTypeReference)

    return ExecutionInput.newExecutionInput().query(query["query"] as String).build()
}

fun addGraphQLOptionServe(server: Javalin) {
    server.options("/pickaxe/graphql/") { ctx ->
        ctx.header("Access-Control-Allow-Origin", "*")
        ctx.header("Access-Control-Allow-Methods", "OPTIONS, POST, GET")
        ctx.header("Access-Control-Allow-Headers", "*")
        ctx.header("Access-Control-Max-Age", "86400")
    }
    return
}
