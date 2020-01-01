describe('server endpoint is up', () => {

    it('can visit our page', () => {
        cy.visit('localhost:8080/pickaxe');
        cy.get('html').contains('Make a pick');
    });

});

describe('GraphQL server responds', () => {

    it('can query to our graphql endpoint', () => {
        const graphqlRequestBody = "{\"operationName\":\"Query\",\"variables\":{}," +
            "\"query\":\"query Query {\\n  users {\\n    name\\n    __typename\\n  }\\n}\\n\"}";

        cy.request('POST', 'localhost:8080/pickaxe/graphql', graphqlRequestBody).then(
            (response) => {
                expect(response.status).to.equal(200)
            }
        )
    });

    it('can query response follows predetermined input', () => {
        const graphqlRequestBody = "{\"operationName\":\"Query\",\"variables\":{}," +
            "\"query\":\"query Query {\\n  users {\\n    name\\n    __typename\\n  }\\n}\\n\"}";

        cy.request('POST', 'localhost:8080/pickaxe/graphql', graphqlRequestBody).then(
            (response) => {
                expect(response.body).to.equal("{\"data\":{\"users\":[{\"name\":\"Seamus\",\"__typename\":\"User\"},{\"name\":\"Sereres\",\"__typename\":\"User\"},{\"name\":\"RNG\",\"__typename\":\"User\"},{\"name\":\"Vegas\",\"__typename\":\"User\"}]}}")
                expect(JSON.parse(response.body).errors).undefined
            }
        )
    });

    it('mutation query response has no errors', () => {
        const graphqlRequestBody = "{\"operationName\":\"Mutation\",\"variables\":{\"name\":\"Seamus\",\"pick\":{\"game\":\"SEA@PHI\",\"pick\":\"SEA\"}},\"query\":\"mutation Mutation($name: String, $pick: UpdatedPick) {\\n  updatePick(name: $name, pick: $pick)\\n}\\n\"}";

        cy.request('POST', 'localhost:8080/pickaxe/graphql', graphqlRequestBody).then(
            (response) => {
                expect(response.status).to.equal(200)
                expect(JSON.parse(response.body).errors).undefined
            }
        )
    });

    it('has access control header *', () => {
        const graphqlRequestBody = "{\"operationName\":\"Query\",\"variables\":{}," +
            "\"query\":\"query Query {\\n  users {\\n    name\\n    __typename\\n  }\\n}\\n\"}";

        cy.request('POST', 'localhost:8080/pickaxe/graphql', graphqlRequestBody).then(
            (response) => {
                expect(response.headers['access-control-allow-origin']).to.equal("*")
            }
        )
    });

    it('graphql endpoint responds to OPTIONS with origin *', () => {
        cy.request('OPTIONS', 'localhost:8080/pickaxe/graphql').then(
            (response) => {
                expect(response.status).to.equal(200);
                expect(response.headers['access-control-allow-origin']).to.equal("*")
            }
        )
    });
});
