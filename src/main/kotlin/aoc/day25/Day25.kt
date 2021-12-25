package aoc.day25

import aoc.AdventOfCodeDay

object Day25 : AdventOfCodeDay {
    override fun String.solve(): Pair<String, String> =
    parseCucumberState()
    .let {
        var state = it
        var moves = 1
        var new = state.move()
        while (state != new) {
            moves += 1
            state = new
            new = state.move()
        }
        moves.toString()
    } to ""

    override val day = "25"
    override val test = "58" to ""
    override val solution = "598" to ""
}

private fun String.parseCucumberState(): List<List<Boolean?>> =
    lines().map { line ->
        line.map { char ->
            when (char) {
                '.' -> null
                '>' -> true
                'v' -> false
                else -> error("Invalid symbol found")
            }
        }
    }

private fun List<List<Boolean?>>.move(): List<List<Boolean?>> =
    moveEast().moveSouth()

private fun List<List<Boolean?>>.moveEast(): List<List<Boolean?>> =
    mapIndexed { y, row ->
        row.mapIndexed { x, value ->
            when (value) {
                true -> if (get(y)[(x+1)%first().size] == null) null else true
                false -> false
                null -> if (get(y)[(x+first().size-1)%first().size] == true) true else null
            }
        }
    }

private fun List<List<Boolean?>>.moveSouth(): List<List<Boolean?>> =
    mapIndexed { y, row ->
        row.mapIndexed { x, value ->
            when (value) {
                true -> true
                false -> if (get((y+1)%size)[x] == null) null else false
                null -> if (get((y+size-1)%size)[x] == false) false else null
            }
        }
    }