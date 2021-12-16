package aoc

import aoc.day16.day16
import java.time.Instant


fun main() {
    val before = Instant.now().toEpochMilli()
    day16()
    val after = Instant.now().toEpochMilli()
    println("${after - before}ms")
}
