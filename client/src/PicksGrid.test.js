import PicksGrid from "./PicksGrid";
import {create} from "react-test-renderer";
import React from "react";
import {useQuery} from '@apollo/react-hooks';

import gql from 'graphql-tag';

export const mockUserData = {
    "users": [
        {"name": "Davebob"},
        {"name": "Luuand"},
        {"name": "Vegas"},
    ]
};

jest.mock('@apollo/react-hooks');
useQuery.mockReturnValue({loading: false, error: null, data: mockUserData});

function findByClassName(grid, className) {
    return grid.findAll(
        el => {
            return el.props.className === className
        });
}

describe('PicksGrid', () => {
    let grid;

    beforeEach(() => {
        jest.resetAllMocks();
        useQuery.mockReturnValue({loading: false, error: null, data: mockUserData});
        grid = create(<PicksGrid/>).root;
    });

    it('calls useQuery with ', () => {
        const userQuery = gql`{
            users {
                name
            }
        }`;
        expect(useQuery.mock.calls[0][0]).toBe(userQuery)
    });

    it('Renders three id cells when there are three users in data response', () => {
        const nameCells = findByClassName(grid, 'namecell');

        expect(nameCells.length).toBe(mockUserData.users.length);
        expect(nameCells.map(cell => cell.props.children))
            .toEqual(mockUserData.users.map(user => user.name))
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
        const nameCells = findByClassName(grid, 'namecell');

        expect(nameCells.length).toBe(twoMockUserData.users.length);
        expect(nameCells.map(cell => cell.props.children))
            .toEqual(twoMockUserData.users.map(user => user.name))
    });

    it('Renders loading when loading from query is true', () => {
        useQuery.mockReturnValue({loading: true, error: false, data: undefined});
        const grid = create(<PicksGrid/>).root;

        expect(grid.findAll(el => el.props.children === 'Loading').length).toEqual(1);
    })

    it('Renders error when error from query is truthy', () => {
        useQuery.mockReturnValue({loading: false, error: true, data: undefined});
        const grid = create(<PicksGrid/>).root;

        expect(grid.findAll(el => el.props.children === 'Error').length).toEqual(1);
    })

    it('Renders derp when data from query is undefined', () => {
        useQuery.mockReturnValue({loading: false, error: undefined, data: undefined});
        const grid = create(<PicksGrid/>).root;

        expect(grid.findAll(el => el.props.children === 'derp').length).toEqual(1);
    })
});
