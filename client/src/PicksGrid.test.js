import PicksGrid, {websocketPort, websocketProtocol, websocketServer, websocketUri} from "./PicksGrid";
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
        useQuery.mockReturnValue({
            loading: false, error: null, data: mockQueryData, refetch: () => {
            }
        });
        useMutation.mockReturnValue([() => {
        }]);
        grid = create(<PicksGrid/>).root;
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
        let grid;
        act(() => {
            grid = create(<PicksGrid/>)
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
    });


});

import WS from "jest-websocket-mock";
import {graphqlPort, graphqlProtocol, graphqlServer, serverUri} from "./App";

describe('Websocket behavior', () => {
    const defaultEnv = process.env;

    beforeEach(() => {
        useQuery.mockReturnValue({
            loading: false, error: null, data: mockQueryData, refetch: () => {
            }
        });
        useMutation.mockReturnValue([() => {
        }]);
        process.env = {...defaultEnv};
    });

    it('Opens a websocket', async () => {
        useQuery.mockReturnValue({
            loading: false, error: true, data: undefined, refetch: () => {
            }
        });
        let grid = null;
        const server = new WS("ws://localhost:8080/pickaxe/updateNotification");

        act(() => {
            grid = create(<PicksGrid/>)
        });

        await server.connected;
        server.close()
    });

    it('On message calls refetch', async () => {
        let refetched = false;
        useQuery.mockReturnValue({
            loading: false, error: true, data: undefined, refetch: () => {
                refetched = true
            }
        });
        let grid = null;
        const server = new WS("ws://localhost:8080/pickaxe/updateNotification");
        act(() => {
            grid = create(<PicksGrid/>)
        });

        expect(refetched).toEqual(false);

        await server.connected;
        server.send("Hi");

        expect(refetched).toEqual(true);
        server.close();
        WS.clean()
    });

    it('On unmount diconnects', async () => {
        let grid = null;
        const server = new WS("ws://localhost:8080/pickaxe/updateNotification");
        act(() => {
            grid = create(<PicksGrid/>)
        });
        await server.connected;

        act(() => {
            grid.unmount()
        });

        expect(server.server.clients()[0].readyState).toBe(WebSocket.CLOSING);
        await server.closed;
        expect(server.server.clients().length).toBe(0);

        server.close();
        WS.clean()
    });

    test('websocketServer returns localhost when environment variable is not set', () => {
        expect(websocketServer()).toEqual('localhost');
        expect(websocketUri()).toEqual('ws://localhost:8080/pickaxe/updateNotification')
    });

    test('websocketServer returns host from environment variable', () => {
        process.env.REACT_APP_GRAPHQL_SERVER = 'someservername';
        expect(websocketServer()).toEqual('someservername');
        expect(websocketUri()).toEqual('ws://someservername:8080/pickaxe/updateNotification')
    });

    test('websocketPort returns 8080 when environment variable is not set', () => {
        expect(websocketPort()).toEqual("8080")
    });

    test('websocketPort returns port from environment variable', () => {
        process.env.REACT_APP_GRAPHQL_PORT = "7979";
        expect(websocketPort()).toEqual("7979");
        expect(websocketUri()).toEqual('ws://localhost:7979/pickaxe/updateNotification');
    });

    test('websocketProtocol returns ws when environment variable is not set', () => {
        expect(websocketProtocol()).toEqual("ws")
    });

    test('websocketProtocol returns wss from environment variable', () => {
        process.env.REACT_APP_GRAPHQL_HTTPS = 1;
        expect(websocketProtocol()).toEqual("wss");
        expect(websocketUri()).toEqual('wss://localhost:8080/pickaxe/updateNotification')
    });
});