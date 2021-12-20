package aoc.day03

import aoc.AdventOfCodeDay
import java.lang.RuntimeException

object Day3: AdventOfCodeDay {
    override fun String.solve(): Pair<String, String> =
        lines()
            .let(Collector::from)
            .run {
                powerConsumption.toString() to lifeSupportRating.toString()
            }

    override val day = "03"
    override val test = "198" to "230"
    override val solution = "2967914" to "7041258"
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

    @Suppress("UNUSED_EXPRESSION")
    private fun applyFilter(predicate: Collector.(Int) -> Boolean, index : Int) : List<List<Boolean>> =
        lines.singleOrNull()?.let(::listOf)
            ?: lines.filter { predicate(index) == it[index] }.let(::Collector).applyFilter(predicate, index+1)

    private val oxygenRating get() = applyFilter(Collector::mostCommon, 0).single().decimal
    private val co2Rating get() = applyFilter(Collector::leastCommon, 0).single().decimal

    val lifeSupportRating get() = oxygenRating * co2Rating
    val powerConsumption get() = gamma * epsilon
}

private val List<Boolean>.decimal get() = joinToString("") { if (it) "1" else "0" }.toInt(2)