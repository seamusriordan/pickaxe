describe('entered data persists on refresh', () => {
    it('has data that persists on refresh', () => {
        cy.visit('localhost:8080/pickaxe');
        let gameCellId = '#Sereres-SEA\\@PHI';
        cy.get(gameCellId)
            .click()
            .type("{backspace}{backspace}{backspace}thing")
            .invoke('blur');
        cy.reload();

        cy.get(gameCellId).contains("thing");

        cy.get(gameCellId)
            .click()
            .type("{backspace}{backspace}{backspace}{backspace}{backspace}PHI")
            .invoke('blur');
    })

    it('has data that persists on different week', () => {
        cy.visit('localhost:8080/pickaxe');

        cy.get('#changeWeek-forward').click()

        let gameCellId = '#Sereres-NE\\@TB';
        cy.get(gameCellId)
            .click()
            .type("{backspace}{backspace}brady")
            .invoke('blur');
        cy.reload();

        cy.get('#changeWeek-forward').click()

        cy.get(gameCellId).contains("brady");

        cy.get(gameCellId)
            .click()
            .type("{backspace}{backspace}{backspace}{backspace}{backspace}ne")
            .invoke('blur');
    })
});
