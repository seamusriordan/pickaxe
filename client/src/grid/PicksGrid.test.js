import PicksGrid from "./PicksGrid";

import {create, act} from "react-test-renderer";
import React from "react";
import {useQuery, useMutation} from '@apollo/react-hooks';
import {mockQueryData} from "../testUtilities/MockQueryData";

import gql from 'graphql-tag';


jest.mock('@apollo/react-hooks');


describe('PicksGrid', () => {
    let grid;
    let refetchSpy;

    beforeEach(() => {
        jest.resetAllMocks();
        refetchSpy = jest.fn();
        refetchSpy.mockReturnValue(Promise.resolve())

        useQuery.mockReturnValue({
            loading: false, error: null, data: mockQueryData, refetch: refetchSpy
        });
        useMutation.mockReturnValue([() => {
        }]);
        // eslint-disable-next-line no-unused-vars,no-unused-expressions
        grid = create(<PicksGrid defaultWeek="0"/>).root;
    });

    it('calls useQuery with some poll interval', () => {
        expect(useQuery).toBeCalled();
        expect(useQuery.mock.calls[0][1].pollInterval).toBeGreaterThan(0)
    });

    it('calls useQuery with default week of 1', () => {
        const defaultWeek = "1";
        jest.resetAllMocks();
        useQuery.mockReturnValue({
            loading: false, error: null, data: mockQueryData, refetch: () => {
            }
        });
        useMutation.mockReturnValue([() => {
        }]);

        // eslint-disable-next-line no-unused-expressions
        create(<PicksGrid defaultWeek={defaultWeek}/>).root;
        expect(useQuery.mock.calls[0][1].variables.week).toEqual(defaultWeek)
    });

    it('calls useMutation', () => {
        expect(useMutation).toBeCalled()
    });

    it('Renders loading when loading from query is true', () => {
        useQuery.mockReturnValue({
            loading: true, error: false, data: undefined, refetch: () => {
            }
        });
        const grid = create(<PicksGrid/>).root;

        expect(grid.findAll(el => el.props.children === 'Loading').length).toEqual(1);
    });

    it('Renders error when error from query is truthy', () => {
        useQuery.mockReturnValue({
            loading: false, error: true, data: undefined, refetch: () => {
            }
        });
        const grid = create(<PicksGrid/>).root;

        expect(grid.findAll(el => el.props.children === 'Error').length).toEqual(1);
    });

    it('Renders derp when data from query is undefined', () => {
        useQuery.mockReturnValue({
            loading: false, error: undefined, data: undefined, refetch: () => {
            }
        });
        const grid = create(<PicksGrid/>).root;

        expect(grid.findAll(el => el.props.children === 'derp').length).toEqual(1);
    });

    it('useMutation is called with pick updating query', () => {
        const updatingQuery =
        gql`mutation Mutation($name: String!, $week: String!, $game: String!, $pick: String!)
        { updatePick(name: $name, userPick: { week: $week, game: $game, pick: $pick })
        }`;

        expect(useMutation.mock.calls[0][0]).toBe(updatingQuery);
    });

    it('PickCells is given sendData callback', () => {
        let grid = null;
        const callback = () => {
        };
        useMutation.mockReturnValue([callback]);
        act(() => {
            grid = create(<PicksGrid/>)
        });

        const cell = grid.root.find(el => el.props.id === "pick-cells");
        expect(cell.props.sendData).toBe(callback);
    });

    describe('advance week', () => {
        it('on week 0 refetches with week 1', () => {
            const changeWeek = grid.findByProps({id: "change-week"})

            act(() => {
                changeWeek.props.forward();
            })

            expect(refetchSpy.mock.calls[0][0]).toEqual({
                week: mockQueryData.weeks[1].week
            })
        });

        it('on week 0 updates displayed week', () => {
            const changeWeek = grid.findByProps({id: "change-week"})
            const displayedWeek = changeWeek.findByProps({id: "changeWeek-week"})

            act(() => {
                changeWeek.props.forward();
            })

            expect(displayedWeek.children[0]).toContain(mockQueryData.weeks[1].week);
        });

        it('on week 1 refetches with week 2', () => {
            const week1Grid = create(<PicksGrid defaultWeek="1"/>).root;

            const changeWeek = week1Grid.findByProps({id: "change-week"})

            act(() => {
                changeWeek.props.forward();
            })

            expect(refetchSpy.mock.calls[0][0]).toEqual({
                week: mockQueryData.weeks[2].week
            })
        });

        it('on final week does nothing', () => {
            const week2Grid = create(<PicksGrid defaultWeek="2"/>).root;

            const changeWeek = week2Grid.findByProps({id: "change-week"})

            act(() => {
                changeWeek.props.forward();
            })

            expect(refetchSpy).not.toHaveBeenCalled();
        });
    });

    describe('rewind week', () => {
        it('on week 2 refetches with week 1', () => {
            const week2Grid = create(<PicksGrid defaultWeek="2"/>).root;

            const changeWeek = week2Grid.findByProps({id: "change-week"})

            act(() => {
                changeWeek.props.back();
            })

            expect(refetchSpy.mock.calls[0][0]).toEqual({
                week: mockQueryData.weeks[1].week
            })
        });

        it('on week 2 updates displayed week', () => {
            const week2Grid = create(<PicksGrid defaultWeek="2"/>).root;

            const changeWeek = week2Grid.findByProps({id: "change-week"})
            const displayedWeek = changeWeek.findByProps({id: "changeWeek-week"})

            act(() => {
                changeWeek.props.back();
            })

            expect(displayedWeek.children[0]).toContain(mockQueryData.weeks[1].week);
        });

        it('on week 1 refetches with week 0', () => {
            const week1Grid = create(<PicksGrid defaultWeek="1"/>).root;

            const changeWeek = week1Grid.findByProps({id: "change-week"})

            act(() => {
                changeWeek.props.back();
            })

            expect(refetchSpy.mock.calls[0][0]).toEqual({
                week: mockQueryData.weeks[0].week
            })
        });

        it('on first week does nothing', () => {
            const changeWeek = grid.findByProps({id: "change-week"})

            act(() => {
                changeWeek.props.back();
            })

            expect(refetchSpy).not.toHaveBeenCalled();
        });
    })


});
