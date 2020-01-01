import PicksGrid from "./PicksGrid";
import {create, act} from "react-test-renderer";
import React from "react";
import {useQuery, useMutation} from '@apollo/react-hooks';
import {mockQueryData} from "./MockQueryData";

import gql from 'graphql-tag';
import {fireEvent, render} from "@testing-library/react";


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
        gql`mutation Mutation($name: String, $pick: UpdatedPick)
        { updatePick(name: $name, pick: $pick)}`;

        expect(useMutation.mock.calls[0][0]).toBe(updatingQuery);
    });

    it('PickCell sendData callback executes send with update on onBlur', () => {
        let sendDataSpyCalled = false;
        let calledData = null;
        let grid = null;

        useMutation.mockReturnValue([(data) => {
            calledData = data;
            sendDataSpyCalled = true;
        }]);
        act(() => {grid = create(<PicksGrid/>)});
        let cell = grid.root.find(el => el.props.id === "Vegas-HAR@NOR");

        act(() => {
            cell.children[0].props.onBlur({type: "onblur", target: {textContent: "THHH"}});
        });

        expect(sendDataSpyCalled).toBeTruthy();

        expect(calledData).toEqual({variables: {name: "Vegas", pick: {game: "HAR@NOR", pick: "THHH"}}})
    });

    it('PickCell send', () => {
        let sendDataSpyCalled = false;
        let calledData = null;
        let grid = null;

        useMutation.mockReturnValue([(data) => {
            calledData = data;
            sendDataSpyCalled = true;
        }]);
        act(() => {grid = create(<PicksGrid/>)});
        let cell = grid.root.find(el => el.props.id === "Davebob-CHI@GB");

        act(() => {
            cell.children[0].props.onBlur({type: "onkeypress", "keyCode": 13, target: {textContent: "GUB"}});
        });
        expect(sendDataSpyCalled).toBeTruthy();

        expect(calledData).toEqual({variables: {name: "Davebob", pick: {game: "CHI@GB", pick: "GUB"}}})
    });

    it('On blur event, sends data with cell InnerHTML', () => {
        let calledData = null;
        useMutation.mockReturnValue([(data) => {
            calledData = data;
        }]);
        let {container} = render(<PicksGrid/>);
        let cell = container.querySelector('#Vegas-CHI\\@GB');

        act(()=> {
            fireEvent.blur(cell, {target: {textContent: "CHI"}});
        });

        expect(calledData.variables.pick.pick).toBe("CHI")
    });

    it('On blur event, do not send data when no change', () => {
        let spyCalled = false;
        useMutation.mockReturnValue([() => {
            spyCalled = true;
        }]);
        let {container} = render(<PicksGrid/>);
        let cell = container.querySelector('#Vegas-CHI\\@GB');

        act(() => {
            fireEvent.blur(cell, {target: {textContent: "B"}});
        });

        expect(spyCalled).toBeFalsy();
    });

    it('On blur event, textContent with newlines only sends up to first newline', () => {
        let calledData = null;
        useMutation.mockReturnValue([(data) => {
            calledData = data;
        }]);
        let {container} = render(<PicksGrid/>);
        let cell = container.querySelector('#Vegas-CHI\\@GB');

        act(()=>{
            fireEvent.blur(cell, {target: {textContent: "CHI\nall this other data"}});
        });

        expect(calledData.variables.pick.pick).toBe("CHI")
    });

    it('On blur event, innerHTML from textContent with newlines only have up to first newline', () => {
        let calledData;
        useMutation.mockReturnValue([(data) => {
            calledData = data;
        }]);
        let {container} = render(<PicksGrid/>);
        let cell = container.querySelector('#Vegas-CHI\\@GB');

        act(() => {
            fireEvent.blur(cell, {target: {textContent: "CHI\nall this other data"}});
        });

        cell = container.querySelector('#Vegas-CHI\\@GB');

        expect(cell.textContent).toBe("CHI")
    })


});



