package aoc.day07

import aoc.AdventOfCodeDay
import java.io.File
import java.util.Collections.max
import java.util.Collections.min
import kotlin.math.abs

object Day7 : AdventOfCodeDay {
    override fun String.solve(): Pair<String, String> =
        split(",")
            .map(String::toInt)
            .run {
                fuelToAlign(::linear) to fuelToAlign(::triangle)
            }

    override val day = "07"
    override val test = "37" to "168"
    override val solution = "337488" to "89647695"

}

private fun List<Int>.fuelToAlign(fuelPerDistance: (Int) -> Int): String =
    (min(this)..max(this))
        .minOf { target -> sumOf { fuelPerDistance(abs(target - it)) } }
        .toString()

private fun linear(n: Int): Int = n
private fun triangle(n: Int): Int = n*(n+1)/2

