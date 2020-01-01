import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment

class UpdatePickDataFetcher : DataFetcher<Unit> {
    override fun get(env: DataFetchingEnvironment){
        println("Get from env")
        println(env.queryDirectives)
        println(env.variables)
    }
}