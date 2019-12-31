class UserDTO(var name: String) {
    var picks: ArrayList<PickDTO> = ArrayList(3);
    var total: Int = 0;

    init {
        picks.add(PickDTO("GB@CHI", "CHI"))
        picks.add(PickDTO("BUF@NE", "BUF"))
        picks.add(PickDTO("SEA@PHI", "SEA"))
    }
}
