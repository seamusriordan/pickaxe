describe('Default data renders', () => {

    it('has users', () => {
        cy.visit('localhost:8080/pickaxe');
        cy.get('#name-0').contains("Seamus");
        cy.get('#name-1').contains("Sereres");
        cy.get('#name-2').contains("RNG");
        cy.get('#name-3').contains("Vegas");
    });

    it('has games', () => {
        cy.visit('localhost:8080/pickaxe');
        cy.get('#game-0').contains("GB@CHI");
        cy.get('#game-1').contains("SEA@PHI");
        cy.get('#game-2').contains("BUF@NE");
    });

});
