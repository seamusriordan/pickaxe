describe('Default data renders', () => {

    it('starts on week 6', () => {
        cy.visit('localhost:8080/pickaxe');
        cy.get('#change-week--week').contains("Week 6");
    });

    it('has users', () => {
        cy.visit('localhost:8080/pickaxe')
            .get('#change-week--back')
            .click().click();
        cy.get('#name-0').contains("Seamus");
        cy.get('#name-1').contains("Sereres");
        cy.get('#name-2').contains("RNG");
        cy.get('#name-3').contains("Vegas");
    });

    it('has games', () => {
        cy.visit('localhost:8080/pickaxe')
            .get('#change-week--back')
            .click().click();
        cy.get('#game-0').contains("GB@CHI");
        cy.get('#game-1').contains("BUF@NE");
        cy.get('#game-2').contains("SEA@PHI");
    });

});
