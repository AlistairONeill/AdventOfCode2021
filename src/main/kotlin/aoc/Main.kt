package aoc

import aoc.day11.day11
import aoc.day12.day12
import java.time.Instant


fun main() {
    val before = Instant.now().toEpochMilli()
    day12()
    val after = Instant.now().toEpochMilli()
    println("${after - before}ms")
}
