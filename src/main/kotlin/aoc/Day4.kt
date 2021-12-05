package aoc

import java.io.File


fun day4() {
    File("data/2021/real/04_01.txt")
        .readText()
        .let(BingoGame::parse)
        .runUntilWinner()
        .let(::println)

    File("data/2021/real/04_01.txt")
        .readText()
        .let(BingoGame::parse)
        .runUntilLastWinner()
        .let(::println)
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
                            .map(BingoBoard::parse)
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
                .map(BingoBoard::parseRow)
                .let(::BingoBoard)

        private fun parseRow(input: String) =
            input
                .split(" ")
                .filter(String::isNotBlank)
                .map(BingoSlot::parse)
    }

    private class BingoSlot(val value: Int) {
        var marked = false
        fun mark(num: Int) {
            if (num == value) {
                marked = true
            }
        }

        companion object {
            fun parse(input: String) = input.toInt().let(::BingoSlot)
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