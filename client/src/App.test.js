import React from 'react';
import {render} from '@testing-library/react';
import {create} from "react-test-renderer";
import App from './App';
import PicksGrid from "./PicksGrid";
import {useQuery} from "@apollo/react-hooks";

export const mockUserData = {
  data: {
    "users": [
      {"name": "Davebob"},
      {"name": "Luuand"},
      {"name": "Vegas"},
    ]
  }
};

jest.mock('@apollo/react-hooks');
useQuery.mockReturnValue({loading: false, error: null, data: mockUserData});



test('renders learn react link', () => {
  const { getByText } = render(<App />);
  const linkElement = getByText("Make a pick");
  expect(linkElement).toBeInTheDocument();
});

test('has PicksGrid element', () => {
  const app = create(<App/>).root;
  expect(app.findAll(el => el.type === PicksGrid).length).toEqual(1);
});
