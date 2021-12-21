package aoc.day08

import aoc.AdventOfCodeDay
import aoc.day08.DisplayCableIdentifier.*
import aoc.day08.DisplayDigit.*

object Day8 : AdventOfCodeDay {
    override fun String.solve(): Pair<String, String> =
        lines()
            .map(::parse)
            .map(InputRow::solve)
            .run {
                part1() to part2()
            }

    override val day = "08"
    override val test = "26" to "61229"
    override val solution = "416" to "1043697"

}

private val part1Digits = setOf(ONE, FOUR, SEVEN, EIGHT)
private fun isPart1Digit(digit: DisplayDigit) = digit in part1Digits
internal fun Solution.part1() = flatten().count(::isPart1Digit).toString()
internal fun Solution.part2() = sumOf(List<DisplayDigit>::toInt).toString()

private typealias Solution = List<Output>

internal data class InputRow(val population: InputPopulation, val toSolve: UnknownNumber)
private typealias InputPopulation = Set<Input>
internal typealias UnknownNumber = List<Input>
internal typealias Input = Set<DisplayCableIdentifier>
internal typealias Output = List<DisplayDigit>

@Suppress("EnumEntryName")
internal enum class DisplayCableIdentifier {
    a, b, c, d, e, f, g
}

internal enum class DisplayDigit {
    ZERO, ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE
}

internal val DisplayDigit.outputs
    get() =
        when (this) {
            ZERO -> setOf(a, b, c, e, f, g)
            ONE -> setOf(c, f)
            TWO -> setOf(a, c, d, e, g)
            THREE -> setOf(a, c, d, f, g)
            FOUR -> setOf(b, c, d, f)
            FIVE -> setOf(a, b, d, f, g)
            SIX -> setOf(a, b, d, e, f, g)
            SEVEN -> setOf(a, c, f)
            EIGHT -> setOf(a, b, c, d, e, f, g)
            NINE -> setOf(a, b, c, d, f, g)
        }

private val outputToDigit = mapOf(
    ZERO.outputs to ZERO,
    ONE.outputs to ONE,
    TWO.outputs to TWO,
    THREE.outputs to THREE,
    FOUR.outputs to FOUR,
    FIVE.outputs to FIVE,
    SIX.outputs to SIX,
    SEVEN.outputs to SEVEN,
    EIGHT.outputs to EIGHT,
    NINE.outputs to NINE
)

internal fun Input.toDisplayDigit(): DisplayDigit? = outputToDigit[this]

private fun List<DisplayDigit>.toInt(): Int =
    if (size == 0) 0
    else dropLast(1).toInt() * 10 + last().toInt()

private fun DisplayDigit.toInt(): Int =
    when (this) {
        ZERO -> 0
        ONE -> 1
        TWO -> 2
        THREE -> 3
        FOUR -> 4
        FIVE -> 5
        SIX -> 6
        SEVEN -> 7
        EIGHT -> 8
        NINE -> 9
    }

private fun String.toInput(): Input =
    toCharArray()
        .map(Char::toString)
        .map(DisplayCableIdentifier::valueOf)
        .toSet()

private fun String.toInputs(): List<Input> = split(" ").map(String::toInput)

internal fun parse(input: String): InputRow {
    val split = input.split(" | ")
    return InputRow(split[0].toInputs().toSet(), split[1].toInputs())
}

internal abstract class AbstractMapping(protected val data: Map<DisplayCableIdentifier, DisplayCableIdentifier>) {
    open fun apply(input: Input): DisplayDigit? = input.map { data[it]!! }.toSet().toDisplayDigit()
}

internal class ActualMapping(data: Map<DisplayCableIdentifier, DisplayCableIdentifier>) :
    AbstractMapping(data) {
    override fun apply(input: Input): DisplayDigit = super.apply(input)!!
}

private sealed class Constraint {
    abstract fun simplify(): Set<Constraint>
    fun deduce(other: Constraint) =
        when (other) {
            is EntryConstraint -> deduce(other)
            is ManyToMany -> deduce(other)
            is ManyToOne -> deduce(other)
            is OneToMany -> deduce(other)
            is SolvedLink -> deduce(other)
        }

    abstract fun deduce(other: SolvedLink): Set<Constraint>?
    abstract fun deduce(other: EntryConstraint): Set<Constraint>?
    abstract fun deduce(other: ManyToMany): Set<Constraint>?
    abstract fun deduce(other: ManyToOne): Set<Constraint>?
    abstract fun deduce(other: OneToMany): Set<Constraint>?
}

private data class SolvedLink(
    val origin: DisplayCableIdentifier,
    val destination: DisplayCableIdentifier
) : Constraint() {
    override fun simplify() = setOf(this)

    override fun deduce(other: SolvedLink) = null

    override fun deduce(other: EntryConstraint): Set<Constraint>? {
        val newPossible = if (other.input.contains(origin)) {
            other.possible.filter {
                it.contains(destination)
            }
        } else {
            other.possible.filter {
                !it.contains(destination)
            }
        }.toSet()

        return if (newPossible != other.possible) {
            setOf(EntryConstraint(other.input, newPossible))
        } else {
            null
        }
    }

    override fun deduce(other: ManyToMany): Set<Constraint>? {
        val newOrigin = other.origin.filter { it != origin }.toSet()
        val newDestination = other.destination.filter { it != destination }.toSet()

        return if (newOrigin == other.origin && newDestination == other.destination) {
            null
        } else {
            setOf(
                ManyToMany(newOrigin, newDestination)
            )
        }
    }

    override fun deduce(other: ManyToOne): Set<Constraint>? =
        if (other.destination == destination) {
            emptySet()
        } else {
            null
        }

    override fun deduce(other: OneToMany): Set<Constraint>? =
        if (other.origin == origin) {
            emptySet()
        } else {
            null
        }

}

private data class EntryConstraint(
    val input: Set<DisplayCableIdentifier>,
    val possible: Set<Set<DisplayCableIdentifier>>
) : Constraint() {
    override fun simplify() =
        possible.reduce { a, b -> a.intersect(b) }
            .map { ManyToOne(input, it) }
            .toSet() +
                setOf(
                    possible.singleOrNull()?.let { ManyToMany(input, it) }
                        ?: if (possible.any { it.size != input.size })
                            EntryConstraint(
                                input,
                                possible.filter { it.size == input.size }.toSet()
                            )
                        else this
                )

    override fun deduce(other: SolvedLink) = other.deduce(this)

    override fun deduce(other: EntryConstraint): Set<Constraint>? =
        if (other.input == input) {
            setOf(
                EntryConstraint(
                    other.input,
                    other.possible.intersect(possible)
                )
            )
        } else {
            null
        }

    override fun deduce(other: ManyToMany): Set<Constraint>? = null

    override fun deduce(other: ManyToOne): Set<Constraint>? {
        val newPossible = if (input.containsAll(other.origin)) {
            possible.filter {
                it.contains(other.destination)
            }.toSet()
        } else {
            possible
        }

        return if (newPossible != possible) {
            setOf(EntryConstraint(input, newPossible))
        } else {
            null
        }
    }

    override fun deduce(other: OneToMany): Set<Constraint>? {
        val newPossible = if (input.contains(other.origin)) {
            possible.filter { possibility ->
                other.destination.any { possibility.contains(it) }
            }.toSet()
        } else {
            possible
        }

        return if (newPossible != possible) {
            setOf(EntryConstraint(input, newPossible))
        } else {
            null
        }
    }
}

private data class ManyToMany(
    val origin: Set<DisplayCableIdentifier>,
    val destination: Set<DisplayCableIdentifier>
) : Constraint() {
    override fun simplify() =
        setOfNotNull(
            origin.singleOrNull()?.let { OneToMany(it, destination) }
                ?: destination.singleOrNull()?.let { ManyToOne(origin, it) }
                ?: if (origin.isEmpty()) null else this
        )

    override fun deduce(other: SolvedLink) = other.deduce(this)

    override fun deduce(other: EntryConstraint) = other.deduce(this)

    override fun deduce(other: ManyToMany): Set<Constraint>? {
        val originIntersection = origin.intersect(other.origin)
        val firstOriginMissing = origin.subtract(originIntersection)
        val secondOriginMissing = other.origin.subtract(originIntersection)

        val destinationIntersection = destination.intersect(other.destination)
        val firstDestinationMissing = destination.subtract(destinationIntersection)
        val secondDestinationMissing = other.destination.subtract(destinationIntersection)

        if (originIntersection.size != destinationIntersection.size) {
            error("CONSTRAINTS INCONSISTENT")
        }

        return if (originIntersection.isEmpty()) {
            null
        } else {
            setOf(
                ManyToMany(originIntersection, destinationIntersection),
                ManyToMany(firstOriginMissing, firstDestinationMissing),
                ManyToMany(secondOriginMissing, secondDestinationMissing)
            )
        }
    }

    override fun deduce(other: ManyToOne): Set<Constraint>? =
        if (origin == other.origin) {
            emptySet()
        } else {
            null
        }

    override fun deduce(other: OneToMany): Set<Constraint>? =
        if (destination == other.destination) {
            emptySet()
        } else {
            null
        }
}

private data class OneToMany(
    val origin: DisplayCableIdentifier,
    val destination: Set<DisplayCableIdentifier>
) : Constraint() {
    override fun simplify() =
        setOf(
            destination.singleOrNull()?.let { SolvedLink(origin, it) } ?: this
        )

    override fun deduce(other: SolvedLink): Set<Constraint>? = other.deduce(this)

    override fun deduce(other: EntryConstraint): Set<Constraint>? = other.deduce(this)

    override fun deduce(other: ManyToMany): Set<Constraint>? = other.deduce(this)

    override fun deduce(other: ManyToOne): Set<Constraint>? = null

    override fun deduce(other: OneToMany): Set<Constraint>? =
        if (origin == other.origin) {
            setOf(
                OneToMany(
                    origin,
                    destination.intersect(other.destination)
                )
            )
        } else {
            null
        }
}

private data class ManyToOne(
    val origin: Set<DisplayCableIdentifier>,
    val destination: DisplayCableIdentifier
) : Constraint() {
    override fun simplify() = setOf(
        origin.singleOrNull()?.let { SolvedLink(it, destination) } ?: this
    )

    override fun deduce(other: SolvedLink) = other.deduce(this)

    override fun deduce(other: EntryConstraint) = other.deduce(this)

    override fun deduce(other: ManyToMany) = other.deduce(this)

    override fun deduce(other: ManyToOne): Set<Constraint>? =
        if (destination == other.destination) {
            setOf(
                ManyToOne(
                    origin.intersect(other.origin),
                    destination
                )
            )
        } else {
            null
        }

    override fun deduce(other: OneToMany) = other.deduce(this)
}

private fun InputRow.solve(): Output =
    population.map { input ->
        EntryConstraint(
            input,
            DisplayDigit.values().map { it.outputs }.toSet()
        )
    }
        .toSet()
        .let(::ConstraintSolver)
        .apply(ConstraintSolver::solve)
        .perform(toSolve)

private class ConstraintSolver(initial: Set<Constraint>) {
    private val constraints = ArrayList<Constraint>()
    private var i = 0

    init {
        initial.forEach(::addConstraint)
    }

    private val isSolved
        get() = constraints
            .filterIsInstance<SolvedLink>()
            .size == DisplayCableIdentifier.values().size

    fun solve() {
        var safety = 0
        while (!isSolved && i<constraints.size) {
            safety += 1
            if (safety > 500) break
            applyLogicStep()
            i += 1
        }

        if (!isSolved) error(":'(")
    }

    private fun addConstraint(constraint: Constraint) {
        val simplified = constraint.simplify()
        if (simplified.contains(constraint)) {
            if (!constraints.contains(constraint)) {
                constraints.add(constraint)
            }

            simplified.filter { it != constraint }
                .forEach(::addConstraint)
        } else {
            simplified.forEach(::addConstraint)
        }
    }

    fun applyLogicStep() {
        val current = constraints[i]
        (0 until i).forEach { j ->
            val past = constraints[j]
            val logic2 = past.deduce(current)
            logic2?.forEach(::addConstraint)
        }
    }

    fun perform(toSolve: UnknownNumber): Output {
        val mapping = constraints.filterIsInstance<SolvedLink>()
            .associate { it.origin to it.destination }
            .let(::ActualMapping)::apply

        return toSolve.map(mapping)
    }
}
