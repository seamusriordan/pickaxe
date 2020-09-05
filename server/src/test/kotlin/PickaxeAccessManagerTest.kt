import com.auth0.AuthenticationController
import io.javalin.core.security.AccessManager
import io.javalin.http.Context
import io.javalin.http.Handler
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession


class PickaxeAccessManagerTest {
    private val auth0Domain = "fake-domain.fakeauth.com"
    private val clientId = "fakeClientId"
    private val redirectUri = "http://localhost:8080/pickaxe/callback"
    private var authUrl = ""
    private var authUriRegex = Regex("")

    private lateinit var authController: AuthenticationController

    @BeforeEach
    fun setUp() {
        authUrl =
            "https://$auth0Domain/authorize\\?redirect_uri=$redirectUri&client_id=$clientId&scope=openid&response_type=code&state="
        authUriRegex = "^$authUrl".toRegex()

        authController = AuthenticationController.newBuilder(
            auth0Domain, clientId, "fakeSecreet").build()
    }

    @AfterEach
    internal fun tearDown() {
        System.clearProperty("PRODUCTION")
    }

    @Test
    fun `is an AccessManager`() {
        val accessManager = PickaxeAccessManager(authController)

        @Suppress("USELESS_IS_CHECK")
        assertTrue(accessManager is AccessManager)
    }

    @Test
    fun `when production no auth cookie 401s`() {
        System.setProperty("PRODUCTION", "true")
        val accessManager = PickaxeAccessManager(authController)
        val request = mockk<HttpServletRequest>()
        val response = mockk<HttpServletResponse>()
        every { response.addCookie(any()) } returns Unit
        every { response.addHeader("Set-Cookie", any()) } returns Unit
        every { response.status = 401 } returns Unit

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

        verify { response.status = 401  }
    }


    @Test
    fun `when not production no auth cookie handles context`() {
        System.clearProperty("PRODUCTION")
        val accessManager = PickaxeAccessManager(authController)
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
        verify(exactly = 0) { response.sendRedirect(any()) }
    }

    @Test
    fun `when role is anyone handles context`() {
        val accessManager = PickaxeAccessManager(authController)
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

        var handledContext: Context? = null
        val handlerSpy = Handler {
            handledContext = it
        }

        accessManager.manage(
            handlerSpy,
            context,
            mutableSetOf(PickaxeRoles.ANYONE)
        )

        assertEquals(handledContext, context)
        verify(exactly = 0) { response.sendRedirect(any()) }
    }


    @Test
    fun `when production with known auth cookie executes context`() {
        System.setProperty("PRODUCTION", "true")
        val knownAuthHash = "authhash"
        val accessManager = PickaxeAccessManager(authController).apply {
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
        verify(exactly = 0) { response.sendRedirect(any()) }
    }

    @Test
    fun `when production with unknown auth cookie 401s`() {
        System.setProperty("PRODUCTION", "true")
        val unknownAuthHash = "authhash"
        val accessManager = PickaxeAccessManager(authController)
        val request = mockk<HttpServletRequest>()
        val response = mockk<HttpServletResponse>()

        every {response.setHeader(any(), any())} returns Unit
        every {response.addHeader(any(), any())} returns Unit
        val mockSession = mockk<HttpSession>()
        every {mockSession.setAttribute(any(), any())} returns  Unit
        every { request.getSession(true) } returns mockSession
        every {response.status = 401 } returns Unit
        val context = Context(request, response)
        every {request.cookies} returns arrayOf(Cookie("pickaxe_auth", unknownAuthHash))

        accessManager.manage(
            {},
            context,
            mutableSetOf()
        )

        verify {response.status = 401 }
    }


}
