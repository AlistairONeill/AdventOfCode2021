package aoc

import aoc.day11.day11
import java.time.Instant


fun main() {
    val before = Instant.now().toEpochMilli()
    day11()
    val after = Instant.now().toEpochMilli()
    println("${after - before}ms")
}
