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

    val schema: String =
        """type Query {
|              user: User
|              users: [User]
|          }
|          
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

    val executionResult = build.execute("{users {name}}")
    println("Query result")
    println(executionResult)
    var goodResults = executionResult.toSpecification()
    println(executionResult.getData<String>())

    app.post("/pickaxe/graphql/") { ctx ->
        var executionInput = ExecutionInput.newExecutionInput().query(ctx.body()).build()
        println(executionInput)
        var executionResult = build.execute(executionInput)
        println(executionResult.getData<String>())
        ctx.result(toJson(goodResults))
    }
}