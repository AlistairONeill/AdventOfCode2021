package aoc

import aoc.DisplayCableIdentifier.*
import aoc.DisplayDigit.*
import java.io.File

fun day8Brute() =
    File("data/2021/real/08_01.txt")
        .readLines()
        .map(::parse)
        .map(InputRow::solve)
        .let { outputs ->
            outputs.part1().let(::println)
            outputs.part2().let(::println)
        }

private val part1Digits = setOf(ONE, FOUR, SEVEN, EIGHT)
private fun isPart1Digit(digit: DisplayDigit) = digit in part1Digits
private fun Solution.part1() = flatten().count(::isPart1Digit)
private fun Solution.part2() = sumOf(List<DisplayDigit>::toInt)

private typealias Solution = List<Output>
private data class InputRow(val population: InputPopulation, val toSolve: UnknownNumber)
private typealias InputPopulation = Set<Input>
private typealias UnknownNumber = List<Input>
private typealias Input = Set<DisplayCableIdentifier>
private typealias Output = List<DisplayDigit>

@Suppress("EnumEntryName")
private enum class DisplayCableIdentifier {
    a, b, c, d, e, f, g
}

private enum class DisplayDigit {
    ZERO, ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE
}

private val allDigits = DisplayDigit.values().toSet()

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

private fun Input.toDisplayDigit(): DisplayDigit? = outputToDigit[this]

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

private val allPotentialMappings: List<PotentialMapping> = DisplayCableIdentifier.values().toList().toPermutations().map {
    DisplayCableIdentifier.values().zip(it)
}.map(::PotentialMapping)

private fun String.toInput() : Input =
    toCharArray()
        .map(Char::toString)
        .map(DisplayCableIdentifier::valueOf)
        .toSet()

private fun String.toInputs(): List<Input> = split(" ").map(String::toInput)

private fun parse(input: String): InputRow {
    val split = input.split(" | ")
    return InputRow(split[0].toInputs().toSet(), split[1].toInputs())
}

private fun InputRow.solve(): Output = toSolve.map(allPotentialMappings.findActual(population))

private fun List<PotentialMapping>.findActual(input: Set<Input>): (Input) -> DisplayDigit =
    first { it.isSolutionFor(input) }.finalise()::apply

private abstract class AbstractMapping(protected val data: Map<DisplayCableIdentifier, DisplayCableIdentifier>) {
    open fun apply(input: Input): DisplayDigit? = input.map{ data[it]!! }.toSet().toDisplayDigit()
}

private class PotentialMapping(data: List<Pair<DisplayCableIdentifier, DisplayCableIdentifier>>):
    AbstractMapping(data.toMap()) {
    fun finalise() = ActualMapping(data)
    fun isSolutionFor(input: Set<Input>) : Boolean = input.mapNotNull(::apply).toSet() == allDigits
}

private class ActualMapping(data: Map<DisplayCableIdentifier, DisplayCableIdentifier>) :
    AbstractMapping(data) {
    override fun apply(input: Input) : DisplayDigit = super.apply(input)!!
}

private fun List<DisplayCableIdentifier>.toPermutations(): List<List<DisplayCableIdentifier>> =
    if (size == 0) {
        listOf(emptyList())
    } else {
        flatMap { element ->
            filter { it != element }
                .toPermutations()
                .map { listOf(element) + it }
        }
    }
