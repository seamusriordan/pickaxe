import graphql.schema.idl.SchemaParser
import graphql.schema.idl.TypeDefinitionRegistry
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.File

class PickaxeTypeDefinitionRegistryTest {
    @Test
    fun generateTypeDefinitionFromRegistryForSimpleSchema() {
        val typeDefReg = pickaxeTypeDefinitionRegistry("simple.graphql")

        Assertions.assertEquals("Query", typeDefReg.types()["Query"]?.name)
        Assertions.assertEquals(
            "TypeName{name='String'}",
            typeDefReg.types()["Query"]?.children?.get(0)?.children?.get(0).toString()
        )
    }


    @Test
    fun generateTypeDefinitionFromRegistryForTwoTypeSchema() {
        val typeDefReg = pickaxeTypeDefinitionRegistry("sample.graphql")

        Assertions.assertEquals("Query", typeDefReg.types()["Query"]?.name)
        Assertions.assertEquals(
            "TypeName{name='User'}",
            typeDefReg.types()["Query"]?.children?.get(0)?.children?.get(0).toString()
        )
    }

}

fun sampleTypeDefinitionRegistry(): TypeDefinitionRegistry {
    val schemaParser = SchemaParser()
    val schemaFile = object {}.javaClass.getResourceAsStream("sample.graphql").bufferedReader()
    return schemaParser.parse(schemaFile)
}
