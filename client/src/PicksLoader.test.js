import PicksGrid from "./grid/PicksGrid";
import {create} from "react-test-renderer";
import PicksLoader from "./PicksLoader";
import React from "react";
import {useMutation, useQuery} from "@apollo/react-hooks";
import {mockQueryData} from "./testUtilities/MockQueryData";

jest.mock('@apollo/react-hooks');

describe('PicksLoader', () => {

    beforeEach(() => {
        jest.resetAllMocks();
        useQuery.mockReturnValue({
            loading: false, error: null, data: mockQueryData, refetch: () => {
            }
        });
        useMutation.mockReturnValue([() => {
        }]);
    });

    it('has a PicksGrid element', () => {
        const loader = create(<PicksLoader/>).root;

        const grid = loader.findAllByType(PicksGrid);
        expect(grid.length).toEqual(1);
    });
});