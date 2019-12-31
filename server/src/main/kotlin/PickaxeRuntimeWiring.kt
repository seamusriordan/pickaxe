import graphql.schema.DataFetcher
import graphql.schema.StaticDataFetcher
import graphql.schema.idl.RuntimeWiring

fun wiringMap(): HashMap<String, HashMap<String, DataFetcher<Any>>> {
    val wiringMap: HashMap<String, HashMap<String, DataFetcher<Any>>> = HashMap()
    val queryFields: HashMap<String, DataFetcher<Any>> = HashMap()

    val usersList = ArrayList<UserDTO>()
    usersList.add(UserDTO("Seamus"))
    usersList.add(UserDTO("Sereres"))
    usersList.add(UserDTO("RNG"))
    usersList.add(UserDTO("Vegas"))

    queryFields["users"] = StaticDataFetcher(usersList)
    wiringMap["Query"] = queryFields
    return wiringMap
}

fun pickaxeRuntimeWiring(): RuntimeWiring {
    val wiringMap = wiringMap();
    return generateRuntimeWiring(wiringMap)
}

fun generateRuntimeWiring(wiringMap: HashMap<String, HashMap<String, DataFetcher<Any>>>): RuntimeWiring {
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