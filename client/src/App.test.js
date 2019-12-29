import React from 'react';
import {render} from '@testing-library/react';
import {create} from "react-test-renderer";
import App, {graphqlServer, graphqlPort} from './App';
import PicksGrid from "./PicksGrid";
import {MockedProvider} from "@apollo/react-testing";


describe('App', () => {
  test('renders learn react link', done => {
      const app = create(<MockedProvider><App/></MockedProvider>).root;
      expect(app.findAll(el => el.props.children === 'Make a pick')).toBeDefined();
      done()
  });

  test('has PicksGrid element', done => {
    const app = create(<MockedProvider><App/></MockedProvider>).root;
    expect(app.findAll(el => el.type === PicksGrid).length).toEqual(1);
    done()
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


});
