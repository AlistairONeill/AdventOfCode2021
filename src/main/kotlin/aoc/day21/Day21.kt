package aoc.day21

import aoc.AdventOfCodeDay
import aoc.day21.P2GameState.GameWinState
import aoc.day21.P2GameState.MidGameState
import java.math.BigInteger
import java.math.BigInteger.ZERO

object Day21 : AdventOfCodeDay {
    override fun String.solve(): Pair<String, String> =
        part1() to part2()

    override val day = "21"
    override val test = "739785" to "444356092776315"
    override val solution = "678468" to "131180774190079"
}


fun String.part2() =
    let(P2GameState::parse)
        .let(::findUniverses)
        .let { (a, b) -> if (a > b) a else b }
        .toString()


sealed interface P2GameState {
    companion object {
        private val regex = "Player \\d starting position: (\\d+)".toRegex()

        fun parse(input: String): MidGameState =
            input.lines()
                .mapNotNull(regex::matchEntire)
                .map(MatchResult::destructured)
                .map(MatchResult.Destructured::component1)
                .map(String::toInt)
                .let { (p1, p2) -> MidGameState(p1, 0, p2, 0) }

        private val rollToSplit = listOf(
            3 to BigInteger.valueOf(1),
            4 to BigInteger.valueOf(3),
            5 to BigInteger.valueOf(6),
            6 to BigInteger.valueOf(7),
            7 to BigInteger.valueOf(6),
            8 to BigInteger.valueOf(3),
            9 to BigInteger.valueOf(1)
        )
    }

    object GameWinState : P2GameState

    data class MidGameState(
        val currentPosition: Int,
        val currentScore: Int,
        val otherPosition: Int,
        val otherScore: Int
    ) : P2GameState {

        fun takeTurn(): List<Pair<P2GameState, BigInteger>> =
            rollToSplit.map { (roll, split) ->
                val newPosition = ((currentPosition + roll - 1) % 10) + 1
                val newScore = currentScore + newPosition
                if (newScore < 21) {
                    MidGameState(
                        otherPosition,
                        otherScore,
                        newPosition,
                        newScore
                    )
                } else {
                    GameWinState
                } to split
            }
    }
}

private val cache = hashMapOf<MidGameState, Pair<BigInteger, BigInteger>>()

private fun findUniverses(state: MidGameState): Pair<BigInteger, BigInteger> =
    cache[state]
        ?: state.takeTurn().map { (state, split) ->
            when (state) {
                GameWinState -> split to ZERO
                is MidGameState -> {
                    val universes = findUniverses(state)
                    universes.second * split to universes.first * split
                }
            }
        }.reduce { acc, pair -> acc.first + pair.first to acc.second + pair.second }
            .also { cache[state] = it }


fun String.part1() =
    let(P1Game::parse)
        .solve()
        .toString()

private class P1Game(
    val playerPositions: IntArray
) {
    var dieRolls = 0
    var currentPlayer = 0
    val scores = IntArray(playerPositions.size) { 0 }

    companion object {
        private val regex = "Player \\d starting position: (\\d+)".toRegex()
        fun parse(input: String): P1Game =
            input.lines()
                .mapNotNull(regex::matchEntire)
                .map(MatchResult::destructured)
                .map(MatchResult.Destructured::component1)
                .map(String::toInt)
                .toIntArray()
                .let(::P1Game)
    }

    fun solve(): Int {
        var result: Int? = null
        while (result == null) {
            result = takeTurn()
        }
        return result
    }

    private fun roll(): Int = (dieRolls++ % 100) + 1

    private fun takeTurn(): Int? {
        val position = playerPositions[currentPlayer]
        val increment = roll() + roll() + roll()
        val newPosition = ((position + increment - 1) % 10) + 1
        playerPositions[currentPlayer] = newPosition
        val newScore = scores[currentPlayer] + newPosition
        scores[currentPlayer] = newScore
        currentPlayer += 1
        currentPlayer %= playerPositions.size
        return if (newScore >= 1000) {
            scores[currentPlayer] * dieRolls
        } else null
    }
}