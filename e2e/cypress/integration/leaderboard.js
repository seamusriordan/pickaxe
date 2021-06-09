describe('leaderboard', () => {
    beforeEach(() => {
        cy.visit('localhost:8080/pickaxe').get('#change-week--back').click().click()
    })

    it('has starting picks', () => {
        cy.get(".leader-element-name").first().should('contain', 'Seamus');
        cy.get(".leader-correct-weeks").first().should('contain', '1');
        cy.get(".leader-correct-picks").first().should('contain', '2');
    });

    it('rankings change with picks', () => {

        cy.get("#Seamus-GB\\@CHI")
            .click()
            .type('go')
            .invoke('blur')

            .get(".leader-element-name", {timeout: 20000}).first().should('contain', 'Sereres')

            .get(":nth-child(7) > .leader-element-name").should('contain', 'Seamus')
            .get(":nth-child(7) > .leader-correct-weeks").should('contain', '0')
            .get(":nth-child(7) > .leader-correct-picks").should('contain', '1')

            .get("#Seamus-GB\\@CHI")
            .click()
            .type('{backspace}{backspace}')
            .invoke('blur')
    });
});