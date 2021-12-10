package aoc

import aoc.day10.day10
import java.time.Instant


fun main() {
    val before = Instant.now().toEpochMilli()
    day10()
    val after = Instant.now().toEpochMilli()
    println("${after - before}ms")
}
