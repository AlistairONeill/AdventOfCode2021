package aoc

interface AdventOfCodeDay {
    fun String.solve(): Pair<String, String>

    val day: String
    val test: Pair<String, String>
    val solution: Pair<String?, String?>
}