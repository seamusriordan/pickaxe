import graphql.schema.idl.SchemaParser
import graphql.schema.idl.TypeDefinitionRegistry
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

private const val sampleSchema = "type Query {user: User, id: Int} type User { name: String }"

fun sampleTypeDefinitionRegistry(): TypeDefinitionRegistry {
    val schemaParser = SchemaParser()
    return schemaParser.parse(sampleSchema)
}

class PickaxeTypeDefinitionRegistryTest {


    @Test
    fun generateTypeDefinitionFromRegistryForSimpleSchema() {
        val simpleSchema = "type Query {username: String}"

        val typeDefReg = generateTypeDefinitionRegistry(simpleSchema)

        Assertions.assertEquals("Query", typeDefReg.types()["Query"]?.name)
        Assertions.assertEquals(
            "TypeName{name='String'}",
            typeDefReg.types()["Query"]?.children?.get(0)?.children?.get(0).toString()
        )
    }


    @Test
    fun generateTypeDefinitionFromRegistryForTwoTypeSchema() {
        val typeDefReg = generateTypeDefinitionRegistry(sampleSchema)

        Assertions.assertEquals("Query", typeDefReg.types()["Query"]?.name)
        Assertions.assertEquals(
            "TypeName{name='User'}",
            typeDefReg.types()["Query"]?.children?.get(0)?.children?.get(0).toString()
        )
    }

}