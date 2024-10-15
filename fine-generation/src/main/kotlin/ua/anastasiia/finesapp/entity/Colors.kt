package ua.anastasiia.finesapp.entity

enum class Colors {
    WHITE,
    BLACK,
    SILVER,
    RED,
    BLUE,
    GREEN,
    BROWN,
    YELLOW,
    UNKNOWN
}

fun adaptColor(color: String): Colors {
    return when (color) {
        "beige" -> Colors.SILVER
        "black" -> Colors.BLACK
        "darkblue" -> Colors.BLUE
        "brown" -> Colors.BROWN
        "green" -> Colors.GREEN
        "gray" -> Colors.SILVER
        "orange" -> Colors.RED
        "purple" -> Colors.BLACK
        "red" -> Colors.RED
        "white" -> Colors.WHITE
        "yellow" -> Colors.YELLOW
        else -> Colors.UNKNOWN
    }
}
