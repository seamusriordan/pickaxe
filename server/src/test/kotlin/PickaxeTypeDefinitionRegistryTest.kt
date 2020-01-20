import graphql.schema.idl.SchemaParser
import graphql.schema.idl.TypeDefinitionRegistry
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.File

private const val sampleSchema = ""

class PickaxeTypeDefinitionRegistryTest {
    @Test
    fun generateTypeDefinitionFromRegistryForSimpleSchema() {
        val typeDefReg = pickaxeTypeDefinitionRegistry("src/test/resources/simple.graphql")

        Assertions.assertEquals("Query", typeDefReg.types()["Query"]?.name)
        Assertions.assertEquals(
            "TypeName{name='String'}",
            typeDefReg.types()["Query"]?.children?.get(0)?.children?.get(0).toString()
        )
    }


    @Test
    fun generateTypeDefinitionFromRegistryForTwoTypeSchema() {
        val typeDefReg = pickaxeTypeDefinitionRegistry("src/test/resources/sample.graphql")

        Assertions.assertEquals("Query", typeDefReg.types()["Query"]?.name)
        Assertions.assertEquals(
            "TypeName{name='User'}",
            typeDefReg.types()["Query"]?.children?.get(0)?.children?.get(0).toString()
        )
    }

}

fun sampleTypeDefinitionRegistry(): TypeDefinitionRegistry {
    val schemaParser = SchemaParser()
    val schemaFile = File("src/test/resources/sample.graphql")
    return schemaParser.parse(schemaFile)
}
