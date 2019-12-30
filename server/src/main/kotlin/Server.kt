import com.fasterxml.jackson.databind.type.MapType
import com.fasterxml.jackson.databind.type.TypeFactory
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import graphql.ExecutionInput
import graphql.GraphQL
import graphql.schema.StaticDataFetcher
import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.SchemaGenerator
import graphql.schema.idl.SchemaParser
import io.javalin.Javalin
import io.javalin.http.staticfiles.Location
import io.javalin.plugin.json.JavalinJson.toJson

fun main(args: Array<String>) {
    val app = Javalin.create().start(8080)
    app.config.addStaticFiles("html", Location.EXTERNAL)

    app.options("/pickaxe/graphql/") { ctx ->
        ctx.header("Access-Control-Allow-Origin", "*")
        ctx.header("Access-Control-Allow-Methods", "OPTIONS, POST, GET")
        ctx.header("Access-Control-Allow-Headers", "*")
        ctx.header("Access-Control-Max-Age", "86400")
    }

    val schema: String =
        """type Query {
|              user: User
|              users: [User]
|          }
|          type User {
                name: String
|          }
|          """.trimMargin()
    val schemaParser = SchemaParser()
    val typeDefinitionRegistry = schemaParser.parse(schema)

    val usersList = ArrayList<UserDTO>()
        usersList.add(UserDTO("Stebe jorbs"))
        usersList.add(UserDTO("Stebe Stebe"))
        usersList.add(UserDTO("Dave Steve"))


    val runtimeWiring = RuntimeWiring.newRuntimeWiring()
        .type("Query") {
            it.dataFetcher("user", StaticDataFetcher(UserDTO("Bob")))
            it.dataFetcher("users", StaticDataFetcher(usersList))
        }.build()

    val schemaGenerator = SchemaGenerator()

    val graphqlSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring)

    val build = GraphQL.newGraphQL(graphqlSchema).build()

    val mapTypeReference: MapType = TypeFactory.defaultInstance().constructMapType(HashMap::class.java, String::class.java, Any::class.java)

    app.post("/pickaxe/graphql/") { ctx ->
        var mapper = jacksonObjectMapper();
        var query = mapper.readValue<HashMap<String, Any>>(ctx.body(), mapTypeReference)

        var executionInput = ExecutionInput.newExecutionInput()
            .query( query["query"] as String).build()

        println(executionInput)
        var executionResult = build.execute(executionInput)
        println(executionResult.getData<String>())

        ctx.header("Access-Control-Allow-Origin", "*")
        ctx.result(toJson(executionResult.toSpecification()))
    }
}