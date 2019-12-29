import React from 'react';
import {create, act} from "react-test-renderer";
import App, {graphqlServer, graphqlPort, graphqlProtocol} from './App';
import PicksGrid from "./PicksGrid";
import {MockedProvider} from "@apollo/react-testing";


describe('App', () => {
    beforeEach(() => {
        jest.resetAllMocks();
    });

    test('renders learn react link', () => {
        let app;
        act(() => {
            app = create(<MockedProvider><App/></MockedProvider>);
        });
        expect(app.root.findAll(el => el.props.children === 'Make a pick')).toBeDefined();
    });

    test('has PicksGrid element', () => {
        let app;
        act(() => {
            app = create(<MockedProvider><App/></MockedProvider>);
        });
        expect(app.root.findAll(el => el.type === PicksGrid).length).toEqual(1);
    });

    test('graphqlServer returns localhost when environment variable is not set', () => {
        expect(graphqlServer()).toEqual('localhost')
    });

    test('graphqlServer returns host from environment variable', () => {
        process.env.REACT_APP_GRAPHQL_SERVER = 'someservername';
        expect(graphqlServer()).toEqual('someservername')
    });

    test('graphqlPort returns 8080 when environment variable is not set', () => {
        expect(graphqlPort()).toEqual("8080")
    });

    test('graphqlPort returns port from environment variable', () => {
        process.env.REACT_APP_GRAPHQL_PORT = "7979";
        expect(graphqlPort()).toEqual("7979")
    });

    test('graphqlProtocol returns http when environment variable is not set', () => {
        expect(graphqlProtocol()).toEqual("http")
    });

    test('graphqlProtocol returns port from environment variable', () => {
        process.env.REACT_APP_GRAPHQL_HTTPS = 1;
        expect(graphqlProtocol()).toEqual("https")
    });


});
