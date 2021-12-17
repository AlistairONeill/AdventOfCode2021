package aoc.day17

import java.io.File

fun day17() {
    File("data/2021/real/17_01.txt")
        .readText()
        .let(TargetRange.Companion::parse)
        .run {
            part1().let(::println)
            part2().let(::println)
        }
}

private fun TargetRange.part1() = startY * (startY+1) / 2

private fun TargetRange.part2(): Int {
    return (1 .. endX).cartesian((startY .. -startY))
        .map { (vX, vY) -> TargetRange.State(0, 0, vX, vY) }
        .count(this::willHitTarget)
}

private fun IntRange.cartesian(other: IntRange) : Sequence<Pair<Int, Int>> =
    asSequence().flatMap { x ->
        other.asSequence().map { y -> x to y }
    }

data class TargetRange(
    val startX: Int,
    val startY: Int,
    val endX: Int,
    val endY: Int
) {
    data class State(val x: Int, val y: Int, val vX: Int, val vY: Int) {
        fun progress() = State(
            x + vX,
            y + vY,
            if (vX > 0) vX-1 else vX,
            vY - 1
        )
    }

    companion object {
        private val regex = "target area: x=(-?\\d+)\\.\\.(-?\\d+), y=(-?\\d+)\\.\\.(-?\\d+)".toRegex()
        fun parse(input: String) =
            regex.matchEntire(input)?.destructured
                ?.let { (startX, endX, startY, endY) ->
                TargetRange(
                    startX.toInt(),
                    startY.toInt(),
                    endX.toInt(),
                    endY.toInt()
                )
            } ?: error("Failed to parse [$input]")
    }

    fun willHitTarget(state: State): Boolean =
        when {
            state.y < startY -> false
            state.x in (startX..endX) && state.y in (startY..endY) -> true
            else -> willHitTarget(state.progress())
        }
}