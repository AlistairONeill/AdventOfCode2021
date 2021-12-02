package aoc

import aoc.Direction.*
import java.io.File

fun day2() {
    File("data/2021/real/02_01.txt").readLines()
        .map(Command::parse)
        .fold(State(0, 0, 0), State::apply1)
        .product
        .let(::println)

    File("data/2021/real/02_01.txt").readLines()
        .map(Command::parse)
        .fold(State(0, 0, 0), State::apply2)
        .product
        .let(::println)
}

private data class State(
    val depth: Int,
    val x: Int,
    val aim: Int
) {
    val product get() = depth * x

    fun apply1(command: Command) =
        when (command.direction) {
            forward -> State(depth, x + command.value, aim)
            down -> State(depth + command.value, x, aim)
            up -> State(depth - command.value, x, aim)
        }

    fun apply2(command: Command) =
        when (command.direction) {
            forward -> State(depth + command.value * aim, x + command.value, aim)
            down -> State(depth, x, aim + command.value)
            up -> State(depth, x, aim - command.value)
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