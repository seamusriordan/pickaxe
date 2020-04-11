import {create} from "react-test-renderer";
import ChangeWeek from "./ChangeWeek";
import React from "react";

describe('ChangeWeek Widget', () => {
    let changeWeek;

    beforeEach(() => {
        changeWeek = create(<ChangeWeek week="0"/>);
    })

    it('renders', () => {
        expect(changeWeek).toBeTruthy();
    });

    it('has a forward button', () => {
        const forwardButton = changeWeek.root.findAllByProps({id: "changeWeek-forward"})
        expect(forwardButton.length).toEqual(1)
    })

    it('has a backwards button', () => {
        const backButton = changeWeek.root.findAllByProps({id: "changeWeek-back"})
        expect(backButton.length).toEqual(1)
    })

    it('displays the week 0 from props with week 0', () => {
        const weekElement = changeWeek.root.findByProps({id: "changeWeek-week"})
        expect(weekElement.children[0]).toEqual("0")
    })

    it('displays the week derp from props with week derp', () => {
        const changeWeekWith7 = create(<ChangeWeek week="derp"/>)

        const weekElement = changeWeekWith7.root.findByProps({id: "changeWeek-week"})
        expect(weekElement.children[0]).toEqual("derp")
    })
});