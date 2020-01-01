import graphql.schema.idl.SchemaParser
import graphql.schema.idl.TypeDefinitionRegistry

private fun schema(): String {
    return """type Query {
    users: [User]
    userPicks(week: Int): [UserPicks]
    games(week: Int): [Game]
}

type User {
    name: String
}

type UserPicks {
    user: User
    picks: [Pick]
    total: Int
}
          
type Game {
    name: String
    week: Int
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
    week: Int
    game: String
    pick: String
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