import React from "react";
import {create, act} from "react-test-renderer";
import UserPickColumn from "./UserPickColumn";

describe('UserPickColumn', () => {
    it('correct pick has cell flagged correct', () => {
        let renderer = null;
        act(() => {
            renderer = create(<UserPickColumn
                games={[{name: "CHI@GB", result:"CHI"}]}
                pickSet={[{game: "CHI@GB", pick: "CHI"}]}
                user={{name: "someone"}}/>)
        });

        const cell = renderer.root.findByProps({id: "someone-CHI@GB"})

        expect(cell.props.correct).toBe(true)
    })

    it('correct pick is not case sensitive', () => {
        let renderer = null;
        act(() => {
            renderer = create(<UserPickColumn
                games={[{name: "CHI@GB", result:"CHI"}]}
                pickSet={[{game: "CHI@GB", pick: "cHi"}]}
                user={{name: "someone"}}/>)
        });

        const cell = renderer.root.findByProps({id: "someone-CHI@GB"})

        expect(cell.props.correct).toBe(true)
    })

    it('incorrect pick has cell flagged incorrect', () => {
        let renderer = null;
        act(() => {
            renderer = create(<UserPickColumn
                games={[{name: "CHI@GB", result:"CHI"}]}
                pickSet={[{game: "CHI@GB", pick: "GB"}]}
                user={{name: "someone"}}/>)
        });

        const cell = renderer.root.findByProps({id: "someone-CHI@GB"})

        expect(cell.props.correct).toBe(false)
    })

    it('null result flagged as not correct', () => {
        let renderer = null;
        act(() => {
            renderer = create(<UserPickColumn
                games={[{name: "CHI@GB"}]}
                pickSet={[{game: "CHI@GB", pick: "GB"}]}
                user={{name: "someone"}}/>)
        });

        const cell = renderer.root.findByProps({id: "someone-CHI@GB"})

        expect(cell.props.correct).toBe(false)
    })

    it('null pick flagged as not correct', () => {
        let renderer = null;
        act(() => {
            renderer = create(<UserPickColumn
                games={[{name: "CHI@GB", result: "GB"}]}
                pickSet={[{game: "CHI@GB"}]}
                user={{name: "someone"}}/>)
        });

        const cell = renderer.root.findByProps({id: "someone-CHI@GB"})

        expect(cell.props.correct).toBe(false)
    })

    it('null pick and result flagged as not correct', () => {
        let renderer = null;
        act(() => {
            renderer = create(<UserPickColumn
                games={[{name: "CHI@GB"}]}
                pickSet={[]}
                user={{name: "someone"}}/>)
        });

        const cell = renderer.root.findByProps({id: "someone-CHI@GB"})

        expect(cell.props.correct).toBe(false)
    })
})