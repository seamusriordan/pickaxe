import React from 'react';
import {create, act} from "react-test-renderer";
import App, {graphqlServer, graphqlPort, graphqlProtocol, serverUri} from './App';
import PicksGrid from "./grid/PicksGrid";
import {MockedProvider} from "@apollo/react-testing";


describe('App', () => {
    const defaultEnv = process.env;

    beforeEach(() => {
        jest.resetAllMocks();
        jest.resetModules();
        process.env = {...defaultEnv};
    });

    it('renders learn react link', () => {
        let app = null;
        act(() => {
            app = create(<MockedProvider><App/></MockedProvider>);
        });
        expect(app.root.findAll(el => el.props.children === 'Make a pick')).toBeDefined();
    });

    it('has PicksGrid element', () => {
        let app = null;
        act(() => {
            app = create(<MockedProvider><App/></MockedProvider>);
        });
        expect(app.root.findAll(el => el.type === PicksGrid).length).toEqual(1);
    });

    it('graphqlServer returns localhost when environment variable is not set', () => {
        expect(graphqlServer()).toEqual('localhost');
        expect(serverUri()).toEqual('http://localhost:8080/pickaxe/graphql')
    });

    it('graphqlServer returns host from environment variable', () => {
        process.env.REACT_APP_GRAPHQL_SERVER = 'someservername';
        expect(graphqlServer()).toEqual('someservername');
        expect(serverUri()).toEqual('http://someservername:8080/pickaxe/graphql')
    });

    it('graphqlPort returns 8080 when environment variable is not set', () => {
        expect(graphqlPort()).toEqual("8080")
    });

    it('graphqlPort returns port from environment variable', () => {
        process.env.REACT_APP_GRAPHQL_PORT = "7979";
        expect(graphqlPort()).toEqual("7979");
        expect(serverUri()).toEqual('http://localhost:7979/pickaxe/graphql');
    });

    it('graphqlProtocol returns http when environment variable is not set', () => {
        expect(graphqlProtocol()).toEqual("http")
    });

    it('graphqlProtocol returns https from environment variable', () => {
        process.env.REACT_APP_GRAPHQL_HTTPS = 1;
        expect(graphqlProtocol()).toEqual("https");
        expect(serverUri()).toEqual('https://localhost:8080/pickaxe/graphql')
    });
});
