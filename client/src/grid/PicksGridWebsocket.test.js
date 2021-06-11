import WS from "jest-websocket-mock";
import WeeklyViewApp from "../WeeklyViewApp";

import {create, act} from "react-test-renderer";
import React from "react";
import {useQuery, useMutation} from '@apollo/react-hooks';
import {mockQueryData} from "../testUtilities/MockQueryData";
import {buildWebsocketUri} from "../helpers";

jest.mock('@apollo/react-hooks');

describe('Websocket behavior', () => {
    describe('connection behavior', () => {
        let server;
        let refetched;
        let grid;

        beforeEach(() => {
            delete window.location;
            window.location = {
                protocol: 'http:',
                host: 'localhost:8080'
            };

            server = new WS("ws://localhost:8080/pickaxe/updateNotification");

            refetched = false;
            useQuery.mockReturnValue({
                loading: false, error: true, data: mockQueryData, refetch: () => {
                    refetched = true;
                    return Promise.resolve(mockQueryData);
                }
            });
            useMutation.mockReturnValue([() => {
            }]);

            act(() => {
                // eslint-disable-next-line no-unused-vars
                grid = create(<WeeklyViewApp/>);
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

            await act(async () => {
                grid.unmount()
            });

            expect(server.server.clients()[0].readyState).toBe(WebSocket.CLOSING);
            await server.closed;
            await server.closed;

            expect(server.server.clients().length).toBe(0);
        });

        it('On unmount eventually closes if connection has not completed', async () => {
            await act(async () => {
                grid.unmount()
            });

            expect(server.server.clients()[0].readyState).toBe(WebSocket.CONNECTING);
            await server.closed;
            await server.closed;

            expect(server.server.clients().length).toBe(0);
        });
    });

    describe('uri from environment variables', () => {
        it('websocketServer returns ws and localhost from window.location http://localhost:8080', () => {
            delete window.location;
            window.location = {
                protocol: 'http:',
                host: 'localhost:8080'
            };

            expect(buildWebsocketUri()).toEqual('ws://localhost:8080/pickaxe/updateNotification')
        });

        it('websocketServer returns ws and someservername from window.location http://someservername:8080', () => {
            delete window.location;
            window.location = {
                protocol: 'http:',
                host: 'someservername:8080'
            };

            expect(buildWebsocketUri()).toEqual('ws://someservername:8080/pickaxe/updateNotification')
        });

        it('websocketServer returns ws and localhost from window.location http://localhost:7979', () => {
            delete window.location;
            window.location = {
                protocol: 'http:',
                host: 'localhost:7979'
            };

            expect(buildWebsocketUri()).toEqual('ws://localhost:7979/pickaxe/updateNotification');
        });

        it('websocketServer returns wss and localhost from window.location https://localhost:8080', () => {
            delete window.location;
            window.location = {
                protocol: 'https:',
                host: 'localhost:8080'
            };

            expect(buildWebsocketUri()).toEqual('wss://localhost:8080/pickaxe/updateNotification')
        });
    });
});
