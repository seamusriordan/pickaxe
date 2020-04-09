package db

import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import java.sql.Connection

class CurrentWeekQuery(connection: Connection) : DataFetcher<String> {
    override fun get(environment: DataFetchingEnvironment?): String {
        return "0"
    }
}
