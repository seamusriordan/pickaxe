import graphql.GraphQL
import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.SchemaGenerator
import graphql.schema.idl.TypeDefinitionRegistry
import io.javalin.Javalin
import io.javalin.http.staticfiles.Location

const val graphqlURI = "/pickaxe/graphql/"
const val staticFilesPath = "html"

fun main(args: Array<String>) {
    val typeDefinitionRegistry = pickaxeTypeDefinitionRegistry()
    val wiring = pickaxeRuntimeWiring()

    val graphQL = generateGraphQLFromRegistryAndWiring(typeDefinitionRegistry, wiring)

    val server = Javalin.create()
    addStaticFileServing(server)
    addGraphQLPostServe(server, graphQL)
    addGraphQLOptionServe(server)

    server.start(8080)
}

fun generateGraphQLFromRegistryAndWiring(registry: TypeDefinitionRegistry, wiring: RuntimeWiring): GraphQL {
    val schemaGenerator = SchemaGenerator()
    val graphqlSchema = schemaGenerator.makeExecutableSchema(registry, wiring)

    return GraphQL.newGraphQL(graphqlSchema).build()
}

fun addStaticFileServing(server: Javalin) {
    server.config.addStaticFiles(staticFilesPath, Location.EXTERNAL)
    return
}

fun addGraphQLPostServe(server: Javalin, graphQL: GraphQL) {
    server.post(graphqlURI, postHandler(graphQL))
    return
}

fun addGraphQLOptionServe(server: Javalin) {
    server.options(graphqlURI, optionsHandler())
    return
}

