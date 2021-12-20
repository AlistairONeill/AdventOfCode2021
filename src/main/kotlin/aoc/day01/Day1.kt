package aoc.day01

import aoc.AdventOfCodeDay

object Day1 : AdventOfCodeDay {
    override fun String.solve(): Pair<String, String> =
        lines()
            .map(String::toInt)
            .run {
                part1() to part2()
            }

    private fun List<Int>.part1() =
        zipWithNext()
            .count { (left, right) -> right > left }
            .toString()


    private fun List<Int>.part2() =
        windowed(3, transform = List<Int>::sum)
            .part1()

    override val day = "01"
    override val test = "7" to "5"

    override val solution = "1559" to "1600"
}

