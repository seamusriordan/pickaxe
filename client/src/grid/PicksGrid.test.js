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

    it('Renders error when error from query is truthy', () => {
        useQuery.mockReturnValue({
            loading: false, error: true, data: undefined, refetch: () => {
            }
        });
        const grid = create(<PicksGrid/>).root;

        expect(grid.findAll(el => el.props.children === 'Error').length).toEqual(1);
    });

    it('Renders Waiting for data when data from query is undefined', () => {
        useQuery.mockReturnValue({
            loading: false, error: undefined, data: undefined, refetch: () => {
            }
        });
        const grid = create(<PicksGrid/>).root;

        expect(grid.findAll(el => el.props.children === 'Waiting for data...').length).toEqual(1);
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

    it('PickCells is given current week', () => {
        const cell = grid.findByProps({id: "pick-cells"});
        expect(cell.props.currentWeek).toBe(mockQueryData.weeks[0].name);
    });

    describe('advance week', () => {
        let week0Change;

        beforeEach(() => {
            week0Change = grid.findByProps({id: "change-week"});
        })

        it('on week 0 refetches with week 1', () => {
            act(() => {
                week0Change.props.forward();
            })

            expect(refetchSpy.mock.calls[0][0]).toEqual({
                week: mockQueryData.weeks[1].name
            })
        });

        it('changes pick cells to advanced week', () => {
            act(() => {
                week0Change.props.forward();
            })

            const cell = grid.findByProps({id: "pick-cells"});
            expect(cell.props.currentWeek).toBe(mockQueryData.weeks[1].name);
        });

        it('twice on week 0 refetches with week 2', () => {
            act(() => {
                week0Change.props.forward();
            })
            act(() => {
                week0Change.props.forward();
            })

            expect(refetchSpy.mock.calls[1][0]).toEqual({
                week: mockQueryData.weeks[2].name
            })
        });

        it('on week 0 updates displayed week', () => {
            const displayedWeek = week0Change.findByProps({id: "changeWeek-week"})

            act(() => {
                week0Change.props.forward();
            })

            expect(displayedWeek.children[0]).toContain(mockQueryData.weeks[1].name);
        });

        it('on week 1 refetches with week 2', () => {
            const week1Grid = create(<PicksGrid defaultWeek="1"/>).root;
            const changeWeek = week1Grid.findByProps({id: "change-week"})

            act(() => {
                changeWeek.props.forward();
            })

            expect(refetchSpy.mock.calls[0][0]).toEqual({
                week: mockQueryData.weeks[2].name
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
        let week2Grid;
        let week2Change;

        beforeEach(() => {
            week2Grid = create(<PicksGrid defaultWeek="2"/>).root;
            week2Change = week2Grid.findByProps({id: "change-week"});
        })

        it('on week 2 refetches with week 1', () => {
            act(() => {
                week2Change.props.back();
            })

            expect(refetchSpy.mock.calls[0][0]).toEqual({
                week: mockQueryData.weeks[1].name
            })
        });

        it('twice on week 2 refetches with week 0', () => {
            act(() => {
                week2Change.props.back();
            })
            act(() => {
                week2Change.props.back();
            })

            expect(refetchSpy.mock.calls[1][0]).toEqual({
                week: mockQueryData.weeks[0].name
            })
        });

        it('on week 2 updates displayed week', () => {
            const displayedWeek = week2Change.findByProps({id: "changeWeek-week"})

            act(() => {
                week2Change.props.back();
            })

            expect(displayedWeek.children[0]).toContain(mockQueryData.weeks[1].name);
        });

        it('on week 1 refetches with week 0', () => {
            const week1Grid = create(<PicksGrid defaultWeek="1"/>).root;
            const changeWeek = week1Grid.findByProps({id: "change-week"})

            act(() => {
                changeWeek.props.back();
            })

            expect(refetchSpy.mock.calls[0][0]).toEqual({
                week: mockQueryData.weeks[0].name
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
