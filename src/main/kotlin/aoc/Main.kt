package aoc

import java.time.Instant


fun main() {
    val before = Instant.now().toEpochMilli()
    day8Brute()
    val after = Instant.now().toEpochMilli()
    println("${after - before}ms")
}
