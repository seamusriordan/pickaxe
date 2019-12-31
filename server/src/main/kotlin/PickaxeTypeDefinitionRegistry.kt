import graphql.schema.idl.SchemaParser
import graphql.schema.idl.TypeDefinitionRegistry

private fun schema(): String {
    return """type Query {
|              users: [User]
|          }
|          type User {
            name: String
|          }
|          """.trimMargin()
}

fun pickaxeTypeDefinitionRegistry(): TypeDefinitionRegistry {
    val schema: String = schema()
    return generateTypeDefinitionRegistry(schema)
}

fun generateTypeDefinitionRegistry(schema: String): TypeDefinitionRegistry {
    val schemaParser = SchemaParser()
    return schemaParser.parse(schema)
}