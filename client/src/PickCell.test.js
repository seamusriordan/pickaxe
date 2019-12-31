import React from "react";
import {create, act} from "react-test-renderer";
import { render, fireEvent, waitForElement } from '@testing-library/react'
import PickCell from "./PickCell";


describe('PickCell', () => {
    it('takes user, game, and pick props and has pick as text', () => {
        let cellRenderer;
        act(() => {
            cellRenderer = create(<PickCell user="Some user" game="GB@CHI" pick="CHI"/>)
        });

        const wrapperChild = cellRenderer.root.children[0];
        expect(wrapperChild.props.children).toBe("CHI");
    });

    it('wrapper type is editable', () => {
        let cellRenderer;
        act(() => {
            cellRenderer = create(<PickCell user="Some user" game="GB@CHI" pick="CHI"/>)
        });

        const wrapperChild = cellRenderer.root.children[0];
        expect(wrapperChild.props.contentEditable).toBe(true)
    });

    it('pick for GB shows up as text as text', () => {
        let cellRenderer;
        act(() => {
            cellRenderer = create(<PickCell user="Some user" game="GB@CHI" pick="GB"/>)
        });

        let inputElement = cellRenderer.root.findByType('div');

        expect(inputElement.children[0]).toBe("GB")
    })

    it('calls sendData callback on keypress', () => {
        let wasPressed = false;
        let spyOnKeypress = () => {
            wasPressed = true
        };
        let {container} = render(<PickCell user="Some user" game="GB@CHI" pick="CHI"
                                     sendData={spyOnKeypress}/>);

        fireEvent.keyPress(container.querySelector('div'), {key: "Enter", keyCode: 13});

        expect(wasPressed).toBeTruthy();
    })

    it('calls sendData callback on mouse away', () => {
        let lostFocus = false;
        let spyOnLoseFocus = () => {
            lostFocus = true
        };
        let {container} = render(<PickCell user="Some user" game="GB@CHI" pick="CHI"
                                     sendData={spyOnLoseFocus}/>);

        fireEvent.blur(container.querySelector('div'));

        expect(lostFocus).toBeTruthy();
    })
});
