package aoc.day1

import java.io.File

fun day1() {
    File("data/2021/real/01_01.txt").readLines()
        .map(String::toInt)
        .zipWithNext()
        .count { (left, right) -> right > left }
        .let(::println)

    File("data/2021/real/01_01.txt").readLines()
        .map(String::toInt)
        .toRollingSum(3)
        .zipWithNext()
        .count { (left, right) -> right > left }
        .let(::println)
}

private fun List<Int>.toRollingSum(averageSize: Int) =
    (0 until size + 1 - averageSize).map {
        (it until it + averageSize).sumOf(this::get)
    }