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
}

private data class SolvedLink(
    val origin: DisplayCableIdentifier,
    val destination: DisplayCableIdentifier
) : Constraint() {
    override fun simplify() = setOf(this)
}

private data class EntryConstraint(
    val input: Set<DisplayCableIdentifier>,
    val possible: Set<Set<DisplayCableIdentifier>>
) : Constraint() {
    override fun simplify() = setOf(
        possible.singleOrNull()?.let { ManyToMany(input, it) }
            ?: if (possible.any { it.size != input.size })
                EntryConstraint(
                    input,
                    possible.filter { it.size == input.size }.toSet()
                )
            else this
    )
}

private data class ManyToMany(
    val origin: Set<DisplayCableIdentifier>,
    val destination: Set<DisplayCableIdentifier>
) : Constraint() {
    override fun simplify() =
        origin.singleOrNull()?.let { OneToMany(it, destination).simplify() }
            ?: destination.singleOrNull()?.let { ManyToOne(origin, it).simplify() }
            ?: setOf(this)

}

private data class OneToMany(
    val origin: DisplayCableIdentifier,
    val destination: Set<DisplayCableIdentifier>
) : Constraint() {
    override fun simplify() =
        setOf(
            destination.singleOrNull()?.let { SolvedLink(origin, it) } ?: this
        )
}

private data class ManyToOne(
    val origin: Set<DisplayCableIdentifier>,
    val destination: DisplayCableIdentifier
) : Constraint() {
    override fun simplify() = setOf(
        origin.singleOrNull()?.let { SolvedLink(it, destination) } ?: this
    )
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
    private var constraints = initial.flatMap(Constraint::simplify).toMutableList()

    private val isSolved
        get() = constraints
            .filterIsInstance<SolvedLink>()
            .size == DisplayCableIdentifier.values().size

    fun solve() {
        var safety = 0
        while (!isSolved) {
            safety += 1
            if (safety > 500) break
            applyLogicStep()
        }

        if (!isSolved) error(":'(")
    }

    private fun addConstraint(constraint: Constraint) {
        if (!constraints.contains(constraint)) {
            constraints.add(constraint)
        }
    }

    private fun replaceConstraints(new: Set<Constraint>) {
        constraints.clear()
        new.flatMap(Constraint::simplify).toSet()
            .forEach(::addConstraint)
    }

    fun applyLogicStep() {
        applySoloLogic()
        applyPairLogic()
    }

    private fun applySoloLogic() {
        replaceConstraints(
            constraints.flatMap {
                applyLogic(it)
            }.toSet()
        )
    }

    private fun applyLogic(constraint: Constraint): Set<Constraint> =
        when (constraint) {
            is EntryConstraint -> applyLogic(constraint)
            is ManyToMany -> applyLogic(constraint)
            is ManyToOne -> applyLogic(constraint)
            is OneToMany -> applyLogic(constraint)
            is SolvedLink -> applyLogic(constraint)
        }

    private fun applyLogic(constraint: EntryConstraint): Set<Constraint> =
        constraint.possible.reduce { a, b -> a.intersect(b) }
            .map { ManyToOne(constraint.input, it) }
            .toSet() + constraint

    private fun applyLogic(constraint: ManyToMany): Set<Constraint> =
        if (constraint.origin.isEmpty()) {
            emptySet()
        } else {
            setOf(constraint)
        }

    private fun applyLogic(constraint: ManyToOne): Set<Constraint> = setOf(constraint)
    private fun applyLogic(constraint: OneToMany): Set<Constraint> = setOf(constraint)
    private fun applyLogic(constraint: SolvedLink): Set<Constraint> = setOf(constraint)

    private fun applyPairLogic() {
        replaceConstraints(
            constraints.flatMap { first ->
                constraints.flatMap { second ->
                    applyLogic2(first, second)
                        ?: emptySet()
                }
            }.toSet()
        )
    }

    private fun applyLogic2(first: Constraint, second: Constraint): Set<Constraint>? =
        when (first) {
            is EntryConstraint -> when (second) {
                is EntryConstraint -> applyLogic(first, second)
                is ManyToMany -> applyLogic(first, second)
                is ManyToOne -> applyLogic(first, second)
                is OneToMany -> applyLogic(first, second)
                is SolvedLink -> applyLogic(first, second)
            }
            is ManyToMany -> when (second) {
                is EntryConstraint -> applyLogic(second, first)
                is ManyToMany -> applyLogic(first, second)
                is ManyToOne -> applyLogic(first, second)
                is OneToMany -> applyLogic(first, second)
                is SolvedLink -> applyLogic(first, second)
            }
            is ManyToOne -> when (second) {
                is EntryConstraint -> applyLogic(second, first)
                is ManyToMany -> applyLogic(second, first)
                is ManyToOne -> applyLogic(first, second)
                is OneToMany -> applyLogic(first, second)
                is SolvedLink -> applyLogic(first, second)
            }
            is OneToMany -> when (second) {
                is EntryConstraint -> applyLogic(second, first)
                is ManyToMany -> applyLogic(second, first)
                is ManyToOne -> applyLogic(second, first)
                is OneToMany -> applyLogic(first, second)
                is SolvedLink -> applyLogic(first, second)
            }
            is SolvedLink -> when (second) {
                is EntryConstraint -> applyLogic(second, first)
                is ManyToMany -> applyLogic(second, first)
                is ManyToOne -> applyLogic(second, first)
                is OneToMany -> applyLogic(second, first)
                is SolvedLink -> applyLogic(first, second)
            }
        }

    private fun applyLogic(first: EntryConstraint, second: EntryConstraint): Set<Constraint>? =
        if (first.input == second.input) {
            setOf(
                EntryConstraint(
                    first.input,
                    first.possible.intersect(second.possible)
                )
            )
        } else {
            null
        }

    @Suppress("UNUSED_PARAMETER")
    private fun applyLogic(first: EntryConstraint, second: ManyToMany): Set<Constraint>? =
        null

    private fun applyLogic(first: EntryConstraint, second: ManyToOne): Set<Constraint>? {
        val newPossible = if (first.input.containsAll(second.origin)) {
            first.possible.filter {
                it.contains(second.destination)
            }.toSet()
        } else {
            first.possible
        }

        return if (newPossible != first.possible) {
            setOf(EntryConstraint(first.input, newPossible), second)
        } else {
            null
        }
    }

    private fun applyLogic(first: EntryConstraint, second: OneToMany): Set<Constraint>? {
        val newPossible = if (first.input.contains(second.origin)) {
            first.possible.filter { possibility ->
                second.destination.any { possibility.contains(it) }
            }.toSet()
        } else {
            first.possible
        }

        return if (newPossible != first.possible) {
            setOf(EntryConstraint(first.input, newPossible), second)
        } else {
            null
        }
    }

    private fun applyLogic(first: EntryConstraint, second: SolvedLink): Set<Constraint>? {
        val newPossible = if (first.input.contains(second.origin)) {
            first.possible.filter {
                it.contains(second.destination)
            }
        } else {
            first.possible.filter {
                !it.contains(second.destination)
            }
        }.toSet()

        return if (newPossible != first.possible) {
            setOf(EntryConstraint(first.input, newPossible), second)
        } else {
            null
        }
    }

    private fun applyLogic(first: ManyToMany, second: ManyToMany): Set<Constraint>? {
        val originIntersection = first.origin.intersect(second.origin)
        val firstOriginMissing = first.origin.subtract(originIntersection)
        val secondOriginMissing = second.origin.subtract(originIntersection)

        val destinationIntersection = first.destination.intersect(second.destination)
        val firstDestinationMissing = first.destination.subtract(destinationIntersection)
        val secondDestinationMissing = second.destination.subtract(destinationIntersection)

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

    private fun applyLogic(first: ManyToMany, second: ManyToOne): Set<Constraint>? =
        if (first.origin == second.origin) {
            setOf(second)
        } else {
            null
        }

    private fun applyLogic(first: ManyToMany, second: OneToMany): Set<Constraint>? =
        if (first.destination == second.destination) {
            setOf(second)
        } else {
            null
        }

    private fun applyLogic(first: ManyToMany, second: SolvedLink): Set<Constraint>? {
        val newOrigin = first.origin.filter { it != second.origin }.toSet()
        val newDestination = first.destination.filter { it != second.destination }.toSet()

        return if (newOrigin == first.origin && newDestination == first.destination) {
            null
        } else {
            setOf(
                ManyToMany(newOrigin, newDestination),
                second
            )
        }
    }

    private fun applyLogic(first: ManyToOne, second: ManyToOne): Set<Constraint>? =
        if (first.destination == second.destination) {
            setOf(
                ManyToOne(
                    first.origin.intersect(second.origin),
                    first.destination
                )
            )
        } else {
            null
        }

    @Suppress("UNUSED_PARAMETER")
    private fun applyLogic(first: ManyToOne, second: OneToMany): Set<Constraint>? = null

    private fun applyLogic(first: ManyToOne, second: SolvedLink): Set<Constraint>? =
        if (first.destination == second.destination) {
            setOf(second)
        } else {
            null
        }

    private fun applyLogic(first: OneToMany, second: OneToMany): Set<Constraint>? =
        if (first.origin == second.origin) {
            setOf(
                OneToMany(
                    first.origin,
                    first.destination.intersect(second.destination)
                )
            )
        } else {
            null
        }

    private fun applyLogic(first: OneToMany, second: SolvedLink): Set<Constraint>? =
        if (first.origin == second.origin) {
            setOf(second)
        } else {
            null
        }

    private fun applyLogic(first: SolvedLink, second: SolvedLink): Set<Constraint> =
        setOf(first, second)

    fun perform(toSolve: UnknownNumber): Output {
        val mapping = constraints.filterIsInstance<SolvedLink>()
            .associate { it.origin to it.destination }
            .let(::ActualMapping)::apply

        return toSolve.map(mapping)
    }
}
