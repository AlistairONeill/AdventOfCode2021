package aoc.day18


import aoc.AdventOfCodeDay
import aoc.day18.SnailNumber.SnailLiteral
import aoc.day18.SnailNumber.SnailPair
import java.io.File

object Day18 : AdventOfCodeDay {
    override fun String.solve(): Pair<String, String> =
        lines().run { part1() to part2() }

    override val day = "18"
    override val test = "4140" to "3993"
    override val solution = "4033" to "4864"
}

private fun List<String>.part1() =
    map(SnailNumber::parse)
        .reduce(SnailNumber::plus)
        .magnitude
        .toString()

private fun List<String>.part2() =
    flatMap { a -> map { b -> a to b } }
        .maxOf { (a, b) -> SnailNumber.parse(a).plus(SnailNumber.parse(b)).magnitude }
        .toString()

private sealed interface SnailNumber {
    companion object {
        fun parse(input: String): SnailNumber =
            input.iterator()
                .run(::parseNext)

        private fun parseNext(iterator: CharIterator): SnailNumber {
            val first = iterator.nextChar()
            return when {
                first.isDigit() -> SnailLiteral(first.digitToInt())
                first == '[' -> finishPairParse(iterator)
                else -> error("Not a digit or opening bracket")
            }
        }

        private fun finishPairParse(iterator: CharIterator): SnailPair {
            val first = parseNext(iterator)
            if (iterator.nextChar() != ',') error("Expecting comma")
            val second = parseNext(iterator)
            if (iterator.nextChar() != ']') error("Expecting closing bracket")
            return SnailPair(first, second)
        }
    }

    class SnailPair(
        var left: SnailNumber,
        var right: SnailNumber
    ) : SnailNumber {
        override lateinit var replace: (SnailNumber) -> Unit
        override val magnitude get() = 3 * left.magnitude + 2 * right.magnitude

        init {
            left.replace = ::replaceLeft
            right.replace = ::replaceRight
        }

        private fun replaceLeft(number: SnailNumber) {
            left = number
            number.replace = ::replaceLeft
        }

        private fun replaceRight(number: SnailNumber) {
            right = number
            number.replace = ::replaceRight
        }
    }

    class SnailLiteral(
        var value: Int
    ) : SnailNumber {
        override lateinit var replace: (SnailNumber) -> Unit
        override val magnitude get() = value.toLong()
    }

    fun plus(other: SnailNumber): SnailNumber = SnailPair(this, other).apply(SnailReducer::reduce)

    var replace: (SnailNumber) -> Unit
    val magnitude: Long
}

private object SnailReducer {
    fun reduce(number: SnailNumber) {
        while (true) {
            if (number.reduceViaExplode()) continue
            if (number.reduceViaSplit()) continue
            break
        }
    }

    private fun SnailNumber.reduceViaExplode(): Boolean {
        val flattened = flatten(0)
        val explosionTarget = flattened.firstOrNull { it.first is SnailPair && it.second >= 4 } ?: return false

        val index = flattened.indexOf(explosionTarget)

        val toExplode = explosionTarget.first as? SnailPair ?: error("Not a pair")
        val first = toExplode.left as? SnailLiteral ?: error("Not a literal")
        val second = toExplode.right as? SnailLiteral ?: error("Not a literal")

        toExplode.replace(SnailLiteral(0))

        flattened
            .take(index - 1)
            .reversed()
            .map { it.first }
            .filterIsInstance<SnailLiteral>()
            .firstOrNull()
            ?.let { it.value += first.value }

        flattened
            .drop(index + 2)
            .map { it.first }
            .filterIsInstance<SnailLiteral>()
            .firstOrNull()
            ?.let { it.value += second.value }

        return true
    }

    private fun SnailNumber.reduceViaSplit(): Boolean {
        val flattened = flatten(0)

        val splitTarget =
            flattened.map { it.first }.filterIsInstance<SnailLiteral>().firstOrNull { it.value >= 10 } ?: return false

        val left = splitTarget.value / 2
        val right = splitTarget.value / 2 + splitTarget.value % 2

        splitTarget.replace(
            SnailPair(
                SnailLiteral(left),
                SnailLiteral(right)
            )
        )

        return true
    }

    private fun SnailNumber.flatten(depth: Int): List<Pair<SnailNumber, Int>> =
        when (this) {
            is SnailLiteral -> listOf(this to depth)
            is SnailPair -> listOf(
                left.flatten(depth + 1),
                listOf(this to depth),
                right.flatten(depth + 1)
            ).flatten()
        }
}


