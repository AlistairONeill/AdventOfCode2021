package aoc.day18


import aoc.AdventOfCodeDay
import aoc.day18.SnailNumber.SnailLiteral
import aoc.day18.SnailNumber.SnailPair

object Day18 : AdventOfCodeDay {
    override fun String.solve(): Pair<String, String> =
        lines().run { part1() to part2() }

    override val day = "18"
    override val test = "4140" to "3993"
    override val solution = "4033" to "4864"
}

private class SnakeNumber private constructor(private var head: Segment) {
    class Segment(
        var left: Segment?,
        var right: Segment?,
        var value: Int,
        var depth: Int
    )

    companion object {
        private fun Segment.farRight(): Segment = right?.farRight() ?: this

        fun parse(input: String): SnakeNumber =
            parseNext(input.iterator(), 0).let(::SnakeNumber)

        private fun parseNext(iterator: CharIterator, depth: Int): Segment {
            val first = iterator.nextChar()

            return when {
                first.isDigit() -> Segment(null, null, first.digitToInt(), depth)
                first == '[' -> finishPairParse(iterator, depth)
                else -> error("Not a digit or opening bracket")
            }
        }

        private fun finishPairParse(iterator: CharIterator, depth: Int): Segment {
            val first = parseNext(iterator, depth + 1)
            if (iterator.nextChar() != ',') error("Expecting comma")
            val second = parseNext(iterator, depth + 1)
            if (iterator.nextChar() != ']') error("Expecting closing bracket")
            second.left = first.farRight()
            first.farRight().right = second
            return first
        }
    }

    private fun assertWellLinked() {
        if (head.left != null) error("Head has a left!")
        var segment : Segment? = head.right
        while (segment != null) {
            if (segment.left!!.right != segment) error("Things aren't being linked too good!")
            segment = segment.right
        }
    }

    private fun forEach(f: Segment.() -> Unit) {
        var segment: Segment? = head
        while (segment != null) {
            segment.f()
            segment = segment.right
        }
    }

    operator fun plus(other: SnakeNumber): SnakeNumber {
        other.head.left = head.farRight()
        head.farRight().right = other.head

        forEach { depth += 1 }
        reduce()
        return this
    }

    private fun reduce() {
        while (true) {
            assertWellLinked()
            if (reduceByExplosion()) continue
            if (reduceBySplit()) continue
            break
        }
    }

    private fun reduceByExplosion(): Boolean {
        var segment: Segment? = head
        while (segment != null) {
            if (
                segment.depth >= 5 &&
                segment.depth == segment.right?.depth
            ) {
                val left = segment
                val right = segment.right ?: error("Ruh Roh")

                left.left?.run { value += left.value }
                right.right?.run { value += right.value }

                left.right = right.right
                right.right?.left = left

                left.value = 0
                left.depth -= 1

                return true
            }
            segment = segment.right
        }
        return false
    }

    private fun reduceBySplit(): Boolean {
        var segment: Segment? = head
        while (segment != null) {
            if (segment.value > 9) {
                val left = Segment(segment.left, null, segment.value / 2, segment.depth + 1)
                val right = Segment(left, segment.right, segment.value / 2 + segment.value % 2, segment.depth + 1)
                left.right = right
                left.left?.right = left
                right.right?.left = right

                if (segment == head) {
                    head = left
                }
                return true
            }
            segment = segment.right
        }
        return false
    }

    fun magnitude(): Int {
        while (true) {
            if (collapse()) continue
            break
        }
        return head.value
    }

    private fun collapse(): Boolean {
        var segment: Segment = head
        while (true) {
            val next = segment.right ?: return false

            if (segment.depth == next.depth) {
                segment.depth -= 1
                segment.value = 3 * segment.value + 2 * next.value
                segment.right = next.right
                return true
            }

            segment = next
        }
    }

    fun clone() =
        SnakeNumber(
            head.cloneRight()
        )

    fun Segment.cloneRight(): Segment =
        Segment(
            null,
            right?.cloneRight(),
            value,
            depth
        ).apply { right?.left = this}
}

private fun List<String>.part1() =
    map(SnakeNumber::parse)
        .reduce(SnakeNumber::plus)
        .magnitude()
        .toString()

private fun List<String>.part2() =
    map(SnakeNumber::parse)
        .let {
            it.flatMap { a ->
                it.map { b -> a to b }
            }
        }.maxOf { (a, b) -> a.clone().plus(b.clone()).magnitude() }
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


