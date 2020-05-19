describe('change week', () => {
    it('advances week back on back click', () => {
        cy.visit('localhost:8080/pickaxe')

        cy.get('#changeWeek-back').click()

        cy.get('#changeWeek-week').contains("1")
        cy.get('#game-0').contains("NE@TB")
        cy.get('#spread-0').contains("-14")
    });

    it('goes back when on earlier week and forward click', () => {
        cy.visit('localhost:8080/pickaxe')
        cy.get('#changeWeek-back').click()

        cy.get('#changeWeek-forward').click()

        cy.get('#changeWeek-week').contains("6")
    });
})