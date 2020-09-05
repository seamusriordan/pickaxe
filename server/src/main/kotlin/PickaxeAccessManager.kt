import com.auth0.AuthenticationController
import com.auth0.jwk.JwkProviderBuilder
import io.javalin.core.security.AccessManager
import io.javalin.core.security.Role
import io.javalin.http.Context
import io.javalin.http.Handler

class PickaxeAccessManager : AccessManager {
    private var authController: AuthenticationController
    var redirectUri: String

    var authHashes = mutableSetOf<String>()

    init {
        val auth0Domain = getEnvOrDefault("AUTH0_DOMAIN","fake-domain.auth0.com")
        val clientId =  getEnvOrDefault("AUTH0_CLIENTID","fakeClientId")
        val clientSecret =  getEnvOrDefault("AUTH0_CLIENTSECRET","fakeClientSecret")
        val serverHostName =  getEnvOrDefault("REACT_APP_GRAPHQL_SERVER","fake-domain.com")

        redirectUri = "https://$serverHostName/callback"

        val jwkProvider = JwkProviderBuilder(auth0Domain).build()

        authController = AuthenticationController.newBuilder(
            auth0Domain,
            clientId,
            clientSecret
        ).withJwkProvider(jwkProvider).build()
    }

    override fun manage(handler: Handler, ctx: Context, permittedRoles: MutableSet<Role>) {
        if(authHashes.contains(ctx.cookie("pickaxe_auth"))){
            handler.handle(ctx)
        } else {
            val authorizeUrl = authController.buildAuthorizeUrl(ctx.req, ctx.res, redirectUri).build()
            ctx.res.sendRedirect(authorizeUrl)
        }
    }
}