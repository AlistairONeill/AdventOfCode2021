package aoc.day01

import java.io.File

fun day1() {
    File("data/2021/real/01_01.txt")
        .readLines()
        .map(String::toInt)
        .zipWithNext()
        .count { (left, right) -> right > left }
        .let(::println)

    File("data/2021/real/01_01.txt")
        .readLines()
        .map(String::toInt)
        .windowed(3, transform = List<Int>::sum)
        .zipWithNext()
        .count { (left, right) -> right > left }
        .let(::println)
}
