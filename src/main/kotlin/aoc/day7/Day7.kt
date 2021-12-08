package aoc.day7

import java.io.File
import java.util.Collections.max
import java.util.Collections.min
import kotlin.math.abs

fun day7() {
    File("data/2021/real/07_01.txt")
        .readText()
        .split(",")
        .map(String::toInt)
        .run {
            fuelToAlign(::linear).let(::println)
            fuelToAlign(::triangle).let(::println)
        }
}

private fun List<Int>.fuelToAlign(fuelPerDistance: (Int) -> Int): Int =
    (min(this)..max(this))
        .minOf { target -> sumOf { fuelPerDistance(abs(target - it)) } }

private fun linear(n: Int): Int = n
private fun triangle(n: Int): Int = n*(n+1)/2

