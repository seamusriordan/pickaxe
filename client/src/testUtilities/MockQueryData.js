export const mockQueryData = {
    "users": [
       { "name": "Davebob"},
        {"name": "Luuand"},
        {"name": "Vegas"}
    ],
    "userPicks": [
        {
            "user": {"name": "Davebob"}, "total": "0", "picks":
                [
                    {"game": "CHI@GB", "pick": "CHI"},
                    {"game": "HAR@NOR", "pick": "NOR"},
                    {"game": "SFE@CRL", "pick": "SFE"},
                    {"game": "ANN@COL", "pick": "ANN"},
                ],
        },
        {
            "user": {"name": "Luuand"}, "total": "3", "picks": [
                {"game": "CHI@GB", "pick": "GB"},
                {"game": "HAR@NOR", "pick": "NOR"},
                {"game": "SFE@CRL", "pick": "SF"},
                {"game": "ANN@COL", "pick": "C"},
            ],


        },
        {
            "user": {"name": "Vegas"}, "total": "1", "picks":
                [
                    {"game": "CHI@GB", "pick": "B"},
                    {"game": "HAR@NOR", "pick": "TH"},
                    {"game": "SFE@CRL", "pick": "CRL"},
                    {"game": "ANN@COL", "pick": "A"},
                ],
        },
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
