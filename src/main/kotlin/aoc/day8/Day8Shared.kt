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

internal val allDigits = DisplayDigit.values().toSet()

internal val DisplayDigit.outputs get() =
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

private fun String.toInput() : Input =
    toCharArray()
        .map(Char::toString)
        .map(DisplayCableIdentifier::valueOf)
        .toSet()

private fun String.toInputs(): List<Input> = split(" ").map(String::toInput)

internal fun parse(input: String): InputRow {
    val split = input.split(" | ")
    return InputRow(split[0].toInputs().toSet(), split[1].toInputs())
}

internal class ActualMapping(data: Map<DisplayCableIdentifier, DisplayCableIdentifier>) :
    AbstractMapping(data) {
    override fun apply(input: Input) : DisplayDigit = super.apply(input)!!
}