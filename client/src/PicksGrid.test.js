import PicksGrid from "./PicksGrid";
import {render} from '@testing-library/react'
import React from "react";

import gql from 'graphql-tag';

import { useQuery } from '@apollo/react-hooks';
jest.mock('@apollo/react-hooks');


describe('PicksGrid',  () => {
    it('Renders without crashing', () => {
        render(<PicksGrid/>);
    });

    it('calls useQuery with ', () => {
        const userQuery = gql`{
            users {
                name
            }
        }`;

        expect(useQuery.mock.calls[0][0]).toBe(userQuery)
    })
});
