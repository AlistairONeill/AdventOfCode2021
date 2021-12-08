package aoc.day8

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
