package aoc.day13

import aoc.day13.Fold.Horizontal
import aoc.day13.Fold.Vertical
import java.io.File

fun day13() {
    File("data/2021/test/13_01.txt")
        .readText()
        .parse()
        .run {
            part1().size.let(::println)
            part2().print()
        }
}

private typealias Dots = Set<Pair<Int, Int>>

private sealed interface Fold {
    data class Horizontal(val y: Int) : Fold
    data class Vertical(val x: Int) : Fold
}

private typealias Input = Pair<Dots, List<Fold>>

private fun String.parse(): Input =
    split("\n\n").let { split ->
        split[0].split("\n").map { line ->
            line.split(",").let { parts ->
                parts[0].toInt() to parts[1].toInt()
            }
        }.toSet() to split[1].split("\n").map { line ->
            "fold along ([xy])=(\\d+)".toRegex().matchEntire(line)!!
                .groupValues.let { parts ->
                    when (parts[1]) {
                        "x" -> Vertical(parts[2].toInt())
                        "y" -> Horizontal(parts[2].toInt())
                        else -> error("?!")
                    }
                }
        }
    }

private fun Input.part1(): Dots = applyFold(first, second.first())

private fun Input.part2(): Dots = second.fold(first, ::applyFold)

private fun applyFold(dots: Dots, fold: Fold): Dots =
    when (fold) {
        is Horizontal -> dots.map { (x, y) ->
            when {
                y < fold.y -> x to y
                y > fold.y -> x to (2 * fold.y - y)
                else -> error("!!")
            }
        }
        is Vertical -> dots.map { (x, y) ->
            when {
                x < fold.x -> x to y
                x > fold.x -> (2 * fold.x - x) to y
                else -> error("!!")
            }
        }
    }.toSet()

private fun Dots.print() =
    (0 .. maxOf { it.second }).forEach { y ->
        (0 .. maxOf { it.first }).forEach { x ->
            if (x to y in this) print("#") else print(".")
        }
        println()
    }