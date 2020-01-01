import graphql.schema.idl.SchemaParser
import graphql.schema.idl.TypeDefinitionRegistry

private fun schema(): String {
    return """type Query {
    users: [User]
    games: [Game]
}

type User {
    name: String
    picks: [Pick]
    total: Int
}
          
type Game {
    name: String
    result: String
    spread: String
}  
                  
type Pick {
    game: String
    pick: String
}  
          
type Mutation {
    updatePick(name: String, pick: UpdatedPick) : String
}

input UpdatedPick {
    name: String
    game: String
}
""".trimMargin()
}

fun pickaxeTypeDefinitionRegistry(): TypeDefinitionRegistry {
    val schema: String = schema()
    return generateTypeDefinitionRegistry(schema)
}

fun generateTypeDefinitionRegistry(schema: String): TypeDefinitionRegistry {
    val schemaParser = SchemaParser()
    return schemaParser.parse(schema)
}