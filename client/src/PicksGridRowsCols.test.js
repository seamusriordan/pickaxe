import {useMutation, useQuery} from "@apollo/react-hooks";
import {create} from "react-test-renderer";
import PicksGrid from "./PicksGrid";
import React from "react";
import {mockQueryData} from "./MockQueryData";
import {findByClassName} from "./Helpers";

jest.mock('@apollo/react-hooks');

describe('PicksGrid data row/column rendering', () => {
    let grid;

    beforeEach(() => {
        jest.resetAllMocks();
        useQuery.mockReturnValue({loading: false, error: null, data: mockQueryData});
        useMutation.mockReturnValue([() => {}]);
        grid = create(<PicksGrid/>).root;
    });


    it('Renders three id cells when there are three users in data response', () => {
        const nameCells = findByClassName(grid, 'name-cell');

        expect(nameCells.length).toBe(mockQueryData.users.length);
        expect(nameCells.map(cell => cell.props.children))
            .toEqual(mockQueryData.users.map(user => user.name))
    });

    it('Renders two id cells when there are two users in data response', () => {
        const twoMockUserData = {
            "users": [
                {"name": "Someone"},
                {"name": "Derp"},
            ]
        };
        useQuery.mockReturnValue({loading: false, error: null, data: twoMockUserData});

        const grid = create(<PicksGrid/>).root;
        const nameCells = findByClassName(grid, 'name-cell');

        expect(nameCells.length).toBe(twoMockUserData.users.length);
        expect(nameCells.map(cell => cell.props.children))
            .toEqual(twoMockUserData.users.map(user => user.name))
    });

    it('Renders three total cells when there are three users in data response', () => {
        const totalCells = findByClassName(grid, 'total-cell');

        expect(totalCells.length).toBe(mockQueryData.users.length);
        expect(totalCells.map(cell => cell.props.children))
            .toEqual(mockQueryData.users.map(user => user.total))
    });

    it('Renders two total cells when there are two users in data response', () => {
        const twoMockUserData = {
            "users": [
                {"name": "Someone"},
                {"name": "Derp"},
            ]
        };
        useQuery.mockReturnValue({loading: false, error: null, data: twoMockUserData});

        const grid = create(<PicksGrid/>).root;
        const totalCells = findByClassName(grid, 'total-cell');

        expect(totalCells.length).toBe(twoMockUserData.users.length);
        expect(totalCells.map(cell => cell.props.children))
            .toEqual(twoMockUserData.users.map(user => user.total))
    });


    it('Renders four game cells when there are four games in data response', () => {
        const gameCells = findByClassName(grid, 'game-cell');

        expect(gameCells.length).toBe(mockQueryData.games.length);
        expect(gameCells.map(cell => cell.props.children))
            .toEqual(mockQueryData.games.map(game => game.name))
    });

    it('Renders one game cell when there is one game in data response', () => {
        const oneMockGameData = {
            "games": [
                {"name": "TLH@PCL"},
            ]
        };
        useQuery.mockReturnValue({loading: false, error: null, data: oneMockGameData});

        const grid = create(<PicksGrid/>).root;
        const gameCells = findByClassName(grid, 'game-cell');

        expect(gameCells.length).toBe(oneMockGameData.games.length);
        expect(gameCells.map(cell => cell.props.children))
            .toEqual(oneMockGameData.games.map(game => game.name))
    });

    it('Renders four spread cells when there are four games in data response', () => {
        const spreadCells = findByClassName(grid, 'spread-cell');

        expect(spreadCells.length).toBe(mockQueryData.games.length);
        expect(spreadCells.map(cell => cell.props.children))
            .toEqual(mockQueryData.games.map(game => game.spread))
    });

    it('Renders one spread cell when there is one game in data response', () => {
        const oneMockGameData = {
            "games": [
                {"name": "TLH@PCL", "spread": "-20"},
            ]
        };
        useQuery.mockReturnValue({loading: false, error: null, data: oneMockGameData});

        const grid = create(<PicksGrid/>).root;
        const spreadCells = findByClassName(grid, 'spread-cell');

        expect(spreadCells.length).toBe(oneMockGameData.games.length);
        expect(spreadCells.map(cell => cell.props.children))
            .toEqual(oneMockGameData.games.map(game => game.spread))
    });

    it('Renders four result cells when there are four games in data response', () => {
        const resultCells = findByClassName(grid, 'result-cell');

        expect(resultCells.length).toBe(mockQueryData.games.length);
        expect(resultCells.map(cell => cell.props.children))
            .toEqual(mockQueryData.games.map(game => game.result))
    });

    it('Renders one result cell when there is one game in data response', () => {
        const oneMockGameData = {
            "games": [
                {"name": "TLH@PCL", "spread": "-20", "result": "PCL"},
            ]
        };
        useQuery.mockReturnValue({loading: false, error: null, data: oneMockGameData});

        const grid = create(<PicksGrid/>).root;
        const resultCells = findByClassName(grid, 'result-cell');

        expect(resultCells.length).toBe(oneMockGameData.games.length);
        expect(resultCells.map(cell => cell.props.children))
            .toEqual(oneMockGameData.games.map(game => game.result))
    });


});
