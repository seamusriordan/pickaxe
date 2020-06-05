describe('leaderboard', () => {
    beforeEach(() => {
        cy.visit('localhost:8080/pickaxe').get('#changeWeek-back').click().click()
    })

    it('has starting picks', () => {
        cy.get(".leader-name").first().should('contain', 'Seamus');
        cy.get(".leader-correct-weeks").first().should('contain', '1');
        cy.get(".leader-correct-picks").first().should('contain', '2');
    });

    it('rankings change with picks', () => {

        cy.get("#Seamus-GB\\@CHI")
            .click()
            .type('go')
            .invoke('blur')

        cy.get(".leader-name").first().should('contain', 'Sereres');

        cy.get(":nth-child(4) > .leader-name").should('contain', 'Seamus');
        cy.get(":nth-child(4) > .leader-correct-weeks").should('contain', '0');
        cy.get(":nth-child(4) > .leader-correct-picks").should('contain', '1');

        cy.get("#Seamus-GB\\@CHI")
            .click()
            .type('{backspace}{backspace}')
            .invoke('blur' )
    });
});