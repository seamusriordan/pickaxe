describe('entered data persists on refresh', () => {
    it('has data that persists on refresh', () => {
        cy.visit('localhost:8080/pickaxe');
        cy.get('#Sereres-SEA\\@PHI').type("thing");
        cy.reload();
        cy.get('#Sereres-SEA\\@PHI').contains("thing");
    })
});
