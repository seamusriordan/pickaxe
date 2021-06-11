import WeeklyViewApp from "./WeeklyViewApp";
import {act, create} from "react-test-renderer";
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

    let loaderRoot = null;

    beforeEach(() => {
        jest.resetAllMocks();
        useQuery
            .mockReturnValueOnce({
                loading: false, error: null, data: {currentWeek: {name: "0"}, weeks: [{name: "0"}, {name: "1"}]}
            })
            .mockReturnValue(picksQueryResult);
        useMutation.mockReturnValue([() => {
        }]);

        let loader = null;
        act(() => {
            loader = create(<AppLoader/>);
        })
        loaderRoot = loader.root;
    });

    it('has a WeeklyGamesGrid element', () => {
        const grid = loaderRoot.findAllByType(WeeklyViewApp);
        expect(grid.length).toEqual(1);
    });

    it('passes current week of 0 to WeeklyGamesGrid', () => {
        const grid = loaderRoot.findByProps({id: "weekly-view-app"});

        expect(grid.props.defaultWeek).toEqual("0")
    });

    it('calls query for weeks', () => {
        expect(useQuery.mock.calls[0][0]).toEqual(WEEKS_QUERY);
    });

    it('passes current week of 1 to WeeklyGamesGrid', () => {
        useQuery.mockReset();
        useQuery
            .mockReturnValueOnce({
                loading: false, error: null, data: {currentWeek: {name: "1"}, weeks: ["0", "1"]}
            })
            .mockReturnValue(picksQueryResult);

        let loader = null;
        act(() => {
            loader = create(<AppLoader/>);
        })

        const grid = loader.root.findByProps({id: "weekly-view-app"});

        expect(grid.props.defaultWeek).toEqual("1")
    });

    it('when query loading shows loading', () => {
        useQuery.mockReset();

        useQuery
            .mockReturnValueOnce({loading: true, error: null, data: undefined});

        let loader = null;
        act(() => {
            loader = create(<AppLoader/>);
        })

        expect(loader.root.findByType('div').props.children).toEqual("Loading App")
    });

    it('when query errors shows error message', () => {
        useQuery.mockReset();

        useQuery
            .mockReturnValueOnce({loading: false, error: true, data: undefined});

        let loader = null;
        act(() => {
            loader = create(<AppLoader/>);
        })

        expect(loader.root.findByType('div').props.children).toEqual("graphQL query failed")
    });
});
