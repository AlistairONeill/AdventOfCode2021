package aoc

import aoc.day16.day16
import aoc.day17.day17
import aoc.day18.day18
import aoc.day19.day19
import java.time.Instant


fun main() {
    val before = Instant.now().toEpochMilli()
    day19()
    val after = Instant.now().toEpochMilli()
    println("${after - before}ms")
}
