package aoc

import aoc.day14.day14
import java.time.Instant


fun main() {
    val before = Instant.now().toEpochMilli()
    day14()
    val after = Instant.now().toEpochMilli()
    println("${after - before}ms")
}
