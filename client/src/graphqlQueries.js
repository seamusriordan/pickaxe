import gql from "graphql-tag";

export const WEEKS_QUERY = gql`query Query {currentWeek {week} weeks}`;

export const PICKS_QUERY = gql`
    query Query($week: String) {
        users {
            name
        }

        userPicks(week: $week) {
            user { name }
            picks {
                game
                pick
            }
            total
        }

        games(week: $week) {
            week
            name
            spread
            result
        }
    }`;

export const UPDATE_PICKS_MUTATION =
gql`
    mutation Mutation($name: String!, $week: String!, $game: String!, $pick: String!) {
        updatePick(name: $name, userPick: { week: $week, game: $game, pick: $pick })
    }`;