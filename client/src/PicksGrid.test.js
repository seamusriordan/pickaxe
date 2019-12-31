import PicksGrid from "./PicksGrid";
import {create} from "react-test-renderer";
import React from "react";
import {useQuery} from '@apollo/react-hooks';

import gql from 'graphql-tag';

export const mockQueryData = {
    "users": [
        {"name": "Davebob"},
        {"name": "Luuand"},
        {"name": "Vegas"},
    ],

    "games": [
        {"name": "CHI@GB"},
        {"name": "HAR@NOR"},
        {"name": "SFE@CRL"},
        {"name": "ANN@COL"},
    ]
};

jest.mock('@apollo/react-hooks');
useQuery.mockReturnValue({loading: false, error: null, data: mockQueryData});

function findByClassName(grid, className) {
    return grid.findAll(
        el => {
            return el.props.className === className
        });
}

describe('PicksGrid basic behavior', () => {
    let grid;

    beforeEach(() => {
        jest.resetAllMocks();
        useQuery.mockReturnValue({loading: false, error: null, data: mockQueryData});
        grid = create(<PicksGrid/>).root;
    });

    it('calls useQuery with ', () => {
        const userQuery = gql`query Query { users { name }}`;
        expect(useQuery.mock.calls[0][0]).toBe(userQuery)
    });



    it('Renders loading when loading from query is true', () => {
        useQuery.mockReturnValue({loading: true, error: false, data: undefined});
        const grid = create(<PicksGrid/>).root;

        expect(grid.findAll(el => el.props.children === 'Loading').length).toEqual(1);
    });

    it('Renders error when error from query is truthy', () => {
        useQuery.mockReturnValue({loading: false, error: true, data: undefined});
        const grid = create(<PicksGrid/>).root;

        expect(grid.findAll(el => el.props.children === 'Error').length).toEqual(1);
    });

    it('Renders derp when data from query is undefined', () => {
        useQuery.mockReturnValue({loading: false, error: undefined, data: undefined});
        const grid = create(<PicksGrid/>).root;

        expect(grid.findAll(el => el.props.children === 'derp').length).toEqual(1);
    })

});

describe('PicksGrid data rendering', () => {
    let grid;

    beforeEach(() => {
        jest.resetAllMocks();
        useQuery.mockReturnValue({loading: false, error: null, data: mockQueryData});
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
});
