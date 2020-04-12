describe('Mutation response update', () => {
    const graphqlMutateBody = "{\"operationName\":\"Mutation\",\"variables\":{\"name\":\"Seamus\",\"week\":\"0\",\"game\":\"SEA@PHI\",\"pick\":\"DERP\"},\"query\":\"mutation Mutation($name: String, $week: String, $game: String, $pick: String) {\\n  updatePick(name: $name, userPick: {week: $week, game: $game, pick: $pick})\\n}\\n\"}";
    const graphqlRevertBody = "{\"operationName\":\"Mutation\",\"variables\":{\"name\":\"Seamus\",\"week\":\"0\",\"game\":\"SEA@PHI\",\"pick\":\"SEA\"},\"query\":\"mutation Mutation($name: String, $week: String, $game: String, $pick: String) {\\n  updatePick(name: $name, userPick: {week: $week, game: $game, pick: $pick})\\n}\\n\"}";

    beforeEach(() => {
        cy.visit('localhost:8080/pickaxe').wait(500);
    });

    it('mutation query causes update', () => {
        cy.get('#Seamus-SEA\\@PHI')
            .contains("SEA")
            .request('POST', 'localhost:8080/pickaxe/graphql', graphqlMutateBody)
        cy.get('#Seamus-SEA\\@PHI')
            .contains("DERP", {timeout: 10000})
            .request('POST', 'localhost:8080/pickaxe/graphql', graphqlRevertBody)
        cy.visit('localhost:8080/pickaxe').get('#Seamus-SEA\\@PHI')
            .contains("SEA")
    });

    it('does not overwrite text in updated cells while typing', () => {
        cy.get('#Sereres-SEA\\@PHI')
            .click()
            .type("{backspace}{backspace}{backspace}thing");

        cy.request('POST', 'localhost:8080/pickaxe/graphql', graphqlMutateBody)
            .get('#Seamus-SEA\\@PHI')
            .contains("SEA", {timeout: 10000})
            .get('#Sereres-SEA\\@PHI').contains("thing")
        cy.get('#Sereres-SEA\\@PHI')
            .click()
            .type("{backspace}{backspace}{backspace}{backspace}{backspace}PHI")
            .invoke('blur')
        cy.request('POST', 'localhost:8080/pickaxe/graphql', graphqlRevertBody)
        cy.visit('localhost:8080/pickaxe').get('#Seamus-SEA\\@PHI')
            .contains("SEA")
    });
});