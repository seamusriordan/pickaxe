import graphql.schema.DataFetchingEnvironment
import graphql.schema.DataFetchingEnvironmentImpl

fun getEnvForWeek(week: String): DataFetchingEnvironment {
    val arguments = HashMap<String, Any>().apply {
        set("week", week)
    }
    return setupEnvForArguments(arguments)
}

private fun setupEnvForArguments(arguments: HashMap<String, Any>): DataFetchingEnvironment {
    return DataFetchingEnvironmentImpl
        .newDataFetchingEnvironment()
        .arguments(arguments)
        .build()
}


