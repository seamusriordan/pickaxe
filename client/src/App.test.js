import React from 'react';
import {create, act} from "react-test-renderer";
import App from './App';
import {MockedProvider} from "@apollo/react-testing";
import {buildGraphqlUri} from "./helpers";
import AppLoader from "./AppLoader";


describe('App', () => {
    beforeEach(() => {
        jest.resetAllMocks();
        jest.resetModules();
    });

    it('renders learn react link', () => {
        let app = null;
        act(() => {
            app = create(<MockedProvider><App/></MockedProvider>);
        });
        expect(app.root.findAll(el => el.props.children === 'Make a pick')).toBeDefined();
    });

    it('has PicksLoader element', () => {
        let app = null;
        act(() => {
            app = create(<MockedProvider><App/></MockedProvider>);
        });
        expect(app.root.findAll(el => el.type === AppLoader).length).toEqual(1);
    });

    it('buildGraphqlUri returns http and localhost:8080 from window.location', () => {
        delete window.location;
        window.location = {
            protocol: 'http',
            host: 'localhost:8080'
        };

        expect(buildGraphqlUri()).toEqual('http://localhost:8080/pickaxe/graphql')
    });

    it('buildGraphqlUri returns http and someservername:8080 from window.location', () => {
        delete window.location;
        window.location = {
            protocol: 'http',
            host: 'someservername:8080'
        };

        expect(buildGraphqlUri()).toEqual('http://someservername:8080/pickaxe/graphql')
    });

    it('buildGraphqlUri returns http and localhost:7979 from window.location', () => {
        delete window.location;
        window.location = {
            protocol: 'http',
            host: 'localhost:7979'
        };

        expect(buildGraphqlUri()).toEqual('http://localhost:7979/pickaxe/graphql');
    });

    it('buildGraphqlUri returns https and localhost:7979 from window.location', () => {
        delete window.location;
        window.location = {
            protocol: 'https',
            host: 'localhost:8080'
        };
        expect(buildGraphqlUri()).toEqual('https://localhost:8080/pickaxe/graphql')
    });
});
