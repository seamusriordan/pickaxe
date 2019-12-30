
describe('Basic server functionality', () => {

    it('can visit our page', () => {
        cy.visit('localhost:8080/pickaxe');
        cy.get('html').contains('Make a pick');
    });

    it('can query to our graphql endpoint', () => {
        const graphqlRequestBody = "{\"operationName\":\"Query\",\"variables\":{},"+
            "\"query\":\"query Query {\\n  users {\\n    name\\n    __typename\\n  }\\n}\\n\"}";

        cy.request('POST', 'localhost:8080/pickaxe/graphql', graphqlRequestBody).then(
            (response) => {
               expect(response.status).to.equal(200)
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
