import graphql.schema.DataFetcher
import graphql.schema.idl.RuntimeWiring
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class PickaxeRuntimeWiringTest {
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
        val wiring: RuntimeWiring = generateRuntimeWiring(sampleRuntimeWiringMap())

        Assertions.assertSame(wiring.dataFetchers["Query"]?.get("User"), sampleRuntimeWiringMap()["Query"]?.get("User"))
        Assertions.assertSame(wiring.dataFetchers["User"]?.get("name"), sampleRuntimeWiringMap()["User"]?.get("name"))
    }
}

fun sampleRuntimeWiring() : RuntimeWiring {
    return generateRuntimeWiring(sampleRuntimeWiringMap())
}

fun sampleRuntimeWiringMap() : HashMap<String, HashMap<String, DataFetcher<Any>>> {
    val sampleWiringMap = HashMap<String, HashMap<String, DataFetcher<Any>>>()

    val field: HashMap<String, DataFetcher<Any>> = HashMap()

    field["user"] = DataFetcher<Any> { UserDTO("JImm") }
    field["id"] = DataFetcher<Any> { 44 }

    sampleWiringMap["Query"] = field

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
