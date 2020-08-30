import WeeklyViewApp from "./WeeklyViewApp";

import {create, act} from "react-test-renderer";
import React from "react";
import {useQuery, useMutation} from '@apollo/react-hooks';
import {mockQueryData} from "../testUtilities/MockQueryData";

import gql from 'graphql-tag';
import {assertAllUserPicksMatchCellText, findByClassName, getPickByGame} from "../testUtilities/Helpers";
import PickCell from "./PickCell";
import {Leaderboard} from "../leaderboard/Leaderboard";
import {LeaderboardRow} from "../leaderboard/LeaderboardRow";


jest.mock('@apollo/react-hooks');


describe('WeeklyViewApp', () => {
    let grid;
    let refetchSpy;

    beforeEach(() => {
        jest.resetAllMocks();
        refetchSpy = jest.fn();
        refetchSpy.mockReturnValue(Promise.resolve())

        useQuery.mockReturnValue({
            loading: false, error: null, data: mockQueryData, refetch: refetchSpy
        });
        useMutation.mockReturnValue([() => {
        }]);
        // eslint-disable-next-line no-unused-vars,no-unused-expressions
        grid = create(<WeeklyViewApp defaultWeek="0"/>).root;
    });

    it('calls useQuery with some poll interval', () => {
        expect(useQuery).toBeCalled();
        expect(useQuery.mock.calls[0][1].pollInterval).toBeGreaterThan(0)
    });

    it('calls useQuery with default week of 1', () => {
        const defaultWeek = "1";
        jest.resetAllMocks();
        useQuery.mockReturnValue({
            loading: false, error: null, data: mockQueryData, refetch: () => {
            }
        });
        useMutation.mockReturnValue([() => {
        }]);

        // eslint-disable-next-line no-unused-expressions
        create(<WeeklyViewApp defaultWeek={defaultWeek}/>).root;
        expect(useQuery.mock.calls[0][1].variables.week).toEqual(defaultWeek)
    });

    it('calls useMutation', () => {
        expect(useMutation).toBeCalled()
    });

    it('Renders error when error from query is truthy', () => {
        useQuery.mockReturnValue({
            loading: false, error: true, data: undefined, refetch: () => {
            }
        });
        const grid = create(<WeeklyViewApp/>).root;

        expect(grid.findAll(el => el.props.children === 'Error').length).toEqual(1);
    });

    it('Renders Waiting for data when data from query is undefined', () => {
        useQuery.mockReturnValue({
            loading: false, error: undefined, data: undefined, refetch: () => {
            }
        });
        const grid = create(<WeeklyViewApp/>).root;

        expect(grid.findAll(el => el.props.children === 'Waiting for data...').length).toEqual(1);
    });

    it('useMutation is called with pick updating query', () => {
        const updatingQuery =
        gql`mutation Mutation($name: String!, $week: String!, $game: String!, $pick: String!)
        { updatePick(name: $name, userPick: { week: $week, game: $game, pick: $pick })
        }`;

        expect(useMutation.mock.calls[0][0]).toBe(updatingQuery);
    });



    describe('advance week', () => {
        let week0Change;

        beforeEach(() => {
            week0Change = grid.findByProps({id: "change-week"});
        })

        it('on week 0 refetches with week 1', () => {
            act(() => {
                week0Change.props.forward();
            })

            expect(refetchSpy.mock.calls[0][0]).toEqual({
                week: mockQueryData.weeks[1].name
            })
        });


        it('twice on week 0 refetches with week 2', () => {
            act(() => {
                week0Change.props.forward();
            })
            act(() => {
                week0Change.props.forward();
            })

            expect(refetchSpy.mock.calls[1][0]).toEqual({
                week: mockQueryData.weeks[2].name
            })
        });

        it('on week 0 updates displayed week', () => {
            const displayedWeek = week0Change.findByProps({id: "change-week--week"})

            act(() => {
                week0Change.props.forward();
            })

            expect(displayedWeek.children[0]).toContain(mockQueryData.weeks[1].name);
        });

        it('on week 1 refetches with week 2', () => {
            const week1Grid = create(<WeeklyViewApp defaultWeek="1"/>).root;
            const changeWeek = week1Grid.findByProps({id: "change-week"})

            act(() => {
                changeWeek.props.forward();
            })

            expect(refetchSpy.mock.calls[0][0]).toEqual({
                week: mockQueryData.weeks[2].name
            })
        });

        it('on final week does nothing', () => {
            const week2Grid = create(<WeeklyViewApp defaultWeek="2"/>).root;
            const changeWeek = week2Grid.findByProps({id: "change-week"})

            act(() => {
                changeWeek.props.forward();
            })

            expect(refetchSpy).not.toHaveBeenCalled();
        });
    });

    describe('rewind week', () => {
        let week2Grid;
        let week2Change;

        beforeEach(() => {
            week2Grid = create(<WeeklyViewApp defaultWeek="2"/>).root;
            week2Change = week2Grid.findByProps({id: "change-week"});
        })

        it('on week 2 refetches with week 1', () => {
            act(() => {
                week2Change.props.back();
            })

            expect(refetchSpy.mock.calls[0][0]).toEqual({
                week: mockQueryData.weeks[1].name
            })
        });

        it('twice on week 2 refetches with week 0', () => {
            act(() => {
                week2Change.props.back();
            })
            act(() => {
                week2Change.props.back();
            })

            expect(refetchSpy.mock.calls[1][0]).toEqual({
                week: mockQueryData.weeks[0].name
            })
        });

        it('on week 2 updates displayed week', () => {
            const displayedWeek = week2Change.findByProps({id: "change-week--week"})

            act(() => {
                week2Change.props.back();
            })

            expect(displayedWeek.children[0]).toContain(mockQueryData.weeks[1].name);
        });

        it('on week 1 refetches with week 0', () => {
            const week1Grid = create(<WeeklyViewApp defaultWeek="1"/>).root;
            const changeWeek = week1Grid.findByProps({id: "change-week"})

            act(() => {
                changeWeek.props.back();
            })

            expect(refetchSpy.mock.calls[0][0]).toEqual({
                week: mockQueryData.weeks[0].name
            })
        });

        it('on first week does nothing', () => {
            const changeWeek = grid.findByProps({id: "change-week"})

            act(() => {
                changeWeek.props.back();
            })

            expect(refetchSpy).not.toHaveBeenCalled();
        });
    })
    describe('rendered picks cells', () => {
        let grid, renderer;

        beforeEach(() => {
            jest.resetAllMocks();
            useQuery.mockReturnValue({loading: false, error: null, data: mockQueryData});
            useMutation.mockReturnValue([() => {}]);

            renderer = create(<WeeklyViewApp defaultWeek="0"/>);
            grid = renderer.root;
        });

        it('WeeklyGamesGrid is given sendData callback', () => {
            let grid = null;
            let isCalled = false;
            const callback = () => {
                isCalled = true;
            };
            useMutation.mockReturnValue([callback]);
            act(() => {
                grid = create(<WeeklyViewApp defaultWeek="0"/>)
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
                "userTotals":[],
                "games": [],
                "leaders": []
            };
            useQuery.mockReturnValue({loading: false, error: null, data: twoMockUserData});

            const grid = create(<WeeklyViewApp/>).root;
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
                "userTotals":[
                    {"name": "Someone", "total": 0},
                    {"name": "Derp", "total": 4}
                ],
                "games": [],
                "leaders": []
            };
            useQuery.mockReturnValue({loading: false, error: null, data: twoMockUserData});

            const grid = create(<WeeklyViewApp/>).root;
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
                "userTotals":[],
                "leaders": []
            };
            useQuery.mockReturnValue({loading: false, error: null, data: oneMockGameData});

            const grid = create(<WeeklyViewApp/>).root;
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
                "userTotals":[],
                "leaders": []
            };
            useQuery.mockReturnValue({loading: false, error: null, data: oneMockGameData});

            const grid = create(<WeeklyViewApp/>).root;
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
                "userTotals":[],
                "leaders": []
            };
            useQuery.mockReturnValue({loading: false, error: null, data: oneMockGameData});

            const grid = create(<WeeklyViewApp/>).root;
            const resultCells = findByClassName(grid, 'grid__cell--result');

            expect(resultCells.length).toBe(oneMockGameData.games.length);
            expect(resultCells.map(cell => cell.props.children))
                .toEqual(oneMockGameData.games.map(game => game.result))
        });

    });
    describe('leaderboard', () => {
        it('has leaderboard', () => {
            const leaderboard = grid.findAllByType(Leaderboard)

            expect(leaderboard).toHaveLength(1)
        })

        it('has row for each leader', () => {
            const rows = grid.findByType(Leaderboard).findAllByType(LeaderboardRow)

            expect(rows).toHaveLength(mockQueryData.leaders.length)
        })
    });

});
