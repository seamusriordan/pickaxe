import graphql.schema.DataFetcher
import graphql.schema.idl.RuntimeWiring
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

fun sampleRuntimeWiring() : RuntimeWiring {
    return generateRuntimeWiring(sampleRuntimeWiringMap())
}

fun sampleRuntimeWiringMap() : HashMap<String, HashMap<String, DataFetcher<Any>>> {
    val sampleWiringMap = HashMap<String, HashMap<String, DataFetcher<Any>>>()

    val field: HashMap<String, DataFetcher<Any>> = HashMap()
    val field2: HashMap<String, DataFetcher<Any>> = HashMap()

    field["user"] = DataFetcher<Any> { null }
    field["id"] = DataFetcher<Any> { 44 }
    field2["name"] = DataFetcher<Any> { "Jefphffgh" }

    sampleWiringMap["Query"] = field
    sampleWiringMap["User"] = field2

    return sampleWiringMap
}

fun generateRuntimeWiringForTest(wiringMap: HashMap<String, HashMap<String, DataFetcher<Any>>>): RuntimeWiring {
    val wiring = RuntimeWiring.newRuntimeWiring()

    wiringMap.map { (typeName, fieldMap) ->
        fieldMap.map { (field, fetcher) ->
            wiring.type(typeName) {
                it.dataFetcher(field, fetcher)
            }
        }
    }

    return wiring.build()
}

class PickaxeRuntimeWiringTest {
    private lateinit var sampleWiringMap: HashMap<String, HashMap<String, DataFetcher<Any>>>


    @BeforeEach
    fun beforeEach() {
        sampleWiringMap = HashMap()

        val field: HashMap<String, DataFetcher<Any>> = HashMap()
        val field2: HashMap<String, DataFetcher<Any>> = HashMap()

        field["user"] = DataFetcher<Any> { null }
        field["id"] = DataFetcher<Any> { 44 }
        field2["name"] = DataFetcher<Any> { "Jefphffgh" }

        sampleWiringMap["Query"] = field
        sampleWiringMap["User"] = field2
    }

    @Test
    fun generateRuntimeWiringFromWiringMapWithOneField() {
        val wiringMap: HashMap<String, HashMap<String, DataFetcher<Any>>> = HashMap()

        val field: HashMap<String, DataFetcher<Any>> = HashMap()
        field["aFieldName1"] = DataFetcher<Any> { }

        wiringMap["aType"] = field

        val wiring: RuntimeWiring = generateRuntimeWiring(wiringMap)

        val wiringFieldFetcher = wiring.dataFetchers["aType"]?.get("aFieldName1")

        Assertions.assertSame(wiringFieldFetcher, field["aFieldName1"])
    }

    @Test
    fun generateRuntimeWiringFromWiringMapWithTwoTypesWithVariousFields() {
        val wiring: RuntimeWiring = generateRuntimeWiring(sampleWiringMap)

        Assertions.assertSame(wiring.dataFetchers["Query"]?.get("User"), sampleWiringMap["Query"]?.get("User"))
        Assertions.assertSame(wiring.dataFetchers["User"]?.get("name"), sampleWiringMap["User"]?.get("name"))
    }
}