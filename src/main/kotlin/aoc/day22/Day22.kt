package aoc.day22

import aoc.AdventOfCodeDay
import aoc.day08.Input
import java.io.File
import java.math.BigInteger
import kotlin.Int.Companion.MAX_VALUE
import kotlin.Int.Companion.MIN_VALUE

object Day22 : AdventOfCodeDay {
    override fun String.solve() =
        lines()
            .map(Command::parse)
            .run { part1() to part2() }

    override val day = "22"
    override val test = "474140" to "2758514936282235"
    override val solution = "650099" to "1254011191104293"

}

//Note that this is exclusive
data class Cuboid(
    val xStart: Int,
    val xEnd: Int,
    val yStart: Int,
    val yEnd: Int,
    val zStart: Int,
    val zEnd: Int
) {
    fun volume(): BigInteger =
        BigInteger.valueOf(xEnd - xStart + 0L) *
                BigInteger.valueOf(yEnd - yStart + 0L) *
                BigInteger.valueOf(zEnd - zStart + 0L)

    fun contains(other: Cuboid): Boolean =
        xStart <= other.xStart &&
                yStart <= other.yStart &&
                zStart <= other.zStart &&
                xEnd >= other.xEnd &&
                yEnd >= other.yEnd &&
                zEnd >= other.zEnd

    private fun remainder(other: Cuboid): List<Cuboid> =
        when {
            other.contains(this) -> emptyList()
            other.disjoint(this) -> listOf(this)
            else -> setOf(xStart, xEnd, other.xStart, other.xEnd)
                .sorted()
                .zipWithNext()
                .flatMap { (xStart, xEnd) ->
                    setOf(yStart, yEnd, other.yStart, other.yEnd)
                        .sorted()
                        .zipWithNext()
                        .flatMap { (yStart, yEnd) ->
                            setOf(zStart, zEnd, other.zStart, other.zEnd)
                                .sorted()
                                .zipWithNext()
                                .map { (zStart, zEnd) ->
                                    Cuboid(xStart, xEnd, yStart, yEnd, zStart, zEnd)
                                }
                        }
                }
                .filter { contains(it) }
                .filter { !other.contains(it) }
        }

    private fun disjoint(other: Cuboid): Boolean =
        disjointX(other) || disjointY(other) || disjointZ(other)

    private fun disjointX(other: Cuboid): Boolean =
        xStart >= other.xEnd || other.xStart >= xEnd

    private fun disjointY(other: Cuboid): Boolean =
        yStart >= other.yEnd || other.yStart >= yEnd

    private fun disjointZ(other: Cuboid): Boolean =
        zStart >= other.zEnd || other.zStart >= zEnd

    fun remainder(others: Set<Cuboid>): List<Cuboid> {
        var cuboids = listOf(this)
        others.forEach { other ->
            cuboids = cuboids.flatMap { it.remainder(other) }
        }
        return cuboids
    }
}

private fun List<Command>.solve(): BigInteger {
    val used = HashSet<Cuboid>()
    var total = BigInteger.ZERO
    reversed()
        .forEach { command ->
            if (command.on) {
                command.cuboid.remainder(used).forEach { cuboid ->
                    total += cuboid.volume()
                }
            }
            used.add(command.cuboid)
        }
    return total
}

private fun List<Command>.part1(): String =
    (this + Command(false, Cuboid(50, MAX_VALUE, MIN_VALUE, MAX_VALUE, MIN_VALUE, MAX_VALUE)) +
            Command(false, Cuboid(MIN_VALUE, -50, MIN_VALUE, MAX_VALUE, MIN_VALUE, MAX_VALUE)) +
            Command(false, Cuboid(MIN_VALUE, MAX_VALUE, 50, MAX_VALUE, MIN_VALUE, MAX_VALUE)) +
            Command(false, Cuboid(MIN_VALUE, MAX_VALUE, MIN_VALUE, -50, MIN_VALUE, MAX_VALUE)) +
            Command(false, Cuboid(MIN_VALUE, MAX_VALUE, MIN_VALUE, MAX_VALUE, 50, MAX_VALUE)) +
            Command(false, Cuboid(MIN_VALUE, MAX_VALUE, MIN_VALUE, MAX_VALUE, MIN_VALUE, -50))
            ).solve().toString()

private fun List<Command>.part2(): String = solve().toString()

data class Command(
    val on: Boolean,
    val cuboid: Cuboid
) {
    companion object {
        private val regex =
            "(on|off) x=(-?\\d+)\\.\\.(-?\\d+),y=(-?\\d+)\\.\\.(-?\\d+),z=(-?\\d+)\\.\\.(-?\\d+)".toRegex()

        fun parse(input: String): Command =
            regex.matchEntire(input)
                ?.destructured
                ?.let { (mode, xStart, xEnd, yStart, yEnd, zStart, zEnd) ->
                    val on = when (mode) {
                        "on" -> true
                        "off" -> false
                        else -> error("Invalid mode [$mode]")
                    }

                    Command(
                        on,
                        Cuboid(
                            xStart.toInt(),
                            xEnd.toInt() + 1,
                            yStart.toInt(),
                            yEnd.toInt() + 1,
                            zStart.toInt(),
                            zEnd.toInt() + 1
                        )
                    )
                } ?: error("Failed to parse [$input]")
    }
}