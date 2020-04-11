import {create} from "react-test-renderer";
import ChangeWeek from "./ChangeWeek";
import React from "react";

describe('ChangeWeek Widget', () => {
    let changeWeek;

    beforeEach(() => {
        changeWeek = create(<ChangeWeek/>);
    })

    it('renders', () => {
        expect(changeWeek).toBeTruthy();
    });

    it('has a forward button', () => {
        const forwardButton = changeWeek.root.findAllByProps({id: "week-forward"})
        expect(forwardButton.length).toEqual(1)
    })

    it('has a backwards button', () => {
        const forwardButton = changeWeek.root.findAllByProps({id: "week-back"})
        expect(forwardButton.length).toEqual(1)
    })
});