import PicksGrid from "./PicksGrid";

import {create, act} from "react-test-renderer";
import React from "react";
import {useQuery, useMutation} from '@apollo/react-hooks';
import {mockQueryData} from "./MockQueryData";

import gql from 'graphql-tag';
import {fireEvent, render} from "@testing-library/react";


jest.mock('@apollo/react-hooks');


describe('PicksGrid basic behavior', () => {
    beforeEach(() => {
        jest.resetAllMocks();
        useQuery.mockReturnValue({
            loading: false, error: null, data: mockQueryData, refetch: () => {
            }
        });
        useMutation.mockReturnValue([() => {
        }]);
        // eslint-disable-next-line no-unused-vars,no-unused-expressions
        create(<PicksGrid/>).root;
    });

    it('calls useQuery with some kind of poll interval', () => {
        expect(useQuery).toBeCalled();
        expect(useQuery.mock.calls[0][1].pollInterval).toBeGreaterThan(0)
    });

    it('calls useMutation ', () => {
        expect(useMutation).toBeCalled()
    });

    it('Renders loading when loading from query is true', () => {
        useQuery.mockReturnValue({
            loading: true, error: false, data: undefined, refetch: () => {
            }
        });
        const grid = create(<PicksGrid/>).root;

        expect(grid.findAll(el => el.props.children === 'Loading').length).toEqual(1);
    });

    it('Renders error when error from query is truthy', () => {
        useQuery.mockReturnValue({
            loading: false, error: true, data: undefined, refetch: () => {
            }
        });
        const grid = create(<PicksGrid/>).root;

        expect(grid.findAll(el => el.props.children === 'Error').length).toEqual(1);
    });

    it('Renders derp when data from query is undefined', () => {
        useQuery.mockReturnValue({
            loading: false, error: undefined, data: undefined, refetch: () => {
            }
        });
        const grid = create(<PicksGrid/>).root;

        expect(grid.findAll(el => el.props.children === 'derp').length).toEqual(1);
    });

    it('useMutation is called with pick updating query', () => {
        act(() => {
            // eslint-disable-next-line no-unused-vars
            create(<PicksGrid/>);
        });

        const updatingQuery =
        gql`mutation Mutation($name: String!, $week: Int!, $game: String!, $pick: String!)
        { updatePick(name: $name, userPick: { week: $week, game: $game, pick: $pick })
        }`;

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
        act(() => {
            grid = create(<PicksGrid/>)
        });
        let cell = grid.root.find(el => el.props.id === "Vegas-HAR@NOR");

        act(() => {
            cell.children[0].props.onBlur({type: "onblur", target: {textContent: "THHH"}});
        });

        expect(sendDataSpyCalled).toBeTruthy();

        expect(calledData).toEqual({variables: {name: "Vegas", week: 0, game: "HAR@NOR", pick: "THHH"}})
    });

    it('PickCell send', () => {
        let sendDataSpyCalled = false;
        let calledData = null;
        let grid = null;

        useMutation.mockReturnValue([(data) => {
            calledData = data;
            sendDataSpyCalled = true;
        }]);
        act(() => {
            grid = create(<PicksGrid/>)
        });
        let cell = grid.root.find(el => el.props.id === "Davebob-CHI@GB");

        act(() => {
            cell.children[0].props.onBlur({type: "onkeypress", "keyCode": 13, target: {textContent: "GUB"}});
        });
        expect(sendDataSpyCalled).toBeTruthy();

        expect(calledData).toEqual({variables: {name: "Davebob", week: 0, game: "CHI@GB", pick: "GUB"}})
    });

    it('On blur event, sends data with cell InnerHTML', () => {
        let calledData = null;
        useMutation.mockReturnValue([(data) => {
            calledData = data;
        }]);
        let {container} = render(<PicksGrid/>);
        let cell = container.querySelector('#Vegas-CHI\\@GB');

        act(() => {
            fireEvent.blur(cell, {target: {textContent: "CHI"}});
        });

        expect(calledData.variables.pick).toBe("CHI")
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

        act(() => {
            fireEvent.blur(cell, {target: {textContent: "CHI\nall this other data"}});
        });

        expect(calledData.variables.pick).toBe("CHI")
    });

    it('On blur event, innerHTML from textContent with newlines only have up to first newline', () => {
        let {container} = render(<PicksGrid/>);
        let cell = container.querySelector('#Vegas-CHI\\@GB');

        act(() => {
            fireEvent.blur(cell, {target: {textContent: "CHI\nall this other data"}});
        });

        cell = container.querySelector('#Vegas-CHI\\@GB');

        expect(cell.textContent).toBe("CHI")
    });
});
