import io.javalin.core.security.AccessManager
import io.javalin.http.Context
import io.javalin.http.Handler
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession


class PickaxeAccessManagerTest {
    @Test
    fun `is an AccessManager`() {
        val accessManager = PickaxeAccessManager()

        @Suppress("USELESS_IS_CHECK")
        assertTrue(accessManager is AccessManager)
    }

    @Test
    fun `when production no auth cookie redirects to auth`() {
        val accessManager = PickaxeAccessManager()
        val request = mockk<HttpServletRequest>()
        val response = mockk<HttpServletResponse>()
        val locationSlot = slot<String>()
        every { response.addCookie(any()) } returns Unit
        every { response.addHeader("Set-Cookie", any()) } returns Unit
        every {response.sendRedirect(capture(locationSlot))} returns Unit
        val mockSession = mockk<HttpSession>()
        every {mockSession.setAttribute(any(), any())} returns  Unit
        every { request.getSession(true) } returns mockSession
        every {request.cookies} returns arrayOf<Cookie>()
        val context = Context(request, response)

        accessManager.manage(
            {},
            context,
            mutableSetOf()
        )

        val auth0Domain = "fake-domain.auth0.com"
        val clientId = "fakeClientId"
        val redirectUri = "https://fake-domain.com/callback"
        val authUrl =
            "https://$auth0Domain/authorize\\?redirect_uri=$redirectUri&client_id=$clientId&scope=openid&response_type=code&state="
        val authUriRegex =
            "^$authUrl".toRegex()
        assertTrue(locationSlot.captured.contains(authUriRegex))
    }

    @Test
    fun `when production with known auth cookie executes context`() {
        val knownAuthHash = "authhash"
        val accessManager = PickaxeAccessManager().apply {
            authHashes.add(knownAuthHash)
        }
        val request = mockk<HttpServletRequest>()
        val response = mockk<HttpServletResponse>()

        every {response.setHeader(any(), any())} returns Unit
        every {response.addHeader(any(), any())} returns Unit
        val mockSession = mockk<HttpSession>()
        every {mockSession.setAttribute(any(), any())} returns  Unit
        every { request.getSession(true) } returns mockSession
        every {response.sendRedirect(any())} returns Unit
        val context = Context(request, response)
        every {request.cookies} returns arrayOf(Cookie("pickaxe_auth", knownAuthHash))

        var handledContext: Context? = null
        val handlerSpy = Handler {
            handledContext = it
        }

        accessManager.manage(
            handlerSpy,
            context,
            mutableSetOf()
        )

        assertEquals(handledContext, context)
    }


}
