package aoc.day16

import aoc.day16.Operation.*
import aoc.day16.Packet.Container
import aoc.day16.Packet.Literal
import java.io.File

fun day16() {
    part1()
    part2()
}

private fun part1() =
    File("data/2021/real/16_01.txt")
        .readText()
        .asSequence()
        .flatMap(::toBits)
        .iterator()
        .readPacket()
        .sumVersions()
        .let(::println)

private fun part2() =
    File("data/2021/real/16_01.txt")
        .readText()
        .asSequence()
        .flatMap(::toBits)
        .iterator()
        .readPacket()
        .calculate()
        .let(::println)

private fun toBits(char: Char): Sequence<Boolean> =
    when (char) {
        '0' -> sequenceOf(false, false, false, false)
        '1' -> sequenceOf(false, false, false, true)
        '2' -> sequenceOf(false, false, true, false)
        '3' -> sequenceOf(false, false, true, true)
        '4' -> sequenceOf(false, true, false, false)
        '5' -> sequenceOf(false, true, false, true)
        '6' -> sequenceOf(false, true, true, false)
        '7' -> sequenceOf(false, true, true, true)
        '8' -> sequenceOf(true, false, false, false)
        '9' -> sequenceOf(true, false, false, true)
        'A' -> sequenceOf(true, false, true, false)
        'B' -> sequenceOf(true, false, true, true)
        'C' -> sequenceOf(true, true, false, false)
        'D' -> sequenceOf(true, true, false, true)
        'E' -> sequenceOf(true, true, true, false)
        'F' -> sequenceOf(true, true, true, true)
        else -> error("COULD NOT PARSE")
    }

private fun Iterator<Boolean>.readBits(count: Int): List<Boolean> = (0 until count).map { next() }

private enum class Operation {
    SUM, PRODUCT, MINIMUM, MAXIMUM, GT, LT, EQ
}

private sealed interface Packet {
    val version: Int

    fun sumVersions(): Long
    fun calculate(): Long

    data class Literal(override val version: Int, val value: Long): Packet {
        companion object {
            fun readValue(iterator: Iterator<Boolean>): Long {
                val bits = ArrayList<Boolean>()
                do {
                    val control = iterator.next()
                    bits.addAll(iterator.readBits(4))
                } while (control)

                return bits.toLong()
            }
        }

        override fun sumVersions(): Long = version.toLong()
        override fun calculate() = value
    }

    data class Container(
        override val version: Int,
        val operation: Operation,
        val subPackets: List<Packet>
        ): Packet {
        companion object {
            fun readSubPackets(iterator: Iterator<Boolean>): List<Packet> {
                return if (iterator.next()) {
                    val packetCount = iterator.readBits(11).toInt()
                    (0 until packetCount).map {
                        iterator.readPacket()
                    }
                } else {
                    val packets = ArrayList<Packet>()
                    val bits = iterator.readBits(15).toInt()
                    val newIterator = iterator.readBits(bits).iterator()
                    while (newIterator.hasNext()) {
                        packets.add(newIterator.readPacket())
                    }
                    return packets
                }
            }
        }

        override fun sumVersions() = version.toLong() + subPackets.sumOf(Packet::sumVersions)

        override fun calculate() =
            when (operation) {
                SUM -> subPackets.sumOf(Packet::calculate)
                PRODUCT -> subPackets.fold(1L) { acc, packet -> acc * packet.calculate() }
                MINIMUM -> subPackets.minOf(Packet::calculate)
                MAXIMUM -> subPackets.maxOf(Packet::calculate)
                GT -> if (subPackets[0].calculate() > subPackets[1].calculate()) 1L else 0L
                LT -> if (subPackets[0].calculate() < subPackets[1].calculate()) 1L else 0L
                EQ -> if (subPackets[0].calculate() == subPackets[1].calculate()) 1L else 0L
            }
    }
}

private fun List<Boolean>.toInt() = joinToString("") { if (it) "1" else "0" }.toInt(2)
private fun List<Boolean>.toLong() = joinToString("") { if (it) "1" else "0" }.toLong(2)

private fun Iterator<Boolean>.readPacket(): Packet {
    val version = readBits(3).toInt()
    val typeId = readBits(3).toInt()

    return if (typeId == 4) {
        Literal(version, Literal.readValue(this))
    } else {
        val subPackets = Container.readSubPackets(this)
        val operation = when (typeId) {
            0 -> SUM
            1 -> PRODUCT
            2 -> MINIMUM
            3 -> MAXIMUM
            5 -> GT
            6 -> LT
            7 -> EQ
            else -> error("Invalid typeId")
        }
        Container(version, operation, subPackets)
    }
}