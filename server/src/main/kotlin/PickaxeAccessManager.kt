import com.auth0.AuthenticationController
import io.javalin.core.security.AccessManager
import io.javalin.core.security.Role
import io.javalin.http.Context
import io.javalin.http.Handler

class PickaxeAccessManager(val authController: AuthenticationController) : AccessManager {
    private val redirectUri: String

    var authHashes = mutableSetOf<String>()
    private val isProduction: Boolean

    init {
        val serverHostName =  getEnvOrDefault("SERVER_HOSTNAME","fake-domain.com")
        isProduction =  System.getProperty("PRODUCTION") != null || System.getenv("PRODUCTION") != null
        redirectUri = "https://$serverHostName$callbackPath"
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