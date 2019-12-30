import io.javalin.Javalin
import io.javalin.core.JavalinConfig
import io.javalin.http.staticfiles.Location
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*

internal class ServerTest {
    val mockedServer = mock(Javalin::class.java);

    @BeforeEach
    fun beforeEach() {
        reset(mockedServer)
    }

    @Test
    fun addsStaticFilesWithPath() {;
        mockedServer.config = mock(JavalinConfig::class.java);
        val path = "some_path"

        addStaticFileServing(mockedServer, path);

        verify(mockedServer.config).addStaticFiles(path, Location.EXTERNAL);
    }

    @Test
    fun addsStaticFilesWithDifferentPath() {;
        mockedServer.config = mock(JavalinConfig::class.java);
        val path = "html"

        addStaticFileServing(mockedServer, path);

        verify(mockedServer.config).addStaticFiles(path, Location.EXTERNAL);
    }

    @Test
    fun handlesOptionsMethod() {
        val serverSpy = spy(Javalin.create())

        addGraphQLOptionServe(serverSpy);

        verify(serverSpy).options(eq("/pickaxe/graphql/"), any())
    }
}