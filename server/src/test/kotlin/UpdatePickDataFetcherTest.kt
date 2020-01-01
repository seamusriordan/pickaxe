import graphql.schema.DataFetchingEnvironmentImpl
import org.junit.jupiter.api.Test

class UpdatePickDataFetcherTest {
    @Test
    fun derivesFromDataFetcher(){
        val updatePickDataFetcher = UpdatePickDataFetcher();

        val env = DataFetchingEnvironmentImpl.newDataFetchingEnvironment().build()

        updatePickDataFetcher.get(env)
    }
}