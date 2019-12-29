import React from 'react';
import {render} from '@testing-library/react';
import {create} from "react-test-renderer";
import App from './App';
import PicksGrid from "./PicksGrid";
import {MockedProvider} from "@apollo/react-testing";

test('renders learn react link', () => {
  const { getByText } = render(<MockedProvider><App/></MockedProvider>);
  const linkElement = getByText("Make a pick");
  expect(linkElement).toBeInTheDocument();
});

test('has PicksGrid element', () => {
  const app = create(<App/>).root;
  expect(app.findAll(el => el.type === PicksGrid).length).toEqual(1);
});
