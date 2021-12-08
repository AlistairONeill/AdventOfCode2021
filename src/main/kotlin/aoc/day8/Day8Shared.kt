package aoc.day8

import aoc.day8.DisplayCableIdentifier.*
import aoc.day8.DisplayDigit.*

private val part1Digits = setOf(ONE, FOUR, SEVEN, EIGHT)
private fun isPart1Digit(digit: DisplayDigit) = digit in part1Digits
internal fun Solution.part1() = flatten().count(::isPart1Digit)
internal fun Solution.part2() = sumOf(List<DisplayDigit>::toInt)

private typealias Solution = List<Output>
internal data class InputRow(val population: InputPopulation, val toSolve: UnknownNumber)
private typealias InputPopulation = Set<Input>
private typealias UnknownNumber = List<Input>
internal typealias Input = Set<DisplayCableIdentifier>
internal typealias Output = List<DisplayDigit>

@Suppress("EnumEntryName")
internal enum class DisplayCableIdentifier {
    a, b, c, d, e, f, g
}

internal enum class DisplayDigit {
    ZERO, ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE
}

internal val allDigits = DisplayDigit.values().toSet()

private val outputToDigit = mapOf(
    setOf(a, b, c, e, f, g) to ZERO,
    setOf(c, f) to ONE,
    setOf(a, c, d, e, g) to TWO,
    setOf(a, c, d, f, g) to THREE,
    setOf(b, c, d, f) to FOUR,
    setOf(a, b, d, f, g) to FIVE,
    setOf(a, b, d, e, f, g) to SIX,
    setOf(a, c, f) to SEVEN,
    setOf(a, b, c, d, e, f, g) to EIGHT,
    setOf(a, b, c, d, f, g) to NINE
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