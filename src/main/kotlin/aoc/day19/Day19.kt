package aoc.day19

import aoc.AdventOfCodeDay
import java.io.File
import kotlin.math.abs

object Day19 : AdventOfCodeDay {
    override fun String.solve(): Pair<String, String> =
        let(ScannerSolver::parse)
            .also(ScannerSolver::solve)
            .run {
                beacons.size.toString() to scanners.maxOf { s1 ->
                    scanners.maxOf(s1::manhatten)
                }.toString()
            }

    override val day = "19"
    override val test = "79" to "3621"
    override val solution = "335" to "10864"
}

private class Scanner(
    readings: Collection<Coordinate>
) {
    val signatures: Map<Coordinate, Set<Int>> =
        readings.associateWith { r1 ->
            readings.filter { it != r1 }
                .map(r1::dist)
                .toSet()
        }

    companion object {
        fun parse(input: String): Scanner =
            Scanner(
                input.split("\n")
                    .drop(1)
                    .map(Coordinate::parse)
            )
    }

    fun overlaps(other: Scanner): Map<Coordinate, Coordinate> =
        signatures
            .mapNotNull { (reading1, signature1) ->
                other.signatures.entries.singleOrNull { (_, signature2) ->
                    signature1.intersect(signature2).size >= 11
                }?.let { reading1 to it.key }
            }.toMap()
}

private data class Coordinate(
    val x: Int,
    val y: Int,
    val z: Int
) {
    companion object {
        fun parse(input: String): Coordinate =
            input.split(",")
                .let { (x, y, z) ->
                    Coordinate(
                        x.toInt(),
                        y.toInt(),
                        z.toInt()
                    )
                }
    }

    fun dist(other: Coordinate): Int =
        (x - other.x) * (x - other.x) + (y - other.y) * (y - other.y) + (z - other.z) * (z - other.z)

    operator fun minus(other: Coordinate): Coordinate =
        Coordinate(
            x - other.x,
            y - other.y,
            z - other.z
        )

    operator fun plus(other: Coordinate): Coordinate =
        Coordinate(
            x + other.x,
            y + other.y,
            z + other.z
        )

    fun manhatten(other: Coordinate): Int =
        abs(x - other.x) + abs(y - other.y) + abs(z - other.z)
}

private class ScannerSolver(input: Collection<Scanner>) {
    private val resolvedScanners = mutableListOf(input.first() to Coordinate(0, 0, 0))
    private val unresolvedScanners = input.drop(1).toMutableSet()

    companion object {
        fun parse(input: String) =
            ScannerSolver(
                input.split("\n\n")
                    .map(Scanner::parse)
            )
    }

    fun solve() {
        var i = 0
        while (i < resolvedScanners.size) {
            val (scanner, coordinate) = resolvedScanners[i]
            unresolvedScanners.filter { unresolved ->
                val overlaps = scanner.overlaps(unresolved)
                if (overlaps.size > 11) {
                    val (translate, rotate) = findOrientation(overlaps.toList())
                    val rotatedNew = Scanner(
                        unresolved.signatures.keys.map(rotate)
                    )
                    resolvedScanners.add(rotatedNew to coordinate + translate)
                    true
                } else {
                    false
                }
            }.forEach(unresolvedScanners::remove)
            i+=1
        }

        if (unresolvedScanners.isNotEmpty()) error("I'm a failure :'(")
    }

    val beacons by lazy {
        resolvedScanners.flatMap { (scanner, location) ->
            scanner.signatures.keys.map { relative ->
                relative + location
            }
        }.toSet()
    }

    val scanners by lazy { resolvedScanners.map { it.second } }
}

private typealias OrientationTransformation = Coordinate.() -> Coordinate

private object Orientations {
    private val orientations: List<OrientationTransformation>

    operator fun get(index: Int) = orientations[index]

    init {
        val flips = listOf<OrientationTransformation>(
            { Coordinate(x, y, z) },
            { Coordinate(-x, y, z) },
            { Coordinate(x, -y, z) },
            { Coordinate(x, y, -z) },
            { Coordinate(-x, -y, z) },
            { Coordinate(-x, y, -z) },
            { Coordinate(x, -y, -z) },
            { Coordinate(-x, -y, -z) }
        )

        val rotations = listOf<OrientationTransformation>(
            { Coordinate(x, y, z) },
            { Coordinate(x, z, y) },
            { Coordinate(y, x, z) },
            { Coordinate(y, z, x) },
            { Coordinate(z, x, y) },
            { Coordinate(z, y, x) },
        )

        orientations = flips.flatMap { flip -> rotations.map { rotate -> combine(flip, rotate) } }
    }

    private fun combine(t1: OrientationTransformation, t2: OrientationTransformation): OrientationTransformation =
        { t1().t2() }
}

private fun findOrientation(toFind: Collection<Pair<Coordinate, Coordinate>>): Pair<Coordinate, OrientationTransformation> {
    for (i in (0 until 48)) {
        val transform = Orientations[i]
        val target = toFind.first().let { it.first - it.second.transform() }
        if (toFind.all { (first, second) -> first - second.transform() == target }) {
            return target to transform
        }
    }
    error("Could not find orientation")
}