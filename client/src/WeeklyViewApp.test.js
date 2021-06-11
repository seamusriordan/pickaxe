import WeeklyViewApp from "./WeeklyViewApp";

import {create, act} from "react-test-renderer";
import React from "react";
import {useQuery, useMutation} from '@apollo/react-hooks';
import {mockQueryData} from "./testUtilities/MockQueryData";

import {Leaderboard} from "./leaderboard/Leaderboard";
import {LeaderboardRow} from "./leaderboard/LeaderboardRow";


jest.mock('@apollo/react-hooks');


describe('WeeklyViewApp', () => {
    let app;
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
        let weeklyViewApp = null;
        act(() => {
            weeklyViewApp = create(<WeeklyViewApp defaultWeek="0"/>);
        })

        app = weeklyViewApp.root;
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
        create(<WeeklyViewApp defaultWeek={defaultWeek}/>).root;
        expect(useQuery.mock.calls[0][1].variables.week).toEqual(defaultWeek)
    });

    it('calls useQuery with with current week', () => {
        let weekChanger = app.findByProps({id: "change-week"});

        act(() => {
            weekChanger.props.forward();
        })

        expect(useQuery.mock.calls[1][1].variables.week).toEqual(mockQueryData.weeks[1].name)
    });

    it('Renders error when error from query is truthy', () => {
        useQuery.mockReturnValue({
            loading: false, error: true, data: undefined, refetch: () => {
            }
        });
        const grid = create(<WeeklyViewApp/>).root;

        expect(grid.findAll(el => el.props.children === 'Error').length).toEqual(1);
    });

    it('Renders Waiting for data when data from query is undefined', () => {
        useQuery.mockReturnValue({
            loading: false, error: undefined, data: undefined, refetch: () => {
            }
        });
        const grid = create(<WeeklyViewApp/>).root;

        expect(grid.findAll(el => el.props.children === 'Waiting for data...').length).toEqual(1);
    });

    describe('games app', () => {
        let gamesGrid;
        beforeEach(() => {
            gamesGrid = app.findByProps({"data-testid": "weekly-games-grid"})
        })

        it('passes default week by default', () => {
            expect(gamesGrid.props.currentWeek).toBe(app.props.defaultWeek)
        });

        it('passes users', () => {
            expect(gamesGrid.props.users).toBe(mockQueryData.users)
        });

        it('passes games', () => {
            expect(gamesGrid.props.games).toBe(mockQueryData.games)
        });

        it('passes picks', () => {
            expect(gamesGrid.props.userPicks).toBe(mockQueryData.userPicks)
        });

        it('passes totals', () => {
            expect(gamesGrid.props.totals).toBe(mockQueryData.userTotals)
        });
    });

    describe('advance week', () => {
        let week0Change;

        beforeEach(() => {
            week0Change = app.findByProps({id: "change-week"});
        })

        it('on week 0 refetches with week 1', () => {
            act(() => {
                week0Change.props.forward();
            })

            expect(refetchSpy.mock.calls[0][0]).toEqual({
                week: mockQueryData.weeks[1].name
            })
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
            const displayedWeek = week0Change.findByProps({id: "change-week--week"})

            act(() => {
                week0Change.props.forward();
            })

            expect(displayedWeek.children[0]).toContain(mockQueryData.weeks[1].name);
        });

        it('on week 1 refetches with week 2', () => {
            let week1Grid = null;
            act(() => {
                week1Grid = create(<WeeklyViewApp defaultWeek="1"/>);
            });
            act( () => {
                const changeWeek = week1Grid.root.findByProps({id: "change-week"})
                changeWeek.props.forward();
            })

            expect(refetchSpy.mock.calls[0][0]).toEqual({
                week: mockQueryData.weeks[2].name
            })
        });

        it('on final week does nothing', () => {
            let week2Grid = null;
            act(() => {
                week2Grid = create(<WeeklyViewApp defaultWeek="2"/>);
            });

            const changeWeek = week2Grid.root.findByProps({id: "change-week"})

            act(() => {
                changeWeek.props.forward();
            })

            expect(refetchSpy).not.toHaveBeenCalled();
        });
    });

    describe('rewind week', () => {
        let week2GridRoot;
        let week2Change;

        beforeEach(() => {
            let week2Grid = null;
            act(() => {
                week2Grid = create(<WeeklyViewApp defaultWeek="2"/>);
            })
            week2GridRoot = week2Grid.root;
            week2Change = week2GridRoot.findByProps({id: "change-week"});
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
            const displayedWeek = week2Change.findByProps({id: "change-week--week"})

            act(() => {
                week2Change.props.back();
            })

            expect(displayedWeek.children[0]).toContain(mockQueryData.weeks[1].name);
        });

        it('on week 1 refetches with week 0', () => {

            let week1Grid = null;
            act(() => {
                week1Grid = create(<WeeklyViewApp defaultWeek="1"/>);
            })
            const changeWeek = week1Grid.root.findByProps({id: "change-week"})

            act(() => {
                changeWeek.props.back();
            })

            expect(refetchSpy.mock.calls[0][0]).toEqual({
                week: mockQueryData.weeks[0].name
            })
        });

        it('on first week does nothing', () => {
            const changeWeek = app.findByProps({id: "change-week"})

            act(() => {
                changeWeek.props.back();
            })

            expect(refetchSpy).not.toHaveBeenCalled();
        });
    })

    describe('leaderboard', () => {
        it('has leaderboard', () => {
            const leaderboard = app.findAllByType(Leaderboard)

            expect(leaderboard).toHaveLength(1)
        })

        it('has row for each leader', () => {
            const rows = app.findByType(Leaderboard).findAllByType(LeaderboardRow)

            expect(rows).toHaveLength(mockQueryData.leaders.length)
        })
    });

});
