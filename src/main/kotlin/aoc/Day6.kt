package aoc

import java.io.File
import java.math.BigInteger
import java.math.BigInteger.ZERO

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
