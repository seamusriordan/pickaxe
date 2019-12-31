import graphql.schema.idl.SchemaParser
import graphql.schema.idl.TypeDefinitionRegistry

private fun schema(): String {
    val schema: String =
        """type Query {
|              users: [User]
|          }
|          type User {
                name: String
|          }
|          """.trimMargin()
    return schema
}

fun pickaxeTypeDefinitionRegistry(): TypeDefinitionRegistry {
    val schema: String = schema()
    return generateTypeDefinitionRegistry(schema)
}

fun generateTypeDefinitionRegistry(schema: String): TypeDefinitionRegistry {
    val schemaParser = SchemaParser()
    return schemaParser.parse(schema)
}