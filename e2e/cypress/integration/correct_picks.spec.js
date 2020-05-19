describe('correct picks', () => {
    beforeEach(() => {
        cy.visit('localhost:8080/pickaxe').get('#changeWeek-back').click().click()
    })
    it('two correct picks are counted correctly', () => {
        cy.get("#total-0").should('contain', '2');
    });

    it('changing pick makes it not count correctly', () => {
        cy.get("#Seamus-GB\\@CHI")
            .click()
            .type('cargo')
            .invoke('blur')

        cy.get("#total-0").should('contain', '1');

        cy.get("#Seamus-GB\\@CHI")
            .click()
            .type('{backspace}{backspace}{backspace}{backspace}{backspace}')
            .invoke('blur')
    });

    it('changing to different type case will count correctly', () => {
        cy.get("#Seamus-GB\\@CHI")
            .click()
            .type('{backspace}{backspace}hi')
            .invoke('blur')

        cy.get("#total-0").should('contain', '2');

        cy.get("#Seamus-GB\\@CHI")
            .click()
            .type('{backspace}{backspace}HI')
            .invoke('blur')
    });
});