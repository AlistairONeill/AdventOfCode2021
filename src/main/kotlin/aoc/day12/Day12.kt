package aoc.day12

import aoc.AdventOfCodeDay

object Day12 : AdventOfCodeDay {
    override fun String.solve(): Pair<String, String> =
        lines()
            .let(::RouteSolver)
            .run {
                part1() to part2()
            }


    override val day = "12"
    override val test = "226" to "3509"
    override val solution = "4241" to "122134"
}


private class RouteSolver(input: List<String>) {

    private val map: Map<String, Set<String>> = HashMap<String, MutableSet<String>>()
        .also { map ->
            input
                .map { it.split("-") }
                .also {
                    it.flatten().toSet().forEach { here ->
                        map[here] = mutableSetOf()
                    }
                }
                .forEach { (here, there) ->
                    map[here]!!.add(there)
                    map[there]!!.add(here)
                }
        }


    private data class State(
        val currentNode: String,
        val history: Set<String>,
        val canRevisitSmall: Boolean
    )

    private val cache = HashMap<State, Int>()

    private fun State.getRouteCount(): Int =
        cache[this]
            ?: map[currentNode]!!.sumOf { target ->
                when {
                    target == "start" -> 0
                    target == "end" -> 1
                    target.all(Char::isUpperCase) -> copy(
                        currentNode = target,
                        history = history + target
                    ).getRouteCount()
                    target.all(Char::isLowerCase) -> if (history.contains(target)) {
                        if (canRevisitSmall) {
                            copy(
                                currentNode = target,
                                history = history + target,
                                canRevisitSmall = false
                            ).getRouteCount()
                        } else {
                            0
                        }
                    } else {
                        copy(currentNode = target, history = history + target).getRouteCount()
                    }
                    else -> error("This shouldn't be possible!")
                }
            }.also { cache[this] = it }

    fun newSolve(canRevisitSmall: Boolean) = State("start", emptySet(), canRevisitSmall).getRouteCount()

    fun part1(): String = newSolve(false).toString()
    fun part2(): String = newSolve(true).toString()
}

