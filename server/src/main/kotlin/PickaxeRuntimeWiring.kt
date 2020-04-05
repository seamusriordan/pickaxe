import db.PickaxeDB
import db.UserQuery
import graphql.schema.DataFetcher
import graphql.schema.StaticDataFetcher
import graphql.schema.idl.RuntimeWiring

fun wiringMap(): HashMap<String, HashMap<String, DataFetcher<*>>> {
    val wiringMap: HashMap<String, HashMap<String, DataFetcher<*>>> = HashMap()
    val queryFields: HashMap<String, DataFetcher<*>> = HashMap()
    val mutationFields: HashMap<String, DataFetcher<*>> = HashMap()

    val connection = PickaxeDB().getDBConnection()
    queryFields["users"] = UserQuery(connection)

    val gamesList = defaultGamesList()
    queryFields["games"] = StaticDataFetcher(gamesList)

    val userPicksList = defaultPicksForUsers(defaultUserList())

    val userPickStore = defaultWeek0PickStore(userPicksList)

    queryFields["userPicks"] = UserPickDataQueryFetcher(userPickStore)

    mutationFields["updatePick"] = UpdatePickMutator(userPickStore)

    wiringMap["Query"] = queryFields
    wiringMap["Mutation"] = mutationFields
    return wiringMap
}

fun pickaxeRuntimeWiring(): RuntimeWiring {
    val wiringMap = wiringMap()
    return generateRuntimeWiring(wiringMap)
}

fun generateRuntimeWiring(wiringMap: HashMap<String, HashMap<String, DataFetcher<*>>>): RuntimeWiring {
    val runtimeBuilder = RuntimeWiring.newRuntimeWiring()

    wiringMap.map { (rootName, fieldMap) ->
        generateWiringForFields(runtimeBuilder, rootName, fieldMap)
    }

    return runtimeBuilder.build()
}

private fun generateWiringForFields(
    runtimeBuilder: RuntimeWiring.Builder,
    rootName: String,
    fieldMap: java.util.HashMap<String, DataFetcher<*>>
) {
    fieldMap.map { (field, fetcher) ->
        runtimeBuilder.type(rootName) {
            it.dataFetcher(field, fetcher)
        }
    }
}