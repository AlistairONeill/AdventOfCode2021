package aoc

import aoc.Direction.*
import java.io.File

fun day2() {
    perform(::task1)
    perform(::task2)
}

private fun perform(transformer: (State, Command) -> State) =
    File("data/2021/real/02_01.txt").readLines()
        .map(Command::parse)
        .fold(State(0, 0, 0), transformer)
        .product
        .let(::println)

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
                Direction.valueOf(split[0]),
                split[1].toInt()
            )
        }
    }
}