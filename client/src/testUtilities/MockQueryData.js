export const mockQueryData = {
    "users": [
       { "name": "Davebob"},
        {"name": "Luuand"},
        {"name": "Vegas"}
    ],
    "userPicks": [
        {
            "user": {"name": "Davebob"}, "picks":
                [
                    {"game": "CHI@GB", "pick": "CHI"},
                    {"game": "HAR@NOR", "pick": "NOR"},
                    {"game": "SFE@CRL", "pick": "SFE"},
                    {"game": "ANN@COL", "pick": "ANN"},
                ],
        },
        {
            "user": {"name": "Luuand"}, "picks": [
                {"game": "CHI@GB", "pick": "GB"},
                {"game": "HAR@NOR", "pick": "NOR"},
                {"game": "SFE@CRL", "pick": "SF"},
                {"game": "ANN@COL", "pick": "C"},
            ],


        },
        {
            "user": {"name": "Vegas"}, "picks":
                [
                    {"game": "CHI@GB", "pick": "B"},
                    {"game": "HAR@NOR", "pick": "TH"},
                    {"game": "SFE@CRL", "pick": "CRL"},
                    {"game": "ANN@COL", "pick": "A"},
                ],
        },
    ],
    "userTotals":[
        {"name": "Davebob", "total": "4", "games": [ {"name": "HAR@NOR"}, {"name": "CHI@GB"}, {"name": "SFE@CRL"}, {"name": "ANN@COL"}]},
        {"name": "Luuand", "total": "1", "games": [ {"name": "HAR@NOR"}]},
        {"name": "Vegas", "total": "0", "games": []}
    ],
    "games": [
        {"name": "CHI@GB", "week": "0", "spread": 400, "result": "CHI"},
        {"name": "HAR@NOR", "week": "0", "spread": -22, "result": ""},
        {"name": "SFE@CRL", "week": "0", "spread": 7, "result": "CRL"},
        {"name": "ANN@COL", "week": "0", "spread": 0, "result": "CHI"},
    ],
    "weeks": [
        {"name": "0"},
        {"name": "1"},
        {"name": "2"},
    ]
};
