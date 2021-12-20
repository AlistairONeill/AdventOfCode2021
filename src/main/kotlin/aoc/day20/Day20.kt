package aoc.day20

import java.io.File

fun day20() {
    File("data/2021/real/20_01.txt")
        .readText()
        .let(String::parse)
        .let { (algo, start) ->
            var state = start
            repeat(2) { state = state.apply(algo) }
            println(state.lit)
            repeat(48) { state = state.apply(algo) }
            println(state.lit)
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

    fun get(index: Int) = data[index]

    fun handleVoid(void: Boolean) =
        if (void) {
            data.last()
        } else {
            data.first()
        }

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
        var i: Int
        val newData = (-1 .. height).map { y ->
            (-1 .. width).map { x ->
                i = 0
                if (get(x-1, y-1)) i+=1
                i*=2
                if (get(x, y-1)) i+=1
                i*=2
                if (get(x+1, y-1)) i+=1
                i*=2
                if (get(x-1, y)) i+=1
                i*=2
                if (get(x, y)) i+=1
                i*=2
                if (get(x+1, y)) i+=1
                i*=2
                if (get(x-1, y+1)) i+=1
                i*=2
                if (get(x, y+1)) i+=1
                i*=2
                if (get(x+1, y+1)) i+=1
                algo.get(i)
            }
        }

        val newVoid = algo.handleVoid(void)

        return State(newData, newVoid)
    }
}