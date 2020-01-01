import PicksGrid from "./PicksGrid";
import {create, act} from "react-test-renderer";
import React from "react";
import {useQuery, useMutation} from '@apollo/react-hooks';
import {mockQueryData} from "./MockQueryData";

import gql from 'graphql-tag';


jest.mock('@apollo/react-hooks');


describe('PicksGrid basic behavior', () => {
    let grid;

    beforeEach(() => {
        jest.resetAllMocks();
        useQuery.mockReturnValue({loading: false, error: null, data: mockQueryData});
        useMutation.mockReturnValue([() => {
        }]);
        grid = create(<PicksGrid/>).root;
    });

    it('calls useQuery with ', () => {
        const userQuery = gql`query Query { users { name picks { game pick } total } games { name spread result } }`;
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
    });

    it('useMutation is called with pick updating query', () => {
        let grid;
        act(() => {grid = create(<PicksGrid/>)});

        const updatingQuery =
        gql`mutation Pick($name: String, $pick: Pick)
        { updatePick(name: $name, pick: $pick) {
            user {
                pick
            }
        }}`;

        expect(useMutation.mock.calls[0][0]).toBe(updatingQuery);
    });

    it('PickCell sendData callback executes send with update on onBlur', () => {
        let sendDataSpyCalled = false;
        let calledData;
        let grid;

        useMutation.mockReturnValue([(data) => {
            calledData = data;
            sendDataSpyCalled = true;
        }]);
        act(() => {grid = create(<PicksGrid/>)});
        let cell = grid.root.find(el => el.props.id === "Vegas-HAR@NOR");

        cell.children[0].props.onBlur({type: "onblur"});

        expect(sendDataSpyCalled).toBeTruthy();

        expect(calledData).toEqual({name: "Vegas", pick: {game: "HAR@NOR", pick: "TH"}})
    })

});



