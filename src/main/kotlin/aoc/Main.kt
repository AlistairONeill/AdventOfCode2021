package aoc

import aoc.day11.day11
import aoc.day12.day12
import aoc.day13.day13
import java.time.Instant


fun main() {
    val before = Instant.now().toEpochMilli()
    day13()
    val after = Instant.now().toEpochMilli()
    println("${after - before}ms")
}
