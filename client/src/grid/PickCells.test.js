import {act, create} from "react-test-renderer";
import React from "react";
import PickCells from "./PickCells";
import {mockQueryData} from "../testUtilities/MockQueryData";
import {fireEvent, render} from "@testing-library/react";

describe('PickCells', () => {
    let sendDataSpy;
    beforeEach(() => {
        sendDataSpy = jest.fn();
    })

    it('PickCell sendData callback executes send with update on onBlur', () => {
        let grid = null;

        act(() => {
            grid = create(<PickCells data={mockQueryData} sendData={sendDataSpy} currentWeek="0"/>)
        });
        let cell = grid.root.find(el => el.props.id === "Vegas-HAR@NOR");

        act(() => {
            cell.children[0].props.onBlur({type: "onblur", target: {textContent: "THHH"}});
        });

        expect(sendDataSpy.mock.calls[0][0]).toEqual({
            variables: {
                name: "Vegas",
                week: "0",
                game: "HAR@NOR",
                pick: "THHH"
            }
        })
    });

    it('PickCell sendData callback executes send with update on onBlur with week 1', () => {
        let grid = null;

        act(() => {
            grid = create(<PickCells data={mockQueryData} sendData={sendDataSpy} currentWeek="1"/>)
        });
        let cell = grid.root.find(el => el.props.id === "Vegas-HAR@NOR");

        act(() => {
            cell.children[0].props.onBlur({type: "onblur", target: {textContent: "THHH"}});
        });

        expect(sendDataSpy.mock.calls[0][0]).toEqual({
            variables: {
                name: "Vegas",
                week: "1",
                game: "HAR@NOR",
                pick: "THHH"
            }
        })
    });

    it('PickCell send on pressing enter', () => {
        let grid = null;

        act(() => {
            grid = create(<PickCells data={mockQueryData} sendData={sendDataSpy} currentWeek="0"/>)
        });
        let cell = grid.root.find(el => el.props.id === "Davebob-CHI@GB");

        act(() => {
            cell.children[0].props.onBlur({type: "onkeypress", "keyCode": 13, target: {textContent: "GUB"}});
        });

        expect(sendDataSpy.mock.calls[0][0]).toEqual({
            variables: {
                name: "Davebob",
                week: "0",
                game: "CHI@GB",
                pick: "GUB"
            }
        })
    });

    describe('on fired blur event', () => {
        let container;
        beforeEach(() => {
            const renderResult = render(<PickCells data={mockQueryData} sendData={sendDataSpy} currentWeek="0"/>);
            container = renderResult.container;
        })


        it('sends data with cell InnerHTML', () => {
            let cell = container.querySelector('#Vegas-CHI\\@GB');

            act(() => {
                fireEvent.blur(cell, {target: {textContent: "CHI"}});
            });

            expect(sendDataSpy.mock.calls[0][0].variables.pick).toBe("CHI")
        });

        it(' do not send data when no change', () => {

            let {container} = render(<PickCells data={mockQueryData} sendData={sendDataSpy} currentWeek="0"/>);
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

            expect(sendDataSpy.mock.calls[0][0].variables.pick).toBe("CHI")
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