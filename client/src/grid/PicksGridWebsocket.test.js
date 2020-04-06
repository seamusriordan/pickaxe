import WS from "jest-websocket-mock";
import PicksGrid from "./PicksGrid";

import {create, act} from "react-test-renderer";
import React from "react";
import {useQuery, useMutation} from '@apollo/react-hooks';
import {mockQueryData} from "../testUtilities/MockQueryData";
import {buildWebsocketUri, getWebsocketHostname, getWebsocketPort, getWebsocketProtocol} from "../helpers";

jest.mock('@apollo/react-hooks');

describe('Websocket behavior', () => {
    const defaultEnv = process.env;

    describe('connection behavior', () => {
        let server;
        let refetched;
        let grid;

        beforeEach(() => {
            server = new WS("ws://localhost:8080/pickaxe/updateNotification");

            refetched = false;
            useQuery.mockReturnValue({
                loading: false, error: true, data: mockQueryData, refetch: () => {
                    refetched = true
                }
            });
            useMutation.mockReturnValue([() => {}]);

            act(() => {
                // eslint-disable-next-line no-unused-vars
                grid = create(<PicksGrid/>);
            });
        });

        afterEach(() => {
            server.close();
            WS.clean()
        });

        it('Opens a websocket', async () => {
            await server.connected;
            server.close()
        });

        it('On open calls refetch', async () => {
            await server.connected;
            expect(refetched).toEqual(true);
        });

        it('On message calls refetch', async () => {
            expect(refetched).toEqual(false);

            await server.connected;
            server.send("Hi");

            expect(refetched).toEqual(true);
        });

        it('On unmount disconnects if connection is open', async () => {
            await server.connected;

            act(() => {
                grid.unmount()
            });

            expect(server.server.clients()[0].readyState).toBe(WebSocket.CLOSING);
            await server.closed;
            expect(server.server.clients().length).toBe(0);
        });

        it('On unmount eventually closes if connection has not completed', async () => {
            act(() => {
                grid.unmount()
            });

            expect(server.server.clients()[0].readyState).toBe(WebSocket.CONNECTING);
            await server.closed;
            expect(server.server.clients().length).toBe(0);
        });
    });

    describe('uri from environment variables', () => {
        beforeEach(() => {
            process.env = {...defaultEnv};
        });

        it('websocketServer returns localhost when environment variable is not set', () => {
            expect(getWebsocketHostname()).toEqual('localhost');
            expect(buildWebsocketUri()).toEqual('ws://localhost:8080/pickaxe/updateNotification')
        });

        it('websocketServer returns host from environment variable', () => {
            process.env.REACT_APP_GRAPHQL_SERVER = 'someservername';
            expect(getWebsocketHostname()).toEqual('someservername');
            expect(buildWebsocketUri()).toEqual('ws://someservername:8080/pickaxe/updateNotification')
        });

        it('websocketPort returns 8080 when environment variable is not set', () => {
            expect(getWebsocketPort()).toEqual("8080")
        });

        it('websocketPort returns port from environment variable', () => {
            process.env.REACT_APP_GRAPHQL_PORT = "7979";
            expect(getWebsocketPort()).toEqual("7979");
            expect(buildWebsocketUri()).toEqual('ws://localhost:7979/pickaxe/updateNotification');
        });

        it('websocketProtocol returns ws when environment variable is not set', () => {
            expect(getWebsocketProtocol()).toEqual("ws")
        });

        it('websocketProtocol returns wss from environment variable', () => {
            process.env.REACT_APP_GRAPHQL_HTTPS = 1;
            expect(getWebsocketProtocol()).toEqual("wss");
            expect(buildWebsocketUri()).toEqual('wss://localhost:8080/pickaxe/updateNotification')
        });
    });
});