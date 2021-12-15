package aoc

import aoc.day14.day14
import aoc.day15.day15
import java.time.Instant


fun main() {
    val before = Instant.now().toEpochMilli()
    day15()
    val after = Instant.now().toEpochMilli()
    println("${after - before}ms")
}
