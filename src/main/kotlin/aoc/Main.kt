package aoc

import aoc.day01.Day1
import aoc.day02.Day2
import aoc.day03.Day3
import aoc.day04.Day4
import aoc.day05.Day5
import aoc.day06.Day6
import aoc.day07.Day7
import aoc.day08.Day8
import aoc.day09.Day9
import aoc.day10.Day10
import aoc.day11.Day11
import aoc.day12.Day12
import aoc.day13.Day13
import aoc.day14.Day14
import aoc.day15.Day15
import aoc.day16.Day16
import aoc.day17.Day17
import aoc.day18.Day18
import aoc.day19.Day19
import aoc.day20.Day20
import aoc.day21.Day21
import aoc.day22.Day22
import java.io.File
import java.time.Duration
import java.time.Instant

fun allDays(): Sequence<AdventOfCodeDay> =
    sequenceOf(
        Day1,
        Day2,
        Day3,
        Day4,
        Day5,
        Day6,
        Day7,
        Day8,
        Day9,
        Day10,
        Day11,
        Day12,
        Day13,
        Day14,
        Day15,
        Day16,
        Day17,
        Day18,
        Day19,
        Day20,
        Day21,
        Day22
    )

fun main() {
    stats()
}

fun stats() {
    println("First run")
    println()
    println("/-------------------------\\")
    println("|Day|   Test   |   Real   |")
    println("|-------------------------|")

    allDays()
        .map(::perform)

    val firstTotal = allDays()
        .map(::perform)
        .onEach(DayTimings::print)
        .reduce(DayTimings::plus)
        .copy(day = "Sum")

    println("|-------------------------|")
    firstTotal.print()
    println("\\-------------------------/")

    println()
    println("Averages:")
    println()

    println("/-------------------------\\")
    println("|Day|   Test   |   Real   |")
    println("|-------------------------|")

    val averageTotal = allDays().map { day ->
        (0 until 10).map {
            perform(day)
        }
            .reduce(DayTimings::plus)
            .dividedBy(10)
    }.onEach(DayTimings::print)
        .reduce(DayTimings::plus)
        .copy(day = "Sum")

    println("|-------------------------|")
    averageTotal.print()
    println("\\-------------------------/")

}

private data class DayTimings(
    val day: String,
    val test: Duration,
    val real: Duration
) {
    fun print() {
        println("| $day|${test.format()}|${real.format()}|")
    }

    private fun Duration.format(): String =
        "${toMillis()} ms ".padStart(10, ' ')

    fun plus(other: DayTimings) =
        DayTimings(
            day,
            test + other.test,
            real + other.real
        )

    fun dividedBy(other: Long) =
        DayTimings(
            day,
            test.dividedBy(other),
            real.dividedBy(other)
        )
}

private fun perform(day: AdventOfCodeDay): DayTimings {
    val testInput = File("data/2021/test/${day.day}_01.txt").readText()
    val realInput = File("data/2021/real/${day.day}_01.txt").readText()
    return day.run {
        val (testTimes, test) = timeIt { testInput.solve() }
        if (test.first != day.test.first) println("Failed Test Part 1. Was ${test.first}")
        if (test.second != day.test.second) println("Failed Test Part 2. Was ${test.second}")
        val (realTimes, real) = timeIt { realInput.solve() }
        if (real.first != day.solution.first) println("Failed Real Part 1. Was ${real.first}")
        if (real.second != day.solution.second) println("Failed Real Part 2. Was ${real.second}")

        DayTimings(
            day.day,
            testTimes,
            realTimes
        )
    }
}

private fun <T> timeIt(block: () -> T): Pair<Duration, T> {
    val before = Instant.now()
    val ret = block()
    val after = Instant.now()
    return Duration.between(before, after) to ret
}