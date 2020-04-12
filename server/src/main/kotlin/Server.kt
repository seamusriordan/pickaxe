import graphql.GraphQL
import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.SchemaGenerator
import graphql.schema.idl.SchemaParser
import graphql.schema.idl.TypeDefinitionRegistry
import io.javalin.Javalin
import io.javalin.http.staticfiles.Location
import io.javalin.websocket.WsContext
import services.ServiceRunner
import java.io.File


const val graphqlURI = "/pickaxe/graphql/"
const val staticFilesPath = "html"
const val schemaPath = "src/main/resources/schema.graphql"

fun main(args: Array<String>) {
    ServiceRunner().start()

    val typeDefinitionRegistry = pickaxeTypeDefinitionRegistry(schemaPath)
    val wiring = pickaxeRuntimeWiring()

    val graphQL = generateGraphQLFromRegistryAndWiring(typeDefinitionRegistry, wiring)

    val server = Javalin.create()
    addStaticFileServing(server)
    val wsContexts = ArrayList<WsContext>(0)
    addGraphQLPostServe(server, graphQL, wsContexts)
    addGraphQLOptionServe(server)
    addNotificationWebSocket(server, wsContexts)

    var port = System.getenv("PICKAXE_PORT")
    if(port == null) {
        port = "8080"
    }
    server.start(port.toInt())
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

fun addGraphQLPostServe(server: Javalin, graphQL: GraphQL, wsContexts: ArrayList<WsContext>) {
    server.post(graphqlURI, postHandler(graphQL, wsContexts))
    return
}

fun addGraphQLOptionServe(server: Javalin) {
    server.options(graphqlURI, optionsHandler())
    return
}

fun addNotificationWebSocket(server: Javalin, wsContexts: ArrayList<WsContext>) {
    server.ws("/pickaxe/updateNotification") { ws ->
        ws.onConnect { ctx ->
            wsContexts.add(ctx)
        }

        ws.onClose { ctx ->
            wsContexts.remove(ctx)
        }
    }
}

fun pickaxeTypeDefinitionRegistry(schemaFilePath: String): TypeDefinitionRegistry {
    val schemaParser = SchemaParser()
    val schemaFile = File(schemaFilePath)
    return schemaParser.parse(schemaFile)
}