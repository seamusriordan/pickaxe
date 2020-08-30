import WeeklyViewApp from "./grid/WeeklyViewApp";
import {create} from "react-test-renderer";
import AppLoader from "./AppLoader";
import React from "react";
import {useMutation, useQuery} from "@apollo/react-hooks";
import {mockQueryData} from "./testUtilities/MockQueryData";
import {WEEKS_QUERY} from "./graphqlQueries";

jest.mock('@apollo/react-hooks');

describe('PicksLoader', () => {
    const picksQueryResult = {
        loading: false, error: null, data: mockQueryData, refetch: () => {
        }
    };

    beforeEach(() => {
        jest.resetAllMocks();
        useQuery
            .mockReturnValueOnce({
                loading: false, error: null, data: {currentWeek: {name: "0"}, weeks: [{name: "0"}, {name: "1"}]}})
            .mockReturnValueOnce(picksQueryResult);
        useMutation.mockReturnValue([() => {
        }]);
    });

    it('has a WeeklyGamesGrid element', () => {
        const loader = create(<AppLoader/>).root;

        const grid = loader.findAllByType(WeeklyViewApp);
        expect(grid.length).toEqual(1);
    });

    it('passes current week of 0 to WeeklyGamesGrid', () => {
        const loader = create(<AppLoader/>).root;

        const grid = loader.findByProps({id: "weekly-view-app"});

        expect(grid.props.defaultWeek).toEqual("0")
    });

    it('calls query for weeks', () => {
        // eslint-disable-next-line no-unused-expressions
        create(<AppLoader/>).root;

        expect(useQuery.mock.calls[0][0]).toEqual(WEEKS_QUERY);
    });

    it('passes current week of 1 to WeeklyGamesGrid', () => {
        useQuery.mockReset();
        useQuery
            .mockReturnValueOnce({
                loading: false, error: null, data: {currentWeek: {name: "1"}, weeks: ["0", "1"]}})
            .mockReturnValueOnce(picksQueryResult);


        const loader = create(<AppLoader/>).root;

        const grid = loader.findByProps({id: "weekly-view-app"});

        expect(grid.props.defaultWeek).toEqual("1")
    });

    it('when query loading shows loading', () => {
        useQuery.mockReset();

        useQuery
            .mockReturnValueOnce({loading: true, error: null, data: undefined});

        const loader = create(<AppLoader/>).root;

        expect(loader.findByType('div').props.children).toEqual("Loading App")
    });

    it('when query errors shows error message', () => {
        useQuery.mockReset();

        useQuery
            .mockReturnValueOnce({loading: false, error: true, data: undefined});

        const loader = create(<AppLoader/>).root;

        expect(loader.findByType('div').props.children).toEqual("Something has gone wrong")
    });
});