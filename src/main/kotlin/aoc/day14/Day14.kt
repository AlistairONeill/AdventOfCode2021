package aoc.day14

import aoc.AdventOfCodeDay

object Day14 : AdventOfCodeDay {
    override fun String.solve(): Pair<String, String> =
        let(::PolymerisationSimulator)
            .run {
                solve(10) to solve(40)
            }

    private fun PolymerisationSimulator.solve(steps: Int) =
        getFinalCounts(steps)
            .run {
                maxOf { it } - minOf { it }
            }.toString()

    override val day = "14"
    override val test = "1588" to "2188189693529"
    override val solution = "3009" to "3459822539451"
}

class PolymerisationSimulator(
    input: String
) {
    private val knowledge = hashMapOf<Triple<Char, Char, Int>, ElementCounts>()
    private val start: List<Char>
    private val recipes : Map<Pair<Char, Char>, Char>

    init {
        val (start, rawRecipes) = input.split("\n\n")
        this.start = start.toList()

        recipes = rawRecipes.split("\n")
            .associate { line -> (line[0] to line[1]) to line[6] }
    }

    private fun getElementCounts(a: Char, b: Char, steps: Int): ElementCounts =
        knowledge[Triple(a, b, steps)]
            ?: if (steps == 0) {
                emptyMap()
            } else {
                val new = recipes[a to b]
                if (new == null) {
                    mapOf(a to 1L, b to 1L)
                } else {
                    combine(
                        getElementCounts(a, new, steps-1),
                        getElementCounts(new, b, steps-1),
                        new
                    )
                }
            }.also {
                knowledge[Triple(a, b, steps)] = it
            }

    private fun combine(count1: ElementCounts, count2: ElementCounts, dupe: Char): ElementCounts =
        (count1.keys.union(count2.keys) + dupe)
            .associateWith {
                (count1[it] ?: 0L) + (count2[it] ?: 0L) - if(dupe==it) 1L else 0L
            }

    fun getFinalCounts(steps: Int): Collection<Long> =
        start.zipWithNext()
            .map { (a, b) -> a to getElementCounts(a, b, steps) }
            .fold(
                mapOf(
                    start.first() to -1L,
                    start.last() to -1L
                )
            ) { acc, (dupe, counts) ->
                combine(
                    acc,
                    counts,
                    dupe
                )
            }
            .values
}

typealias ElementCounts = Map<Char, Long>
