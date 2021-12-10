package aoc.day10


import aoc.day10.ChunkCharacter.Closer
import aoc.day10.ChunkCharacter.Opener
import java.io.File

fun day10() {
    File("data/2021/real/10_01.txt")
        .readLines()
        .map(::parse)
        .run {
            filterIsInstance<SyntaxError>()
                .sumOf(SyntaxError::score)
                .let(::println)

            filterIsInstance<AutoCompletion>()
                .map(AutoCompletion::score)
                .median()
                .let(::println)
        }
}

private sealed interface ChunkCharacter {
    enum class Opener(val closer: Closer): ChunkCharacter {
        Parenthesis(Closer.Parenthesis),
        Bracket(Closer.Bracket),
        SquigglyBoi(Closer.SquigglyBoi),
        SharpLad(Closer.SharpLad)
    }

    enum class Closer: ChunkCharacter {
        Parenthesis, Bracket, SquigglyBoi, SharpLad
    }

    companion object {
        fun parse(char: Char): ChunkCharacter =
            when (char) {
                '(' -> Opener.Parenthesis
                '[' -> Opener.Bracket
                '{' -> Opener.SquigglyBoi
                '<' -> Opener.SharpLad
                ')' -> Closer.Parenthesis
                ']' -> Closer.Bracket
                '}' -> Closer.SquigglyBoi
                '>' -> Closer.SharpLad
                else -> error("Failed to parse Char $char")
            }
    }
}

// Only valid for odd size, but task gives us that
private fun List<Long>.median() = sorted()[(size+1)/2-1]

private sealed interface ParseResult

private data class SyntaxError(val bad: Closer): ParseResult

private data class AutoCompletion(val characters: List<Closer>): ParseResult

private fun SyntaxError.score(): Long =
    when (bad) {
        Closer.Parenthesis -> 3
        Closer.Bracket -> 57
        Closer.SquigglyBoi -> 1197
        Closer.SharpLad -> 25137
    }

private fun AutoCompletion.score(): Long =
    characters.joinToString("") { c ->
        when (c) {
            Closer.Parenthesis -> "1"
            Closer.Bracket -> "2"
            Closer.SquigglyBoi -> "3"
            Closer.SharpLad -> "4"
        }
    }.toLong(5)

private fun parse(input: String): ParseResult {
    val stack = mutableListOf<Opener>()

    input.asSequence()
        .map(ChunkCharacter.Companion::parse)
        .forEach { c ->
            when (c) {
                is Opener -> stack.add(c)
                is Closer -> {
                    if (stack.removeLastOrNull()?.closer != c) {
                        return SyntaxError(c)
                    }
                }
            }
        }

    return stack.reversed().map(Opener::closer).let(::AutoCompletion)
}