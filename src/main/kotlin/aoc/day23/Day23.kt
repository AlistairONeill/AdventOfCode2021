package aoc.day23

import aoc.AdventOfCodeDay
import aoc.day23.Amphipod.*
import java.io.File

object Day23: AdventOfCodeDay {
    override fun String.solve(): Pair<String, String> =
        part1() to part2()

    private fun String.part1(): String = parseStartingState(false).solve()
    private fun String.part2(): String = parseStartingState(true).solve()

    private fun AmphipodDwellingState.solve(): String =
        let(::Solver)
            .solve()
            .toString()

    override val day = "23"
    override val test = "12521" to "44169"
    override val solution = "19160" to "47232"
}

enum class Amphipod(val cost: Int) {
    A(1),
    B(10),
    C(100),
    D(1000)
}

private interface DwellingHomeLocation {
    val accepting: Boolean
    val emitting: Boolean
    val solved: Boolean
    fun canEmit(): Triple<DwellingHomeLocation, Amphipod, Int>
    fun accept(travelCost: Int, createState: (DwellingHomeLocation) -> AmphipodDwellingState): Pair<AmphipodDwellingState, Int>

    companion object {
        fun factory(intended: Amphipod, vararg actual: Amphipod): DwellingHomeLocation =
            if (actual.all { it == intended }) AmphipodCompleteDwelling
            else AmphipodMixedDwelling(intended, actual.toList())
    }
}


private data class AmphipodDwellingState(
    val farLeft: Amphipod?,
    val left: Amphipod?,
    val dwellingA: DwellingHomeLocation,
    val blockingAB: Amphipod?,
    val dwellingB: DwellingHomeLocation,
    val blockingBC: Amphipod?,
    val dwellingC: DwellingHomeLocation,
    val blockingCD: Amphipod?,
    val dwellingD: DwellingHomeLocation,
    val right: Amphipod?,
    val farRight: Amphipod?,
) {
    fun isSolved() =
        dwellingA.solved &&
                dwellingB.solved &&
                dwellingC.solved &&
                dwellingD.solved

    private val fl = farLeft == null
    private val l = left == null
    private val a = dwellingA.accepting
    private val ab = blockingAB == null
    private val b = dwellingB.accepting
    private val bc = blockingBC == null
    private val c = dwellingC.accepting
    private val cd = blockingCD == null
    private val d = dwellingD.accepting
    private val r = right == null
    private val fr = farRight == null

    fun options(): List<Pair<AmphipodDwellingState, Int>> =
        tryClearHallways()?.let(::listOf)
            ?: ArrayList<Pair<AmphipodDwellingState, Int>>().also {
                addDwellingAOptions(it)
                addDwellingBOptions(it)
                addDwellingCOptions(it)
                addDwellingDOptions(it)
            }

    private fun tryClearHallways(): Pair<AmphipodDwellingState, Int>? =
        if (dwellingA is AmphipodReceivingDwelling || dwellingB is AmphipodReceivingDwelling || dwellingC is AmphipodReceivingDwelling || dwellingD is AmphipodReceivingDwelling) {
            tryClearFarLeft()
                ?: tryClearLeft()
                ?: tryClearAB()
                ?: tryClearBC()
                ?: tryClearCD()
                ?: tryClearRight()
                ?: tryClearFarRight()
        } else null

    private fun tryClearFarLeft(): Pair<AmphipodDwellingState, Int>? =
        farLeft?.let { amphipod ->
            val base = copy(farLeft = null)
            when (amphipod) {
                A -> if (l&&a) dwellingA.accept(2) { base.copy(dwellingA = it) } else null
                B -> if (l&&ab&&b) dwellingB.accept(4) { base.copy(dwellingB = it) } else null
                C -> if (l&&ab&&bc&&c) dwellingC.accept(6) { base.copy(dwellingC = it) } else null
                D -> if (l&&ab&&bc&&cd&&d) dwellingD.accept(8) { base.copy(dwellingD = it) } else null
            }
        }

    private fun tryClearLeft(): Pair<AmphipodDwellingState, Int>? =
        left?.let { amphipod ->
            val base = copy(left = null)
            when (amphipod) {
                A -> if (a) dwellingA.accept(1) { base.copy(dwellingA = it) } else null
                B -> if (ab&&b) dwellingB.accept(3) { base.copy(dwellingB = it) } else null
                C -> if (ab&&bc&&c) dwellingC.accept(5) { base.copy(dwellingC = it) } else null
                D -> if (ab&&bc&&cd&&d) dwellingD.accept(7) { base.copy(dwellingD = it) } else null
            }
        }

    private fun tryClearAB(): Pair<AmphipodDwellingState, Int>? =
        blockingAB?.let { amphipod ->
            val base = copy(blockingAB = null)
            when (amphipod) {
                A -> if (a) dwellingA.accept(1) { base.copy(dwellingA = it) } else null
                B -> if (b) dwellingB.accept(1) { base.copy(dwellingB = it) } else null
                C -> if (bc&&c) dwellingC.accept(3) { base.copy(dwellingC = it) } else null
                D -> if (bc&&cd&&d) dwellingD.accept(5) { base.copy(dwellingD = it) } else null
            }
        }

    private fun tryClearBC(): Pair<AmphipodDwellingState, Int>? =
        blockingBC?.let { amphipod ->
            val base = copy(blockingBC = null)
            when (amphipod) {
                A -> if (ab&&a) dwellingA.accept(3) { base.copy(dwellingA = it) } else null
                B -> if (b) dwellingB.accept(1) { base.copy(dwellingB = it) } else null
                C -> if (c) dwellingC.accept(1) { base.copy(dwellingC = it) } else null
                D -> if (cd&&d) dwellingD.accept(3) { base.copy(dwellingD = it) } else null
            }
        }

    private fun tryClearCD(): Pair<AmphipodDwellingState, Int>?  =
        blockingCD?.let { amphipod ->
            val base = copy(blockingCD = null)
            when (amphipod) {
                A -> if (ab&&bc&&a) dwellingA.accept(5) { base.copy(dwellingA = it) } else null
                B -> if (bc&&b) dwellingB.accept(3) { base.copy(dwellingB = it) } else null
                C -> if (c) dwellingC.accept(1) { base.copy(dwellingC = it) } else null
                D -> if (d) dwellingD.accept(1) { base.copy(dwellingD = it) } else null
            }
        }

    private fun tryClearRight(): Pair<AmphipodDwellingState, Int>? =
        right?.let { amphipod ->
            val base = copy(right = null)
            when (amphipod) {
                A -> if (ab&&bc&&cd&&a) dwellingA.accept(7) { base.copy(dwellingA = it) } else null
                B -> if (bc&&cd&&b) dwellingB.accept(5) { base.copy(dwellingB = it) } else null
                C -> if (cd&&c) dwellingC.accept(3) { base.copy(dwellingC = it) } else null
                D -> if (d) dwellingD.accept(1) { base.copy(dwellingD = it) } else null
            }
        }

    private fun tryClearFarRight(): Pair<AmphipodDwellingState, Int>? =
        farRight?.let { amphipod ->
            val base = copy(farRight = null)
            when (amphipod) {
                A -> if (r&&ab&&bc&&cd&&a) dwellingA.accept(8) { base.copy(dwellingA = it) } else null
                B -> if (r&&bc&&cd&&b) dwellingB.accept(6) { base.copy(dwellingB = it) } else null
                C -> if (r&&cd&&c) dwellingC.accept(4) { base.copy(dwellingC = it) } else null
                D -> if (r&&d) dwellingD.accept(2) { base.copy(dwellingD = it) } else null
            }
        }

    private fun addDwellingAOptions(list: MutableList<Pair<AmphipodDwellingState, Int>>) {
        if (!dwellingA.emitting) return
        dwellingA.canEmit().let { (newDwelling, amphipod, emitCost) ->
            val base = copy(dwellingA = newDwelling)
            if (l&&fl) list.add(base.copy(farLeft = amphipod) to (emitCost+2)*amphipod.cost)
            if (l) list.add(base.copy(left = amphipod) to (emitCost+1)*amphipod.cost)
            if (ab) list.add(base.copy(blockingAB = amphipod) to (emitCost+1)*amphipod.cost)
            if (ab&&bc) list.add(base.copy(blockingBC = amphipod) to (emitCost+3)*amphipod.cost)
            if (ab&&bc&&cd) list.add(base.copy(blockingCD = amphipod) to (emitCost+5)*amphipod.cost)
            if (ab&&bc&&cd&&r) list.add(base.copy(right = amphipod) to (emitCost+7)*amphipod.cost)
            if (ab&&bc&&cd&&r&&fr) list.add(base.copy(farRight = amphipod) to (emitCost+8)*amphipod.cost)
        }
    }

    private fun addDwellingBOptions(list: MutableList<Pair<AmphipodDwellingState, Int>>) {
        if (!dwellingB.emitting) return
        dwellingB.canEmit()?.let { (newDwelling, amphipod, emitCost) ->
            val base = copy(dwellingB = newDwelling)
            if (l&&ab&&fl) list.add(base.copy(farLeft = amphipod) to (emitCost+4)*amphipod.cost)
            if (l&&ab) list.add(base.copy(left = amphipod) to (emitCost+3)*amphipod.cost)
            if (ab) list.add(base.copy(blockingAB = amphipod) to (emitCost+1)*amphipod.cost)
            if (bc) list.add(base.copy(blockingBC = amphipod) to (emitCost+1)*amphipod.cost)
            if (bc&&cd) list.add(base.copy(blockingCD = amphipod) to (emitCost+3)*amphipod.cost)
            if (bc&&cd&&r) list.add(base.copy(right = amphipod) to (emitCost+5)*amphipod.cost)
            if (bc&&cd&&r&&fr) list.add(base.copy(farRight = amphipod) to (emitCost+6)*amphipod.cost)
        }
    }

    private fun addDwellingCOptions(list: MutableList<Pair<AmphipodDwellingState, Int>>) {
        if (!dwellingC.emitting) return
        dwellingC.canEmit().let { (newDwelling, amphipod, emitCost) ->
            val base = copy(dwellingC = newDwelling)
            if (l&&ab&&bc&&fl) list.add(base.copy(farLeft = amphipod) to (emitCost+6)*amphipod.cost)
            if (l&&ab&&bc) list.add(base.copy(left = amphipod) to (emitCost+5)*amphipod.cost)
            if (ab&&bc) list.add(base.copy(blockingAB = amphipod) to (emitCost+3)*amphipod.cost)
            if (bc) list.add(base.copy(blockingBC = amphipod) to (emitCost+1)*amphipod.cost)
            if (cd) list.add(base.copy(blockingCD = amphipod) to (emitCost+1)*amphipod.cost)
            if (cd&&r) list.add(base.copy(right = amphipod) to (emitCost+3)*amphipod.cost)
            if (cd&&r&&fr) list.add(base.copy(farRight = amphipod) to (emitCost+4)*amphipod.cost)
        }
    }

    private fun addDwellingDOptions(list: MutableList<Pair<AmphipodDwellingState, Int>>) {
        if (!dwellingD.emitting) return
        dwellingD.canEmit().let { (newDwelling, amphipod, emitCost) ->
            val base = copy(dwellingD = newDwelling)
            if (l&&ab&&bc&&cd&&fl) list.add(base.copy(farLeft = amphipod) to (emitCost+8)*amphipod.cost)
            if (l&&ab&&bc&&cd) list.add(base.copy(left = amphipod) to (emitCost+7)*amphipod.cost)
            if (ab&&bc&&cd) list.add(base.copy(blockingAB = amphipod) to (emitCost+5)*amphipod.cost)
            if (bc&&cd) list.add(base.copy(blockingBC = amphipod) to (emitCost+3)*amphipod.cost)
            if (cd) list.add(base.copy(blockingCD = amphipod) to (emitCost+1)*amphipod.cost)
            if (r) list.add(base.copy(right = amphipod) to (emitCost+1)*amphipod.cost)
            if (r&&fr) list.add(base.copy(farRight = amphipod) to (emitCost+2)*amphipod.cost)
        }
    }
}

private data class AmphipodReceivingDwelling(
    val intended: Amphipod,
    val size: Int,
    val contains: Int
) : DwellingHomeLocation {
    override val accepting = true
    override val emitting = false
    override val solved = false
    override fun canEmit() = error("This doesn't emit")

    override fun accept(travelCost: Int, createState: (DwellingHomeLocation) -> AmphipodDwellingState): Pair<AmphipodDwellingState, Int> =
        if (contains == size-1) createState(AmphipodCompleteDwelling) to (1 + travelCost)*intended.cost
        else createState(copy(contains = contains + 1)) to (travelCost + size - contains)*intended.cost
}

private object AmphipodCompleteDwelling: DwellingHomeLocation {
    override val accepting = false
    override val emitting = false
    override val solved = true
    override fun canEmit() = error("This doesn't emit")
    override fun accept(travelCost: Int, createState: (DwellingHomeLocation) -> AmphipodDwellingState) = error("This doesn't accept")
}

private data class AmphipodMixedDwelling(
    val intended: Amphipod,
    val size: Int,
    val actual: List<Amphipod>
) : DwellingHomeLocation {
    constructor(
        intended: Amphipod,
        actual: List<Amphipod>
    ) : this(
        intended,
        actual.size,
        actual
    )

    override val accepting = false
    override val emitting = true
    override val solved = false

    override fun canEmit(): Triple<DwellingHomeLocation, Amphipod, Int> {
        val first = actual.first()
        val rest = actual.drop(1)
        return if (rest.all { it == intended }) {
            Triple(AmphipodReceivingDwelling(intended, size, rest.size), first, size - actual.size + 1)
        } else {
            Triple(copy(actual = rest), first, size - actual.size + 1)
        }
    }

    override fun accept(travelCost: Int, createState: (DwellingHomeLocation) -> AmphipodDwellingState) = error("This doesn't accept")
}

private fun String.parseStartingState(part2: Boolean): AmphipodDwellingState =
    lines()
        .drop(2)
        .take(2)
        .map { it.split("#").filter(String::isNotBlank) }
        .let { (top, bottom) ->
            if (part2) {
                AmphipodDwellingState(
                    null,
                    null,
                    DwellingHomeLocation.factory(A, valueOf(top[0]), D, D, valueOf(bottom[0])),
                    null,
                    DwellingHomeLocation.factory(B, valueOf(top[1]), C, B, valueOf(bottom[1])),
                    null,
                    DwellingHomeLocation.factory(C, valueOf(top[2]), B, A, valueOf(bottom[2])),
                    null,
                    DwellingHomeLocation.factory(D, valueOf(top[3]), A, C, valueOf(bottom[3])),
                    null,
                    null,
                )
            } else {
                AmphipodDwellingState(
                    null,
                    null,
                    DwellingHomeLocation.factory(A, valueOf(top[0]), valueOf(bottom[0])),
                    null,
                    DwellingHomeLocation.factory(B, valueOf(top[1]), valueOf(bottom[1])),
                    null,
                    DwellingHomeLocation.factory(C, valueOf(top[2]), valueOf(bottom[2])),
                    null,
                    DwellingHomeLocation.factory(D, valueOf(top[3]), valueOf(bottom[3])),
                    null,
                    null,
                )
            }
        }

private class Solver(
    initial: AmphipodDwellingState
) {
    private val minValues = hashMapOf(initial to 0)
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
                val old = minValues[state]
                val new = cost + current
                if (old == null) {
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