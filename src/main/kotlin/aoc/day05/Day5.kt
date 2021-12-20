package aoc.day05

import aoc.AdventOfCodeDay

object Day5 : AdventOfCodeDay {
    override fun String.solve(): Pair<String, String> =
        lines()
            .map(HydrothermalLine::parse)
            .run { solve(HydrothermalLine::gridAligned) to solve { true } }

    private fun List<HydrothermalLine>.solve(filter: (HydrothermalLine) -> Boolean) =
        filter(filter)
            .flatMap(HydrothermalLine::points)
            .groupBy { it }
            .count { (_, points) -> points.size > 1 }
            .toString()

    override val day = "05"
    override val test = "5" to "12"
    override val solution = "6461" to "18065"
}

private data class Point(val x: Int, val y: Int)

private class HydrothermalLine(x1: Int, y1: Int, x2: Int, y2: Int) {
    companion object {
        fun parse(input: String) : HydrothermalLine {
            val split = input.replace(" -> ", ",").split(",").map(String::toInt)
            return HydrothermalLine(split[0], split[1], split[2], split[3])
        }

        private fun biDiRange(n1: Int, n2: Int) =
            if (n2 >= n1) n1 .. n2
            else (n2 .. n1).reversed()
    }

    val gridAligned: Boolean = x1 == x2 || y1 == y2
    val points: List<Point> =
        when {
            x1 == x2 -> biDiRange(y1, y2).map { Point(x1, it) }
            y1 == y2 -> biDiRange(x1, x2).map { Point(it, y1) }
            else -> biDiRange(x1, x2).zip(biDiRange(y1, y2), ::Point)
        }
}
