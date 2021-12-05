package aoc

import java.io.File

fun day5() {
    perform(HydrothermalLine::gridAligned)
    perform { true }
}

private fun perform(filter: (HydrothermalLine) -> Boolean) =
    File("data/2021/real/05_01.txt")
        .readLines()
        .map(HydrothermalLine::parse)
        .filter(filter)
        .flatMap(HydrothermalLine::points)
        .groupBy{ it }
        .count { (_, points) -> points.size > 1 }
        .let(::println)

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
