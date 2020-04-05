import db.PickaxeDB
import db.UserQuery
import graphql.schema.DataFetcher
import graphql.schema.StaticDataFetcher
import graphql.schema.idl.RuntimeWiring

fun wiringMap(): HashMap<String, HashMap<String, DataFetcher<Any>>> {
    val wiringMap: HashMap<String, HashMap<String, DataFetcher<Any>>> = HashMap()
    val queryFields: HashMap<String, DataFetcher<Any>> = HashMap()
    val mutationFields: HashMap<String, DataFetcher<Any>> = HashMap()

    val usersList = UserQuery(PickaxeDB().getDBConnection()).getActiveUsers()
    queryFields["users"] = StaticDataFetcher(usersList)

    val gamesList = defaultGamesList()
    queryFields["games"] = StaticDataFetcher(gamesList)

    val userPicksList = defaultPicksForUsers(usersList)

    val userPickStore = defaultWeek0PickStore(userPicksList)

    @Suppress("UNCHECKED_CAST")
    queryFields["userPicks"] = UserPickDataQueryFetcher(userPickStore) as DataFetcher<Any>

    @Suppress("UNCHECKED_CAST")
    mutationFields["updatePick"] = UpdatePickMutator(userPickStore) as DataFetcher<Any>

    wiringMap["Query"] = queryFields
    wiringMap["Mutation"] = mutationFields
    return wiringMap
}

fun pickaxeRuntimeWiring(): RuntimeWiring {
    val wiringMap = wiringMap()
    return generateRuntimeWiring(wiringMap)
}

fun generateRuntimeWiring(wiringMap: HashMap<String, HashMap<String, DataFetcher<Any>>>): RuntimeWiring {
    val runtimeBuilder = RuntimeWiring.newRuntimeWiring()

    wiringMap.map { (rootName, fieldMap) ->
        generateWiringForFields(runtimeBuilder, rootName, fieldMap)
    }

    return runtimeBuilder.build()
}

private fun generateWiringForFields(
    runtimeBuilder: RuntimeWiring.Builder,
    rootName: String,
    fieldMap: java.util.HashMap<String, DataFetcher<Any>>
) {
    fieldMap.map { (field, fetcher) ->
        runtimeBuilder.type(rootName) {
            it.dataFetcher(field, fetcher)
        }
    }
}