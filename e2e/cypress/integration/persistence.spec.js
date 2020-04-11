describe('entered data persists on refresh', () => {
    it('has data that persists on refresh', () => {
        cy.visit('localhost:8080/pickaxe');
        cy.get('#Sereres-SEA\\@PHI')
            .click()
            .type("{backspace}{backspace}{backspace}thing")
            .invoke('blur');
        cy.reload();

        cy.get('#Sereres-SEA\\@PHI').contains("thing");

        cy.get('#Sereres-SEA\\@PHI')
            .click()
            .type("{backspace}{backspace}{backspace}{backspace}{backspace}PHI")
            .invoke('blur');
    })
});
