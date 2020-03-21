describe('Mutation response update', () => {
    it('mutation query causes update', () => {
        let graphqlRequestBody = "{\"operationName\":\"Mutation\",\"variables\":{\"name\":\"Seamus\",\"week\":0,\"game\":\"SEA@PHI\",\"pick\":\"DERP\"},\"query\":\"mutation Mutation($name: String, $week: Int, $game: String, $pick: String) {\\n  updatePick(name: $name, userPick: {week: $week, game: $game, pick: $pick})\\n}\\n\"}";

        cy.visit('localhost:8080/pickaxe')
            .get('#Seamus-SEA\\@PHI')
            .contains("SEA");

        cy.wait(1000).request('POST', 'localhost:8080/pickaxe/graphql', graphqlRequestBody)
            .get('#Seamus-SEA\\@PHI')
            .contains("DERP");

        graphqlRequestBody = "{\"operationName\":\"Mutation\",\"variables\":{\"name\":\"Seamus\",\"week\":0,\"game\":\"SEA@PHI\",\"pick\":\"SEA\"},\"query\":\"mutation Mutation($name: String, $week: Int, $game: String, $pick: String) {\\n  updatePick(name: $name, userPick: {week: $week, game: $game, pick: $pick})\\n}\\n\"}";
        cy.wait(1000).request('POST', 'localhost:8080/pickaxe/graphql', graphqlRequestBody)
            .get('#Seamus-SEA\\@PHI')
            .contains("SEA");
    });
});
