import {act, create} from "react-test-renderer";
import React from "react";
import UserPicksGrid from "./UserPicksGrid";
import {mockQueryData} from "../testUtilities/MockQueryData";
import {fireEvent, render} from "@testing-library/react";

describe('UserPicksGrid', () => {
    let sendDataSpy;
    beforeEach(() => {
        sendDataSpy = jest.fn();
    })

    it('PickCell sendData callback executes send with update on onBlur', () => {
        let grid = null;

        act(() => {
            grid = create(<UserPicksGrid
                users={mockQueryData.users}
                games={mockQueryData.games}
                userPicks={mockQueryData.userPicks}
                sendData={sendDataSpy}
            />)
        });
        let cell = grid.root.find(el => el.props.id === "Vegas-HAR@NOR");

        act(() => {
            cell.children[0].props.onBlur({type: "onblur", target: {textContent: "THHH"}});
        });

        expect(sendDataSpy.mock.calls[0][0]).toEqual("Vegas")
        expect(sendDataSpy.mock.calls[0][1]).toEqual("HAR@NOR")
        expect(sendDataSpy.mock.calls[0][2]).toEqual("THHH")
    });


    it('PickCell send on pressing enter', () => {
        let grid = null;

        act(() => {
            grid = create(<UserPicksGrid
                users={mockQueryData.users}
                games={mockQueryData.games}
                userPicks={mockQueryData.userPicks}
                sendData={sendDataSpy}/>)
        });
        let cell = grid.root.find(el => el.props.id === "Davebob-CHI@GB");

        act(() => {
            cell.children[0].props.onBlur({type: "onkeypress", "keyCode": 13, target: {textContent: "GUB"}});
        });

        expect(sendDataSpy.mock.calls[0][0]).toEqual("Davebob")
        expect(sendDataSpy.mock.calls[0][1]).toEqual("CHI@GB")
        expect(sendDataSpy.mock.calls[0][2]).toEqual("GUB")
    });

    describe('on fired blur event', () => {
        let container;
        beforeEach(() => {
            const renderResult = render(<UserPicksGrid
                users={mockQueryData.users}
                games={mockQueryData.games}
                userPicks={mockQueryData.userPicks}
                sendData={sendDataSpy}/>);
            container = renderResult.container;
        })


        it('sends data with cell InnerHTML', () => {
            let cell = container.querySelector('#Vegas-CHI\\@GB');

            act(() => {
                fireEvent.blur(cell, {target: {textContent: "CHI"}});
            });

            expect(sendDataSpy.mock.calls[0][2]).toBe("CHI")
        });

        it(' do not send data when no change', () => {

            let {container} = render(<UserPicksGrid
                users={mockQueryData.users}
                games={mockQueryData.games}
                userPicks={mockQueryData.userPicks}
                sendData={sendDataSpy}/>);
            let cell = container.querySelector('#Vegas-CHI\\@GB');

            act(() => {
                fireEvent.blur(cell, {target: {textContent: "B"}});
            });

            expect(sendDataSpy.mock.calls.length).toEqual(0);
        });

        it('textContent with newlines only sends up to first newline', () => {
            let cell = container.querySelector('#Vegas-CHI\\@GB');

            act(() => {
                fireEvent.blur(cell, {target: {textContent: "CHI\nall this other data"}});
            });

            expect(sendDataSpy.mock.calls[0][2]).toBe("CHI")
        });

        it('innerHTML from textContent with newlines only have up to first newline', () => {
            let cell = container.querySelector('#Vegas-CHI\\@GB');

            act(() => {
                fireEvent.blur(cell, {target: {textContent: "CHI\nall this other data"}});
            });

            cell = container.querySelector('#Vegas-CHI\\@GB');
            expect(cell.textContent).toBe("CHI")
        });
    });
});