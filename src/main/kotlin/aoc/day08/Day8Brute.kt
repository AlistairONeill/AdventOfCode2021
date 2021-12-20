package aoc.day08

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

private fun InputRow.solve(): Output = toSolve.map(allPotentialMappings.findActual(population))

private fun List<PotentialMapping>.findActual(input: Set<Input>): (Input) -> DisplayDigit =
    first { it.isSolutionFor(input) }.finalise()::apply

internal abstract class AbstractMapping(protected val data: Map<DisplayCableIdentifier, DisplayCableIdentifier>) {
    open fun apply(input: Input): DisplayDigit? = input.map{ data[it]!! }.toSet().toDisplayDigit()
}

private class PotentialMapping(data: List<Pair<DisplayCableIdentifier, DisplayCableIdentifier>>):
    AbstractMapping(data.toMap()) {
    fun finalise() = ActualMapping(data)
    fun isSolutionFor(input: Set<Input>) : Boolean = input.mapNotNull(::apply).toSet() == allDigits
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
