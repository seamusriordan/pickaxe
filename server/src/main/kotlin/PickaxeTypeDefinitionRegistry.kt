import graphql.schema.idl.SchemaParser
import graphql.schema.idl.TypeDefinitionRegistry
import java.io.File

fun pickaxeTypeDefinitionRegistry(schemaFilePath: String): TypeDefinitionRegistry {
    val schemaParser = SchemaParser()
    val schemaFile = File(schemaFilePath)
    return schemaParser.parse(schemaFile)
}