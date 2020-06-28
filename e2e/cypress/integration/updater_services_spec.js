describe('updater services', () => {
    it('RNG has picked a game', () => {
        cy.visit('localhost:8080/pickaxe')
            .get('#RNG-DET\\@GB')
            .contains(/GB|DET/);
    });

    it('Vegas has picked a game', () => {
        cy.visit('localhost:8080/pickaxe')
            .get('#Vegas-DET\\@GB')
            .contains('GB');
    });
    it('Vegas has updated spread', () => {
        cy.visit('localhost:8080/pickaxe')
            .get('.game-cell')
            .then(findIndexOfGame("DET@GB"))
            .then(i =>
                cy.get(`#spread-${i}`).contains("-6.5")
            )
    });
});

function findIndexOfGame(game) {
    return (elements) => {
        for (let i = 0; i < elements.length; i++) {
            if (elements.get(i).innerText === game) {
                return i;
            }
        }
        return -1
    };
}