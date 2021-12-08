package aoc.day3

import java.io.File
import java.lang.RuntimeException

fun day3() {
    File("data/2021/real/03_01.txt")
        .readLines()
        .let(Collector::from)
        .run {
            println(powerConsumption)
            println(lifeSupportRating)
        }
}

private class Collector(private val lines: List<List<Boolean>>) {

    companion object {
        fun from(input: List<String>) = Collector(
            input.map { line ->
                line.map {
                    when (it) {
                        '1' -> true
                        '0' -> false
                        else -> throw RuntimeException("u wot m8")
                    }
                }
            }
        )
    }

    private val counts = Array(lines.first().size) { 0 }
    private val size = lines.size

    init {
        lines.forEach(::collect)
    }

    private fun collect(line: List<Boolean>) {
        line.forEachIndexed(::handle)
    }

    private fun handle(index: Int, value: Boolean) {
        if (value) counts[index] += 1
    }

    private fun mostCommon(index: Int) = 2 * counts[index] >= size
    private fun leastCommon(index: Int) = 2 * counts[index] < size

    private val gamma get() = counts.indices.map(::mostCommon).decimal
    private val epsilon get() = counts.indices.map(::leastCommon).decimal

    private fun applyFilter(predicate: Collector.(Int) -> Boolean, index : Int) : List<List<Boolean>> =
        lines.singleOrNull()?.let(::listOf)
            ?: lines.filter { it[index] == predicate(index) }.let(::Collector).applyFilter(predicate, index+1)

    private val oxygenRating get() = applyFilter(Collector::mostCommon, 0).single().decimal
    private val co2Rating get() = applyFilter(Collector::leastCommon, 0).single().decimal

    val lifeSupportRating get() = oxygenRating * co2Rating
    val powerConsumption get() = gamma * epsilon
}

private val List<Boolean>.decimal get() = joinToString("") { if (it) "1" else "0" }.toInt(2)