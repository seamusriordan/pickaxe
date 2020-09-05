import com.auth0.AuthenticationController
import io.javalin.core.security.AccessManager
import io.javalin.core.security.Role
import io.javalin.http.Context
import io.javalin.http.Handler

class PickaxeAccessManager(val authController: AuthenticationController) : AccessManager {
    private val redirectUri: String

    var authHashes = mutableSetOf<String>()
    private val isProduction: Boolean =
        System.getProperty("PRODUCTION") != null || System.getenv("PRODUCTION") != null
    private val serverHostName: String = getEnvOrDefault("SERVER_HOSTNAME","localhost")
    private val serverPort: String = getEnvOrDefault("SERVER_PORT","8080")
    val serverBaseUri: String

    init {
        if(serverPort != "443") {
            serverBaseUri = "http://$serverHostName:$serverPort"
        } else {
            serverBaseUri = "https://$serverHostName"
        }
        redirectUri = "$serverBaseUri$callbackPath"

    }

    override fun manage(handler: Handler, ctx: Context, permittedRoles: MutableSet<Role>) {
        if(authHashes.contains(ctx.cookie("pickaxe_auth")) or !isProduction){
            handler.handle(ctx)
        } else {
            val authorizeUrl = authController.buildAuthorizeUrl(ctx.req, ctx.res, redirectUri).build()
            ctx.res.sendRedirect(authorizeUrl)
        }
    }
}