import {useQuery} from "@apollo/react-hooks";
import {create} from "react-test-renderer";
import PicksGrid, {getPickByGame} from "./PicksGrid";
import React from "react";
import {mockQueryData} from "./MockQueryData";
import {findByClassName, assertAllUserPicksMatchCellText} from "./Helpers";
import PickCell from "./PickCell";

jest.mock('@apollo/react-hooks');
useQuery.mockReturnValue({loading: false, error: null, data: mockQueryData});

describe('PicksGrid pick cell rendering', () => {
    let grid, renderer;

    beforeEach(() => {
        jest.resetAllMocks();
        useQuery.mockReturnValue({loading: false, error: null, data: mockQueryData});

        renderer = create(<PicksGrid/>);
        grid = renderer.root;
    });

    it('Renders twelve pick cells when there are three users and four games in data response', () => {
        const pickCells = findByClassName(grid, 'pick-cell');

        expect(pickCells.length).toBe(mockQueryData.games.length * mockQueryData.users.length);

        assertAllUserPicksMatchCellText(mockQueryData, pickCells);
    });

    it('Pick cells are of PickCell type', () => {
        const pickCells = findByClassName(grid, 'pick-cell');

        pickCells.map(cell => expect(cell.type).toBe(PickCell))
    });

    it('can choose specific game from pick list for first mock user', () => {
        let picks = mockQueryData["users"][0].picks;

        expect(getPickByGame(picks, "CHI@GB")).toBe("CHI")
    })

    it('can choose specific game from pick list for second mock user', () => {
        let picks = mockQueryData["users"][1].picks;

        expect(getPickByGame(picks, "ANN@COL")).toBe("C")
    })

    it('empty list of picks returns null', () => {
        let emptyPicks = [];

        expect(getPickByGame(emptyPicks, "ANN@COL")).toBe(null)
    })


});
