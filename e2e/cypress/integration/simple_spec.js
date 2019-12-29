describe('Basic server functionality', () => {
    it('can visit our page', () => {
        cy.visit('localhost:8080/pickaxe');
        cy.get('html').contains('Make a pick');
    })
});
