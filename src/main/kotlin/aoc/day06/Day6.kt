package aoc.day06

import aoc.AdventOfCodeDay
import java.io.File
import java.math.BigInteger
import java.math.BigInteger.ZERO

object Day6: AdventOfCodeDay {
    override fun String.solve(): Pair<String, String> {
        val simulator = parse()
        simulator.simulate(80)
        val part1 = simulator.population
        simulator.simulate(256 - 80)
        val part2 = simulator.population
        return part1.toString() to part2.toString()
    }

    override val day = "06"
    override val test = "5934" to "26984457539"
    override val solution = "387413" to "1738377086345"
}

fun day6() {
    File("data/2021/real/06_01.txt")
        .readText()
        .parse()
        .apply { simulate(80) }
        .apply { println(population) }
        .apply { simulate(256 - 80) }
        .apply { println(population) }
}

private fun String.parse(): LakeState {
    val counts = split(",")
        .map(String::toInt)
        .groupBy { it }
        .mapValues { (_, value) -> BigInteger.valueOf(value.size.toLong()) }

    return (0 .. 8).map {
        counts[it] ?: ZERO
    }.let(::LakeState)
}


private class LakeState(
    private var birthingTimers: List<BigInteger>
) {
    fun simulate(days: Int) {
        repeat(days) {
            simulateDay()
        }
    }

    private fun simulateDay() {
        birthingTimers = listOf(
            birthingTimers[1],
            birthingTimers[2],
            birthingTimers[3],
            birthingTimers[4],
            birthingTimers[5],
            birthingTimers[6],
            birthingTimers[7] + birthingTimers[0],
            birthingTimers[8],
            birthingTimers[0]
        )
    }

    val population get() = birthingTimers.sumOf { it }
}
