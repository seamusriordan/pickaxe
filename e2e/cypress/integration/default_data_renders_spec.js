describe('Default data renders', () => {

    it('has users', () => {
        cy.visit('localhost:8080/pickaxe');
        cy.get('.name-cell').contains("Seamus");
        cy.get('.name-cell').contains("Sereres");
        cy.get('.name-cell').contains("Vegas");
        cy.get('.name-cell').contains("RNG");
    });

    it('has games', () => {
        cy.visit('localhost:8080/pickaxe');
        cy.get('.game-cell').contains("GB@CHI");
        cy.get('.game-cell').contains("BUF@NE");
        cy.get('.game-cell').contains("SEA@PHI");
    });

});
