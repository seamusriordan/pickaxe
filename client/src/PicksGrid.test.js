import PicksGrid from "./PicksGrid";
import {create} from "react-test-renderer";
import React from "react";
import {useQuery} from '@apollo/react-hooks';

import gql from 'graphql-tag';

jest.mock('@apollo/react-hooks');

const mockUserData = {
    data: {
        "users": [
            {"name": "Davebob"},
            {"name": "Luuand"},
            {"name": "Vegas"},
        ]
    }
};


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

        expect(nameCells.length).toBe(mockUserData.data.users.length);
        expect(nameCells.map(cell => cell.props.children))
            .toEqual(mockUserData.data.users.map(user => user.name))
    });

    it('Renders two id cells when there are two users in data response', () => {
        const twoMockUserData = {
            data: {
                "users": [
                    {"name": "Someone"},
                    {"name": "Derp"},
                ]
            }
        };
        useQuery.mockReturnValue({loading: false, error: null, data: twoMockUserData});

        const grid = create(<PicksGrid/>).root;
        const nameCells = findByClassName(grid, 'namecell');

        expect(nameCells.length).toBe(twoMockUserData.data.users.length);
        expect(nameCells.map(cell => cell.props.children))
            .toEqual(twoMockUserData.data.users.map(user => user.name))
    })
});
