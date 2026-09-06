package jp.kaleidot725.pulse.demo.grid.area

/**
 * One quadrant of the 2x2 grid.
 *
 * [neighbors] is what makes a pulse spread rather than fill the screen: quadrants that share an edge
 * are neighbors, the diagonal one is not. A pulse therefore reaches three of the four areas.
 */
enum class AreaPosition(
    val label: String,
) {
    TopLeft("Top Left"),
    TopRight("Top Right"),
    BottomLeft("Bottom Left"),
    BottomRight("Bottom Right"),
    ;

    val neighbors: Set<AreaPosition>
        get() =
            when (this) {
                TopLeft -> setOf(TopRight, BottomLeft)
                TopRight -> setOf(TopLeft, BottomRight)
                BottomLeft -> setOf(TopLeft, BottomRight)
                BottomRight -> setOf(TopRight, BottomLeft)
            }
}
