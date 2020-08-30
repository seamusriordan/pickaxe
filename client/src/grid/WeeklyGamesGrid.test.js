import {mockQueryData} from "../testUtilities/MockQueryData";
import {act, create} from "react-test-renderer";
import {assertAllUserPicksMatchCellText, findByClassName, getPickByGame} from "../testUtilities/Helpers";
import PickCell from "./PickCell";
import React from "react";
import WeeklyGamesGrid from "./WeeklyGamesGrid";
import {useMutation} from '@apollo/react-hooks';
import gql from "graphql-tag";

jest.mock('@apollo/react-hooks');


describe('WeeklyGamesGrid', () => {
    let grid, renderer;

    beforeEach(() => {
        jest.resetAllMocks();
        useMutation.mockReturnValue([() => {
        }]);

        renderer = create(<WeeklyGamesGrid
            currentWeek="0"
            users={mockQueryData.users}
            games={mockQueryData.games}
            userPicks={mockQueryData.userPicks}
            totals={mockQueryData.userTotals}/>);
        grid = renderer.root;
    });

    it('calls useMutation', () => {
        expect(useMutation).toBeCalled()
    });

    it('useMutation is called with pick updating query', () => {
        const updatingQuery =
        gql`mutation Mutation($name: String!, $week: String!, $game: String!, $pick: String!)
        { updatePick(name: $name, userPick: { week: $week, game: $game, pick: $pick })
        }`;

        expect(useMutation.mock.calls[0][0]).toBe(updatingQuery);
    });

    describe('rendered picks cells', () => {
        it('WeeklyGamesGrid is given sendData callback', () => {
            let grid = null;
            let isCalled = false;
            const callback = () => {
                isCalled = true;
            };
            useMutation.mockReturnValue([callback]);
            act(() => {
                grid = create(<WeeklyGamesGrid
                    currentWeek="0"
                    users={mockQueryData.users}
                    games={mockQueryData.games}
                    userPicks={mockQueryData.userPicks}
                    totals={mockQueryData.userTotals}/>);
            });
            const cell = grid.root.find(el => el.props.id === "user-picks-grid");

            cell.props.sendData("name", "game", "pick");

            expect(isCalled).toBe(true)
        });


        it('Renders twelve pick cells when there are three users and four games in data response', () => {
            const pickCells = findByClassName(grid, 'pick-cell');

            expect(pickCells.length).toBe(mockQueryData.games.length * mockQueryData.users.length);

            assertAllUserPicksMatchCellText(mockQueryData, pickCells);
        });

        it('Pick cells are of PickCell type', () => {
            const pickCells = findByClassName(grid, 'pick-cell');

            pickCells.map(cell => expect(cell.type).toBe(PickCell))
        });

        it('can choose specific game from pick list for first mock user', () => {
            let picks = mockQueryData["userPicks"][0].picks;

            expect(getPickByGame(picks, "CHI@GB")).toBe("CHI")
        });

        it('can choose specific game from pick list for second mock user', () => {
            let picks = mockQueryData["userPicks"][1].picks;

            expect(getPickByGame(picks, "ANN@COL")).toBe("C")
        });

        it('empty list of picks returns null', () => {
            let emptyPicks = [];

            expect(getPickByGame(emptyPicks, "ANN@COL")).toBe(null)
        })
    });

    describe('rendered users cells', () => {

        it('Renders three id cells when there are three users in data response', () => {
            const nameCells = findByClassName(grid, 'name-linear-cell');

            expect(nameCells.length).toBe(mockQueryData.users.length);
            expect(nameCells.map(cell => cell.props.children))
                .toEqual(mockQueryData.users.map(user => user.name))
        });

        it('Renders two id cells when there are two users in data response', () => {
            const twoMockUserData = {
                "users": [
                    {"name": "Someone"},
                    {"name": "Derp"},
                ],
                "userTotals": [],
                "games": [],
                "leaders": []
            };

            const grid = create(<WeeklyGamesGrid
                currentWeek="0"
                users={twoMockUserData.users}
                games={twoMockUserData.games}
                userPicks={twoMockUserData.userPicks}
                totals={twoMockUserData.userTotals}/>).root;
            const nameCells = findByClassName(grid, 'name-linear-cell');

            expect(nameCells.length).toBe(twoMockUserData.users.length);
            expect(nameCells.map(cell => cell.props.children))
                .toEqual(twoMockUserData.users.map(user => user.name))
        });
    });

    describe('rendered totals cells', () => {
        it('Renders three total cells when there are three users in data response', () => {
            const totalCells = findByClassName(grid, 'total-linear-cell');

            expect(totalCells.length).toBe(mockQueryData.users.length);
            expect(totalCells.map(cell => cell.props.children))
                .toEqual(mockQueryData.userTotals.map(user => user.total))
        });

        it('Renders two total cells when there are two users in data response', () => {
            const twoMockUserData = {
                "users": [
                    {"name": "Someone"},
                    {"name": "Derp"},
                ],
                "userTotals": [
                    {"name": "Someone", "total": 0},
                    {"name": "Derp", "total": 4}
                ],
                "games": [],
                "leaders": []
            };

            const grid = create(<WeeklyGamesGrid
                currentWeek="0"
                users={twoMockUserData.users}
                games={twoMockUserData.games}
                userPicks={twoMockUserData.userPicks}
                totals={twoMockUserData.userTotals}/>).root;
            const totalCells = findByClassName(grid, 'total-linear-cell');

            expect(totalCells.length).toBe(twoMockUserData.users.length);
            expect(totalCells.map(cell => cell.props.children))
                .toEqual(twoMockUserData.userTotals.map(user => user.total))
        });
    });

    describe('rendered games cells', () => {
        it('Renders four game cells when there are four games in data response', () => {
            const gameCells = findByClassName(grid, 'grid__cell--game');

            expect(gameCells.length).toBe(mockQueryData.games.length);
            expect(gameCells.map(cell => cell.props.children))
                .toEqual(mockQueryData.games.map(game => game.name))
        });

        it('Renders one game cell when there is one game in data response', () => {
            const oneMockGameData = {
                "users": [],
                "games": [
                    {"name": "TLH@PCL"},
                ],
                "userTotals": [],
                "leaders": []
            };

            const grid = create(<WeeklyGamesGrid
                currentWeek="0"
                users={oneMockGameData.users}
                games={oneMockGameData.games}
                userPicks={oneMockGameData.userPicks}
                totals={oneMockGameData.userTotals}/>).root;

            const gameCells = findByClassName(grid, 'grid__cell--game');

            expect(gameCells.length).toBe(oneMockGameData.games.length);
            expect(gameCells.map(cell => cell.props.children))
                .toEqual(oneMockGameData.games.map(game => game.name))
        });

    });

    describe('rendered spread cells', () => {
        it('Renders four spread cells when there are four games in data response', () => {
            const spreadCells = findByClassName(grid, 'grid__cell--spread');

            expect(spreadCells.length).toBe(mockQueryData.games.length);
            expect(spreadCells.map(cell => cell.props.children))
                .toEqual(mockQueryData.games.map(game => game.spread))
        });

        it('Renders one spread cell when there is one game in data response', () => {
            const oneMockGameData = {
                "users": [],
                "games": [
                    {"name": "TLH@PCL", "spread": "-20"},
                ],
                "userTotals": [],
                "leaders": []
            };

            const grid = create(<WeeklyGamesGrid
                currentWeek="0"
                users={oneMockGameData.users}
                games={oneMockGameData.games}
                userPicks={oneMockGameData.userPicks}
                totals={oneMockGameData.userTotals}/>).root;

            const spreadCells = findByClassName(grid, 'grid__cell--spread');

            expect(spreadCells.length).toBe(oneMockGameData.games.length);
            expect(spreadCells.map(cell => cell.props.children))
                .toEqual(oneMockGameData.games.map(game => game.spread))
        });
    });

    describe('rendered result cells', () => {
        it('Renders four result cells when there are four games in data response', () => {
            const resultCells = findByClassName(grid, 'grid__cell--result');

            expect(resultCells.length).toBe(mockQueryData.games.length);
            expect(resultCells.map(cell => cell.props.children))
                .toEqual(mockQueryData.games.map(game => game.result))
        });

        it('Renders one result cell when there is one game in data response', () => {
            const oneMockGameData = {
                "users": [],
                "games": [
                    {"name": "TLH@PCL", "spread": "-20", "result": "PCL"},
                ],
                "userTotals": [],
                "leaders": []
            };

            const grid = create(<WeeklyGamesGrid
                currentWeek="0"
                users={oneMockGameData.users}
                games={oneMockGameData.games}
                userPicks={oneMockGameData.userPicks}
                totals={oneMockGameData.userTotals}/>).root;
            const resultCells = findByClassName(grid, 'grid__cell--result');

            expect(resultCells.length).toBe(oneMockGameData.games.length);
            expect(resultCells.map(cell => cell.props.children))
                .toEqual(oneMockGameData.games.map(game => game.result))
        });

    });
});