describe('entered data persists on refresh', () => {
    xit('has data that persists on refresh', () => {
        cy.visit('localhost:8080/pickaxe');
        cy.get('#Sereres-SEA\\@PHI').trigger('blur', {target: {textContent: "thing"}});
        cy.reload();
        cy.get('#Sereres-SEA\\@PHI').contains("thing");
    })
});
