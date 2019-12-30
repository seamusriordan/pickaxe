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
import io.javalin.http.staticfiles.Location
import io.javalin.plugin.json.JavalinJson.toJson


fun addStaticFileServing(server: Javalin, path: String) {
    server.config.addStaticFiles(path, Location.EXTERNAL);
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
    val wiring = RuntimeWiring.newRuntimeWiring();

    wiringMap.map { (typeName, fieldMap) ->
        fieldMap.map { (field, fetcher) ->
            wiring.type(typeName) {
                it.dataFetcher(field, fetcher)
            }
        }
    }

    return wiring.build()
}


fun main(args: Array<String>) {
    val server = Javalin.create()

    addStaticFileServing(server, "html")
    addGraphQLOptionServe(server)

    server.start(8080)

    return

    val schema: String =
        """type Query {
|              user: User
|              users: [User]
|          }
|          type User {
                name: String
|          }
|          """.trimMargin()

    val typeDefinitionRegistry = generateTypeDefinitionRegistry(schema)

    var wiringMap: HashMap<String, HashMap<String, DataFetcher<Any>>> =
        HashMap();


    val usersList = ArrayList<UserDTO>()
    usersList.add(UserDTO("Stebe jorbs"))
    usersList.add(UserDTO("Stebe Stebe"))
    usersList.add(UserDTO("Dave Steve"))

    var queryFields: HashMap<String, DataFetcher<Any>> = HashMap();
    queryFields["user"] = StaticDataFetcher(UserDTO("Bob"));
    queryFields["users"] = StaticDataFetcher(usersList);

    wiringMap["Query"] = queryFields

    return



    val schemaGenerator = SchemaGenerator()

    val graphqlSchema = schemaGenerator.makeExecutableSchema(TypeDefinitionRegistry(), runtimeWiring)

    val build = GraphQL.newGraphQL(graphqlSchema).build()

    val mapTypeReference: MapType =
        TypeFactory.defaultInstance().constructMapType(HashMap::class.java, String::class.java, Any::class.java)

    server.post("/pickaxe/graphql/") { ctx ->
        var mapper = jacksonObjectMapper();
        var query = mapper.readValue<HashMap<String, Any>>(ctx.body(), mapTypeReference)

        var executionInput = ExecutionInput.newExecutionInput()
            .query(query["query"] as String).build()

        println(executionInput)
        var executionResult = build.execute(executionInput)
        println(executionResult.getData<String>())

        ctx.header("Access-Control-Allow-Origin", "*")
        ctx.result(toJson(executionResult.toSpecification()))
    }
}