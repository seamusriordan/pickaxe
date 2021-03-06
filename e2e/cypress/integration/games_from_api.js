describe('games comes from api', () => {
    beforeEach(() => {
        cy.visit('localhost:8080/pickaxe')
        cy.get('#change-week--forward').click().click()
    })

    it('gets a set of games from the api', () => {
        cy.get('.grid__cell--game').should("have.length", 14)
    });

    it('completed game is set', () => {
        cy.get('.grid__cell--game')
            .then(findIndexOfGame("NYG@NE"))
            .then(i =>
                cy.get(`#result-${i}`).contains("NE")
            )
    });

    it('incomplete game is not set', () => {
        cy.get('.grid__cell--game')
            .then(findIndexOfGame("CAR@TB"))
            .then(i =>
                cy.get(`#result-${i}`).should("be.empty")
            )
    });

    it('game without game details id is not set', () => {
        cy.get('.grid__cell--game')
            .then(findIndexOfGame("CIN@BAL"))
            .then(i =>
                cy.get(`#result-${i}`).should("be.empty")
            )
    });
})

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