package aoc.day20

import java.io.File

fun day20() {
    File("data/2021/real/20_01.txt")
        .readText()
        .let(String::parse)
        .let { (algo, start) ->
            (0 until 2)
                .fold(start) { acc, _ -> acc.apply(algo) }
                .lit
                .let(::println)

            (0 until 50)
                .fold(start) { acc, _ -> acc.apply(algo) }
                .lit
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

private data class State(
    private val data: List<List<Boolean>>,
    private val void: Boolean
) {
    companion object {
        fun parse(input: String): State =
            input.split("\n").map { line ->
                line.map(Char::toBool)
            }.let {
                State(it, false)
            }
    }

    private val width = data.first().size
    private val height = data.size
    val lit get() = data.sumOf { row -> row.count { it }}

    private fun get(x: Int, y: Int): Boolean =
        if (x >= 0 && y >= 0 && x < width && y < height) data[y][x]
        else void


    fun apply(algo: Algorithm): State {
        val newData = (-1 .. height).map { y ->
            (-1 .. width).map { x ->
                listOf(
                    get(x-1, y-1),
                    get(x, y-1),
                    get(x+1, y-1),
                    get(x-1, y),
                    get(x, y),
                    get(x+1, y),
                    get(x-1, y+1),
                    get(x, y+1),
                    get(x+1, y+1)
                ).let(algo::get)
            }
        }

        val newVoid = algo.get(List(9) { void })

        return State(newData, newVoid)
    }
}