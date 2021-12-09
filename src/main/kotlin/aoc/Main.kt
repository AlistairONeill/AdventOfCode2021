package aoc

import aoc.day8.day8Brute
import aoc.day8.day8Smart
import aoc.day9.day9
import java.time.Instant


fun main() {
    val before = Instant.now().toEpochMilli()
    day9()
    val after = Instant.now().toEpochMilli()
    println("${after - before}ms")
}
