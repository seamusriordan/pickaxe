import {useMutation, useQuery} from "@apollo/react-hooks";
import AppLoader from "./AppLoader";
import React from "react";
import {act, create} from "react-test-renderer";
import {mockQueryData} from "./testUtilities/MockQueryData";

jest.mock('@apollo/react-hooks')

describe('AppLoader', () => {
    beforeEach(() => {
        jest.resetAllMocks()
    })

    it('Passes current week when no network error', () => {
        useQuery.mockReturnValueOnce(
            {
                loading: false,
                error: null,
                data: {
                    currentWeek: {
                        name: "Week 0"
                    }
                }
            }
        ).mockReturnValueOnce(
            {
                loading: false,
                error: null,
                data: mockQueryData
            }
        );
        useMutation.mockReturnValue([() => {
        }]);

        let appLoader = null;
        act(() => {
            appLoader = create(<AppLoader/>)
        })

        const weeklyViewApp = appLoader.root.findByProps({'data-testid': "weekly-view-app"})
        expect(weeklyViewApp.props.defaultWeek).toBe("Week 0")
    });


    it('Goes to authorize on graphql unauthorized status', () => {
        delete window.location;
        window.location = {
            href: null
        }

        useQuery.mockReturnValueOnce(
            {
                loading: false,
                error: {
                    networkError: {
                        statusCode: 401
                    }
                },
                data: undefined
            }
        );

        act(() => {
            create(<AppLoader/>)
        })

        expect(window.location.href).toBe("/pickaxe/authorize")
    });
});