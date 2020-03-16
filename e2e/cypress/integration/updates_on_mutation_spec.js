describe('Mutation response update',() => {
    it.skip('mutation query causes update', () => {
        cy.visit('localhost:8080/pickaxe');
        cy.get('#Seamus-SEA\\@PHI').contains("SEA");

        let  graphqlRequestBody = "{\"operationName\":\"Mutation\",\"variables\":{\"name\":\"Seamus\",\"week\":0,\"game\":\"SEA@PHI\",\"pick\":\"DERP\"},\"query\":\"mutation Mutation($name: String, $week: Int, $game: String, $pick: String) {\\n  updatePick(name: $name, userPick: {week: $week, game: $game, pick: $pick})\\n}\\n\"}";

        cy.request('POST', 'localhost:8080/pickaxe/graphql', graphqlRequestBody);

        cy.get('#Seamus-SEA\\@PHI').contains("DERP");

        graphqlRequestBody = "{\"operationName\":\"Mutation\",\"variables\":{\"name\":\"Seamus\",\"week\":0,\"game\":\"SEA@PHI\",\"pick\":\"SEA\"},\"query\":\"mutation Mutation($name: String, $week: Int, $game: String, $pick: String) {\\n  updatePick(name: $name, userPick: {week: $week, game: $game, pick: $pick})\\n}\\n\"}";
        cy.request('POST', 'localhost:8080/pickaxe/graphql', graphqlRequestBody);

        cy.get('#Seamus-SEA\\@PHI').contains("SEA");
    });
});
