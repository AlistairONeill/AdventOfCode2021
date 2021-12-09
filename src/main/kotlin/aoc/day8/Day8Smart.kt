package aoc.day8

import java.io.File


private sealed class Constraint {
    abstract fun simplify(): Constraint
}

private data class SolvedLink(
    val origin: DisplayCableIdentifier,
    val destination: DisplayCableIdentifier
) : Constraint() {
    override fun simplify() = this
}

private data class EntryConstraint(
    val input: Set<DisplayCableIdentifier>,
    val possible: Set<Set<DisplayCableIdentifier>>
) : Constraint() {
    override fun simplify() =
        possible.singleOrNull()?.let { ManyToMany(input, it) }
            ?: EntryConstraint(
                input,
                possible.filter { it.size == input.size }.toSet()
            )
}

private data class ManyToMany(
    val origin: Set<DisplayCableIdentifier>,
    val destination: Set<DisplayCableIdentifier>
) : Constraint() {
    override fun simplify() =
        origin.singleOrNull()?.let { OneToMany(it, destination) }
            ?: destination.singleOrNull()?.let { ManyToOne(origin, it) }
            ?: this
}

private data class OneToMany(
    val origin: DisplayCableIdentifier,
    val destination: Set<DisplayCableIdentifier>
) : Constraint() {
    override fun simplify() = destination.singleOrNull()?.let { SolvedLink(origin, it) } ?: this
}

private data class ManyToOne(
    val origin: Set<DisplayCableIdentifier>,
    val destination: DisplayCableIdentifier
) : Constraint() {
    override fun simplify() = origin.singleOrNull()?.let { SolvedLink(it, destination) } ?: this
}

fun day8Smart() {
    File("data/2021/real/08_01.txt")
        .readLines()
        .map(::parse)
        .map(InputRow::solve)
        .let { outputs ->
            outputs.part1().let(::println)
            outputs.part2().let(::println)
        }
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
    private var constraints = initial
    private val superseded = mutableSetOf<Constraint>()

    private val isSolved get() = constraints
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

    fun applyLogicStep() {
        applySelfSimplification()
        applySoloLogic()
        applyPairLogic()
        removeSuperseded()
    }


    private fun removeSuperseded() {
        constraints = constraints.filter {
            !superseded.contains(it)
        }.toSet()
    }

    private fun applySelfSimplification() {
        constraints = constraints.map(Constraint::simplify).toSet()
    }

    private fun applySoloLogic() {
        constraints = constraints.flatMap {
            applyLogic(it)
        }.toSet()
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
        constraints = constraints.flatMap { first ->
            constraints.flatMap { second ->
                applyLogic2(first, second)
                    ?.apply {
                        if (!contains(first)) superseded.add(first)
                        if (!contains(second)) superseded.add(second)
                    }
                    ?: emptySet()
            }
        }.toSet()
    }

    private fun applyLogic3(first: SolvedLink, second: Constraint): Set<Constraint>? =
        when (second) {
            is EntryConstraint -> applyLogic(second, first)
            is ManyToMany -> applyLogic(second, first)
            is ManyToOne -> applyLogic(second, first)
            is OneToMany -> applyLogic(second, first)
            is SolvedLink -> applyLogic(first, second)
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
