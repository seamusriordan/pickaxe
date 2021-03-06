import com.auth0.AuthenticationController
import com.auth0.jwk.JwkProviderBuilder
import graphql.GraphQL
import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.SchemaGenerator
import graphql.schema.idl.SchemaParser
import graphql.schema.idl.TypeDefinitionRegistry
import io.javalin.Javalin
import io.javalin.core.security.SecurityUtil.roles
import io.javalin.http.staticfiles.Location
import io.javalin.websocket.WsContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import services.ServiceRunner

const val graphqlURI = "/pickaxe/graphql/"
const val callbackPath = "/pickaxe/callback"
const val authorizePath = "/pickaxe/authorize"
const val redirectPath = "/pickaxe"
const val failPath = "/"
const val staticFilesPath = "/html"
const val schemaPath = "schema.graphql"

val wsContexts = ArrayList<WsContext?>(0)

val logger: Logger = LoggerFactory.getLogger("dev.revived.pickaxe-server.Server")

fun main(args: Array<String>) {

    ServiceRunner().start()

    val typeDefinitionRegistry = pickaxeTypeDefinitionRegistry(schemaPath)
    val wiring = pickaxeRuntimeWiring()

    val graphQL = generateGraphQLFromRegistryAndWiring(typeDefinitionRegistry, wiring)
    val accessManager = PickaxeAccessManager(generateAuthController())

    val server = Javalin.create {
        it.accessManager(accessManager)
    }
    addStaticFileServing(server)
    addGraphQLPostServe(server, graphQL, wsContexts)
    addCallbackHandler(server, accessManager)
    addAuthorizeHandler(server, accessManager)
    addGraphQLOptionServe(server)
    addNotificationWebSocket(server, wsContexts)

    var port = System.getenv("PICKAXE_PORT")
    if (port == null) {
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
    server.config.addStaticFiles(redirectPath,staticFilesPath, Location.CLASSPATH)
    return
}

fun addGraphQLPostServe(server: Javalin, graphQL: GraphQL, wsContexts: ArrayList<WsContext?>) {
    server.post(graphqlURI, postHandler(graphQL, wsContexts))
    return
}

fun addGraphQLOptionServe(server: Javalin) {
    server.options(graphqlURI, optionsHandler())
    return
}

fun addCallbackHandler(server: Javalin, accessManager: PickaxeAccessManager){
    server.get(callbackPath, callbackHandler(accessManager), roles(PickaxeRoles.ANYONE))
}

fun addAuthorizeHandler(server: Javalin, accessManager: PickaxeAccessManager){
    server.get(authorizePath, authorizeHandler(accessManager), roles(PickaxeRoles.ANYONE))
}

fun addNotificationWebSocket(server: Javalin, wsContexts: ArrayList<WsContext?>) {
    server.ws("/pickaxe/updateNotification") { ws ->
        ws.onConnect { ctx ->
            wsContexts.add(ctx)
        }

        ws.onClose { ctx ->
            wsContexts.removeAll { it == null }
            try {
                wsContexts.remove(ctx)
            } catch (e: TypeCastException) {
                logger.warn("Nulls in websocket contexts")
            }
        }
    }
}

fun pickaxeTypeDefinitionRegistry(schemaFilePath: String): TypeDefinitionRegistry {
    val schemaParser = SchemaParser()
    val schemaFile = object{}.javaClass.getResourceAsStream(schemaFilePath).bufferedReader()
    return schemaParser.parse(schemaFile)
}

fun generateAuthController(): AuthenticationController {
    val auth0Domain = getEnvOrDefault("AUTH0_DOMAIN", "fake-domain.fakeauth.com")
    val clientId =  getEnvOrDefault("AUTH0_CLIENTID", "fakeClientId")
    val clientSecret =  getEnvOrDefault("AUTH0_CLIENTSECRET", "fakeClientSecret")

    val jwkProvider = JwkProviderBuilder(auth0Domain).build()

    return AuthenticationController.newBuilder(
        auth0Domain,
        clientId,
        clientSecret
    ).withJwkProvider(jwkProvider).build()
}