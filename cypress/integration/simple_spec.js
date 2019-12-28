describe('simple test', () => {
    it('just passes', () => {
        expect(true).to.equal(true)
    })

    it('can visit our page', () => {
        cy.visit('localhost:8080')
        cy.get('html').contains('Make a pick')
    })
});
