import com.fasterxml.jackson.databind.type.MapType
import com.fasterxml.jackson.databind.type.TypeFactory
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import graphql.ExecutionInput
import graphql.GraphQL
import graphql.schema.DataFetcher
import graphql.schema.StaticDataFetcher
import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.SchemaGenerator
import graphql.schema.idl.SchemaParser
import graphql.schema.idl.TypeDefinitionRegistry
import io.javalin.Javalin
import io.javalin.http.Context
import io.javalin.http.staticfiles.Location
import io.javalin.plugin.json.JavalinJson.toJson


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

fun addGraphQLOptionServe(server: Javalin) {
    server.options("/pickaxe/graphql/") { ctx ->
        ctx.header("Access-Control-Allow-Origin", "*")
        ctx.header("Access-Control-Allow-Methods", "OPTIONS, POST, GET")
        ctx.header("Access-Control-Allow-Headers", "*")
        ctx.header("Access-Control-Max-Age", "86400")
    }
    return
}

fun generateTypeDefinitionRegistry(schema: String): TypeDefinitionRegistry {
    val schemaParser = SchemaParser()
    return schemaParser.parse(schema)
}

fun generateRuntimeWiring(wiringMap: Map<String, Map<String, DataFetcher<Any>>>): RuntimeWiring {
    val wiring = RuntimeWiring.newRuntimeWiring()

    wiringMap.map { (typeName, fieldMap) ->
        fieldMap.map { (field, fetcher) ->
            wiring.type(typeName) {
                it.dataFetcher(field, fetcher)
            }
        }
    }

    return wiring.build()
}

fun generateGraphQL(registry: TypeDefinitionRegistry, wiring: RuntimeWiring): GraphQL {
    val schemaGenerator = SchemaGenerator()
    val graphqlSchema = schemaGenerator.makeExecutableSchema(registry, wiring)

    return GraphQL.newGraphQL(graphqlSchema).build()
}

fun extractExecutionInput(ctx: Context): ExecutionInput {
    val mapTypeReference: MapType =
        TypeFactory.defaultInstance().constructMapType(HashMap::class.java, String::class.java, Any::class.java)

    val mapper = jacksonObjectMapper()
    val query = mapper.readValue<HashMap<String, Any>>(ctx.body(), mapTypeReference)

    return ExecutionInput.newExecutionInput().query(query["query"] as String).build()
}

fun main(args: Array<String>) {
    val schema: String =
        """type Query {
|              users: [User]
|          }
|          type User {
                name: String
|          }
|          """.trimMargin()

    val typeDefinitionRegistry = generateTypeDefinitionRegistry(schema)

    val wiringMap: HashMap<String, HashMap<String, DataFetcher<Any>>> = HashMap()
    val queryFields: HashMap<String, DataFetcher<Any>> = HashMap()

    val usersList = ArrayList<UserDTO>()
    usersList.add(UserDTO("Seamus"))
    usersList.add(UserDTO("Sereres"))
    usersList.add(UserDTO("RNG"))
    usersList.add(UserDTO("Vegas"))

    queryFields["users"] = StaticDataFetcher(usersList)
    wiringMap["Query"] = queryFields

    val graphQL = generateGraphQLFromSchemaAndWiringMap(typeDefinitionRegistry, wiringMap)

    val server = Javalin.create()
    addStaticFileServing(server, "html")
    addGraphQLPostServe(server, graphQL)
    addGraphQLOptionServe(server)

    server.start(8080)
}

private fun generateGraphQLFromSchemaAndWiringMap(
        typeDefinitionRegistry: TypeDefinitionRegistry,
        wiringMap: HashMap<String, HashMap<String, DataFetcher<Any>>>
    ): GraphQL {


    val runtimeWiring = generateRuntimeWiring(wiringMap)
    val graphQL = generateGraphQL(typeDefinitionRegistry, runtimeWiring)
    return graphQL
}