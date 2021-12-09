package aoc

import aoc.day8.day8Brute
import aoc.day8.day8Smart
import java.time.Instant


fun main() {
    val before = Instant.now().toEpochMilli()
    day8Smart()
    val after = Instant.now().toEpochMilli()
    println("${after - before}ms")
}
