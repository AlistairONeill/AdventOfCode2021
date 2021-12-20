package aoc.day20

import java.io.File

fun day20() {
    File("data/2021/real/20_01.txt")
        .readText()
        .let(String::parse)
        .let { (algo, start) ->
            (0 until 2)
                .fold(start) { acc, _ -> acc.apply(algo) }
                .points
                .size
                .let(::println)

            (0 until 50)
                .fold(start) { acc, _ -> acc.apply(algo) }
                .points
                .size
                .let(::println)
        }

}

private fun String.parse(): Pair<Algorithm, State> =
    split("\n\n").let { (algo, start) ->
        Algorithm.parse(algo) to State.parse(start)
    }

private class Algorithm(private val data: List<Boolean>) {
    companion object {
        fun parse(input: String): Algorithm =
            input.map(Char::toBool).let(::Algorithm)
    }

    private val cache = mutableMapOf<List<Boolean>, Boolean>()

    fun get(signature: List<Boolean>): Boolean =
        cache[signature]
            ?: signature.toInt()
                .let(data::get)
                .also { cache[signature] = it }
}

private fun List<Boolean>.toInt() : Int = joinToString("") { if (it) "1" else "0" }.toInt(2)

private fun Char.toBool() =
    when (this) {
        '.' -> false
        '#' -> true
        else -> error("Invalid Algorithm character")
    }

private typealias Point = Pair<Int, Int>

private data class State(
    private val minX: Int,
    private val minY: Int,
    private val maxX: Int,
    private val maxY: Int,
    val points: Set<Point>,
    private val void: Boolean
) {
    companion object {
        fun parse(input: String): State =
            input.split("\n").map { line ->
                line.map(Char::toBool)
            }.run {
                State(
                    0,
                    0,
                    first().size - 1,
                    size - 1,
                    flatMapIndexed { y, row ->
                        row.mapIndexedNotNull { x, lit ->
                            if (lit) Point(x, y) else null
                        }
                    }.toSet(),
                    false
                )
            }
    }

    private val cache = mutableMapOf<Point, Boolean>()

    private fun get(point: Point): Boolean =
        cache[point]
            ?: (if (point.first in minX..maxX && point.second in minY..maxY) points.contains(point)
                else void)
                .also { cache[point] = it }


    fun apply(algo: Algorithm): State {
        val newMinX = minX - 1
        val newMaxX = maxX + 1
        val newMinY = minY - 1
        val newMaxY = maxY + 1

        val newPoints = (newMinX..newMaxX).flatMap { x ->
            (newMinY..newMaxY).map { y ->
                (x to y) to listOf(
                    get(x-1 to y-1),
                    get(x to y-1),
                    get(x+1 to y-1),
                    get(x-1 to y),
                    get(x to y),
                    get(x+1 to y),
                    get(x-1 to y+1),
                    get(x to y+1),
                    get(x+1 to y+1)
                )
            }
        }.filter { algo.get(it.second) }
            .map { it.first }
            .toSet()

        val newVoid = algo.get(List(9) { void })

        return State(
            newMinX,
            newMinY,
            newMaxX,
            newMaxY,
            newPoints,
            newVoid
        )
    }
}