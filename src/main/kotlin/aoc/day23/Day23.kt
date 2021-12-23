package aoc.day23

import aoc.day23.Amphipod.*
import java.io.File
import kotlin.Int.Companion.MAX_VALUE

fun day23() {
    File("data/2021/test/23_01.txt")
        .readText()
        .parseStartingState(false)
}

enum class Amphipod {
    A, B, C, D
}

private interface DwellingSubLocation<T: DwellingSubLocation<T>> {
    fun isSolved(): Boolean
    fun canEmit(): Triple<T, Amphipod, Int>?
    fun canAccept(amphipod: Amphipod): List<Pair<T, Int>>
}

private data class AmphipodDwellingState(
    val leftAlcove: AlcoveState,
    val dwellingA: AmphipodDwelling,
    val blockingAB: CorridorSlot,
    val dwellingB: AmphipodDwelling,
    val blockingBC: CorridorSlot,
    val dwellingC: AmphipodDwelling,
    val blockingCD: CorridorSlot,
    val dwellingD: AmphipodDwelling,
    val rightAlcove: AlcoveState,
) {
    private val all = listOf(
        leftAlcove,
        dwellingA,
        blockingAB,
        dwellingB,
        blockingBC,
        dwellingC,
        blockingCD,
        dwellingD,
        rightAlcove
    )

    fun isSolved(): Boolean = all.all(DwellingSubLocation<*>::isSolved)

    fun options(): List<Pair<AmphipodDwellingState, Int>> {
        TODO()
    }
}

data class CorridorSlot(
    val amphipod: Amphipod?
) : DwellingSubLocation<CorridorSlot> {
    override fun isSolved() = amphipod == null

    override fun canEmit(): Triple<CorridorSlot, Amphipod, Int>? =
        amphipod?.let {
            Triple(CorridorSlot(null), it, 0)
        }

    override fun canAccept(amphipod: Amphipod): List<Pair<CorridorSlot, Int>> =
        if (this.amphipod == null) {
            listOf(CorridorSlot(amphipod) to 0)
        } else emptyList()
}

data class AlcoveState(
    val inner: Amphipod?,
    val outer: Amphipod?
) : DwellingSubLocation<AlcoveState> {
    override fun isSolved() = inner == null && outer == null

    override fun canEmit(): Triple<AlcoveState, Amphipod, Int>? =
        when {
            outer != null -> Triple(AlcoveState(inner, null), outer, 1)
            inner != null -> Triple(AlcoveState(null, null), inner, 2)
            else -> null
        }

    override fun canAccept(amphipod: Amphipod): List<Pair<AlcoveState, Int>> =
        when {
            inner == null && outer == null -> listOf(
                AlcoveState(amphipod, null) to 2,
                AlcoveState(null, amphipod) to 1
            )
            outer == null -> listOf(
                AlcoveState(inner, amphipod) to 1
            )
            else -> emptyList()
        }
}

data class AmphipodDwelling(
    val intended: Amphipod,
    val size: Int,
    val actual: List<Amphipod>
) : DwellingSubLocation<AmphipodDwelling> {
    constructor(
        intended: Amphipod,
        actual: List<Amphipod>
    ): this(
        intended,
        actual.size,
        actual
    )

    override fun isSolved() = actual.size == size && actual.all { it == intended }
    override fun canEmit(): Triple<AmphipodDwelling, Amphipod, Int>? =
        when {
            actual.all { it == intended } -> null
            actual.isNotEmpty() -> Triple(copy(actual = actual.drop(1)), actual.first(), size - actual.size + 1)
            else -> null
        }

    override fun canAccept(amphipod: Amphipod): List<Pair<AmphipodDwelling, Int>> =
        when {
            amphipod != intended -> emptyList()
            actual.all { it == intended } -> listOf(copy(actual = listOf(amphipod) + actual) to (size - actual.size))
            else -> emptyList()
        }
}

private fun String.parseStartingState(part2: Boolean): AmphipodDwellingState =
    lines()
        .take(2)
        .drop(1)
        .map { it.split("#").filter(String::isNotEmpty) }
        .let { (top, bottom) ->
            AmphipodDwellingState(
                AlcoveState(null, null),
                AmphipodDwelling(A, listOf(valueOf(top[0]), valueOf(bottom[0]))),
                CorridorSlot(null),
                AmphipodDwelling(B, listOf(valueOf(top[1]), valueOf(bottom[1]))),
                CorridorSlot(null),
                AmphipodDwelling(C, listOf(valueOf(top[2]), valueOf(bottom[2]))),
                CorridorSlot(null),
                AmphipodDwelling(D, listOf(valueOf(top[3]), valueOf(bottom[3]))),
                AlcoveState(null, null)
            )
        }

private class Solver(initial: AmphipodDwellingState) {
    private val minValues = hashMapOf(initial to 0).withDefault { MAX_VALUE }
    private val interesting = hashSetOf(initial)

    fun solve(): Int {
        while (interesting.isNotEmpty()) {
            val toConsider = interesting.minByOrNull { minValues[it]!! }!!
            val current = minValues[toConsider]!!

            if (toConsider.isSolved()) {
                return current
            }

            interesting.remove(toConsider)

            toConsider.options().forEach { (state, cost) ->
                val old = minValues[state]!!
                val new = cost + current
                if (old == MAX_VALUE) {
                    interesting.add(state)
                    minValues[state] = new
                } else if (old > new) {
                    minValues[state] = new
                }
            }
        }

        error("Ruh Roh")
    }


}