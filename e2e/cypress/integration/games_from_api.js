describe('games comes from api', () => {
    beforeEach(() => {
        cy.visit('localhost:8080/pickaxe')
        cy.get('#changeWeek-forward').click().click()
    })

    it('gets a set of games from the api', () => {
        cy.get('.game-cell').should("have.length", 14)
    });

    it('completed game is set', () => {
        cy.get('#result-0').should("contain", "NE")
    });

    it('incomplete game is not set', () => {
        cy.get('#result-1').should("be.empty")
    });
})