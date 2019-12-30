import graphql.schema.DataFetcher
import graphql.schema.idl.RuntimeWiring
import io.javalin.Javalin
import io.javalin.core.JavalinConfig
import io.javalin.http.staticfiles.Location
import org.junit.jupiter.api.Assertions.*
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
    fun addsStaticFilesWithArbitraryPath() {;
        mockedServer.config = mock(JavalinConfig::class.java);
        val path = "some_path"

        addStaticFileServing(mockedServer, path);

        verify(mockedServer.config).addStaticFiles(path, Location.EXTERNAL);
    }

    @Test
    fun addsStaticFilesWithPathHtml() {;
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

    @Test
    fun generateTypeDefinitionFromRegistryForSimpleSchema() {
        val simpleSchema = "type Query {username: String}";

        val typeDefReg = generateTypeDefinitionRegistry(simpleSchema)

        assertEquals("Query", typeDefReg.types()["Query"]?.name)
        assertEquals("TypeName{name='String'}",
            typeDefReg.types()["Query"]?.children?.get(0)?.children?.get(0).toString()
        )
    }

    @Test
    fun generateTypeDefinitionFromRegistryForTwoTypeSchema() {
        val simpleSchema = "type Query {user: User} type User { name: String }";

        val typeDefReg = generateTypeDefinitionRegistry(simpleSchema)

        assertEquals("Query", typeDefReg.types()["Query"]?.name)
        assertEquals("TypeName{name='User'}",
            typeDefReg.types()["Query"]?.children?.get(0)?.children?.get(0).toString()
        )
    }

    @Test
    fun generateRuntimeWiringFromWiringMapWithOneField() {
        var wiringMap: HashMap<String, HashMap<String, DataFetcher<Any>>> =
            HashMap();

        var field: HashMap<String, DataFetcher<Any>> = HashMap();
        field["aFieldName1"] = DataFetcher<Any> { };

        wiringMap["aType"] = field;

        var wiring: RuntimeWiring = generateRuntimeWiring(wiringMap)

        val wiringFieldFetcher = wiring.dataFetchers["aType"]?.get("aFieldName1")

        assertSame(wiringFieldFetcher, field["aFieldName1"])
    }

    @Test
    fun generateRuntimeWiringFromWiringMapWithTwoTypesWithOneField() {
        val wiringMap: HashMap<String, HashMap<String, DataFetcher<Any>>> =
            HashMap();

        val field: HashMap<String, DataFetcher<Any>> = HashMap();
        field["username"] = DataFetcher<Any> { };
        field["something"] = DataFetcher<Any> { };

        wiringMap["Query"] = field;


        val wiring: RuntimeWiring = generateRuntimeWiring(wiringMap)

        assertSame(wiring.dataFetchers["Query"]?.get("username"), field["username"])
        assertSame(wiring.dataFetchers["Query"]?.get("something"), field["something"])
    }
}