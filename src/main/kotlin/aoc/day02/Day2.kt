package aoc.day02

import aoc.AdventOfCodeDay
import aoc.day02.Direction.*

object Day2: AdventOfCodeDay {
    override fun String.solve() =
        lines()
            .map(Command.Companion::parse)
            .run {
                solve(::task1) to solve(::task2)
            }

    private fun List<Command>.solve(transformer: (State, Command) -> State): String =
        fold(State(0, 0, 0), transformer)
            .product
            .toString()

    override val day = "02"
    override val test = "150" to "900"
    override val solution = "1488669" to "1176514794"
}

private data class State(
    val depth: Int,
    val x: Int,
    val aim: Int
) {
    val product get() = depth * x
}

private fun task1(state: State, command: Command): State =
    state.run {
        when (command.direction) {
            forward -> copy(x = x + command.value)
            down -> copy(depth = depth + command.value)
            up -> copy(depth = depth - command.value)
        }
    }

private fun task2(state: State, command: Command): State =
    state.run {
        when (command.direction) {
            forward -> copy(depth = depth + command.value * aim, x = x + command.value)
            down -> copy(aim = aim + command.value)
            up -> copy(aim = aim - command.value)
        }
    }

@Suppress("EnumEntryName")
private enum class Direction {
    forward, down, up;
}

private data class Command(
    val direction: Direction,
    val value: Int
) {
    companion object {
        fun parse(input: String): Command {
            val split = input.split(" ")
            return Command(
                valueOf(split[0]),
                split[1].toInt()
            )
        }
    }
}