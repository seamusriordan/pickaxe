
import WS from "jest-websocket-mock";
import PicksGrid, {websocketPort, websocketProtocol, websocketServer, websocketUri} from "./PicksGrid";

import {create, act} from "react-test-renderer";
import React from "react";
import {useQuery, useMutation} from '@apollo/react-hooks';
import {mockQueryData} from "./MockQueryData";

jest.mock('@apollo/react-hooks');

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
        const server = new WS("ws://localhost:8080/pickaxe/updateNotification");

        act(() => {
            // eslint-disable-next-line no-unused-vars
            create(<PicksGrid/>);
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
        const server = new WS("ws://localhost:8080/pickaxe/updateNotification");
        act(() => {
            // eslint-disable-next-line no-unused-vars
            create(<PicksGrid/>);
        });

        expect(refetched).toEqual(false);

        await server.connected;
        server.send("Hi");

        expect(refetched).toEqual(true);
        server.close();
        WS.clean()
    });

    it('On unmount disconnects if connection is open', async () => {
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

    it('On unmount eventually closes if connection has not completed', async () => {
        let grid = null;
        const server = new WS("ws://localhost:8080/pickaxe/updateNotification");
        act(() => {
            grid = create(<PicksGrid/>)
        });

        act(() => {
            grid.unmount()
        });

        expect(server.server.clients()[0].readyState).toBe(WebSocket.CONNECTING);
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