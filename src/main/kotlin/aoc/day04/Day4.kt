package aoc.day04

import aoc.AdventOfCodeDay

object Day4: AdventOfCodeDay {
    override fun String.solve(): Pair<String, String> =
        solve(BingoGame::runUntilWinner) to solve(BingoGame::runUntilLastWinner)

    private fun String.solve(fn: BingoGame.() -> Int) : String =
        let(BingoGame.Companion::parse)
            .fn()
            .toString()

    override val day = "04"
    override val test = "4512" to "1924"
    override val solution = "69579" to "14877"
}

class BingoGame private constructor(private val input: List<Int>, private val boards: MutableList<BingoBoard>) {
    companion object {
        fun parse(input: String) =
            input.split("\n\n")
                .run {
                    BingoGame(
                        first()
                            .split(",")
                            .map(String::toInt),
                        drop(1)
                            .map(BingoBoard.Companion::parse)
                            .toMutableList()
                    )
                }
    }

    fun runUntilWinner(): Int {
        for (num in input) {
            boards.forEach { it.mark(num) }
            winningBoard?.run { return unmarkedSum * num }
        }

        throw RuntimeException(":(")
    }

    fun runUntilLastWinner(): Int {
        for (num in input) {
            boards.forEach { it.mark(num) }
            if (boards.size > 1) {
                boards.removeAll(BingoBoard::hasWon)
            }

            winningBoard?.run { return unmarkedSum * num }
        }
        throw RuntimeException(":(")
    }

    private val winningBoard get() = boards.firstOrNull(BingoBoard::hasWon)
}

private class BingoBoard private constructor(private val data: List<List<BingoSlot>>) {
    companion object {
        fun parse(input: String) =
            input
                .split("\n")
                .map(Companion::parseRow)
                .let(::BingoBoard)

        private fun parseRow(input: String) =
            input
                .split(" ")
                .filter(String::isNotBlank)
                .map(BingoSlot.Companion::parse)
    }

    private class BingoSlot(val value: Int) {
        var marked = false
        fun mark(num: Int) {
            if (num == value) {
                marked = true
            }
        }

        companion object {
            fun parse(input: String) = input.toInt().let(BingoBoard::BingoSlot)
        }
    }

    private val winningCombinations = data + data[0].indices.map { i ->data.map { it[i] } }
    private val cells = data.flatten()

    fun mark(num: Int) {
        cells.forEach { it.mark(num) }
    }

    val hasWon get() = winningCombinations.any { it.all(BingoSlot::marked) }
    val unmarkedSum get() = cells.sumOf { if (it.marked) 0 else it.value }
}