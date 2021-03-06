import db.*
import graphql.schema.DataFetcher
import graphql.schema.idl.RuntimeWiring

fun wiringMap(): HashMap<String, HashMap<String, DataFetcher<*>>> {
    val wiringMap: HashMap<String, HashMap<String, DataFetcher<*>>> = HashMap()
    val queryFields: HashMap<String, DataFetcher<*>> = HashMap()
    val mutationFields: HashMap<String, DataFetcher<*>> = HashMap()

    val connection = PickaxeDB().getDBConnection()
    queryFields["weeks"] = WeeksQuery(connection)
    queryFields["users"] = UserQuery(connection)
    queryFields["games"] = GamesQuery(connection)
    queryFields["userTotals"] = UserWeekTotalQuery(connection)
    queryFields["currentWeek"] = CurrentWeekQuery(WeeksQuery(connection), GamesQuery(connection))
    queryFields["userPicks"] = UserPickQuery(connection)
    queryFields["leaders"] = LeaderQuery(WeeksQuery(connection), GamesQuery(connection), UserWeekTotalQuery(connection))

    mutationFields["updatePick"] = UpdatePickMutator(connection)

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