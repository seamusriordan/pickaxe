import {create} from "react-test-renderer";
import React from "react";
import {LeaderboardRow} from "./LeaderboardRow";
import {findByClassName} from "../testUtilities/Helpers";

describe('LeaderboardRow', () => {
    it('renders name', () => {
        const name = "Gandalf";
        const row = create(<LeaderboardRow name={name} weeks={0} picks={0}/>).root;
        expect(findByClassName(row, "leader-element-name")[0].children[0]).toBe(name)
    });

    it('renders different name', () => {
        const name = "Ogo";
        const row = create(<LeaderboardRow name={name} weeks={0} picks={0}/>).root;
        expect(findByClassName(row, "leader-element-name")[0].children[0]).toBe(name)
    });

    it('renders weeks of 0', () => {
        const row = create(<LeaderboardRow name="Gandalf" weeks={0} picks={0}/>).root;
        expect(findByClassName(row, "leader-correct-weeks")[0].children[0]).toBe("0")
    });

    it('renders weeks of 1', () => {
        const row = create(<LeaderboardRow name="Gandalf" weeks={1} picks={0}/>).root;
        expect(findByClassName(row, "leader-correct-weeks")[0].children[0]).toBe("1")
    });

    it('renders picks of 0', () => {
        const row = create(<LeaderboardRow name="Gandalf" weeks={0} picks={0}/>).root;
        expect(findByClassName(row, "leader-correct-weeks")[0].children[0]).toBe("0")
    });

    it('renders picks of 1', () => {
        const row = create(<LeaderboardRow name="Gandalf" weeks={1} picks={1}/>).root;
        expect(findByClassName(row, "leader-correct-weeks")[0].children[0]).toBe("1")
    });
});