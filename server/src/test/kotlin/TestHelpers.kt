import graphql.schema.DataFetchingEnvironment
import graphql.schema.DataFetchingEnvironmentImpl
import io.mockk.every
import java.sql.ResultSet

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

fun mockNextReturnTimes(mockResultSet: ResultSet, times: Int) {
    val trues = ArrayList<Boolean>(0)
    for (i in 1..times) {
        trues.add(true)
    }

    every {
        mockResultSet.next()
    } returnsMany trues + listOf(false)
}