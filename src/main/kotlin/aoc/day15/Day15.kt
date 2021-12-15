package aoc.day15

import java.io.File
import kotlin.Int.Companion.MAX_VALUE

fun day15() {
    File("data/2021/real/15_01.txt")
        .readText()
        .run {
            part1()
            part2()
        }

}

private fun String.part1() =
    RouteSolver(this, 1)
        .solve()
        .let(::println)

private fun String.part2() =
    RouteSolver(this, 5)
        .solve()
        .let(::println)

private class RouteSolver(input: String, copies: Int) {
    private inner class RouteNode(
        val entryCost: Int
    ) {
        val connections = mutableSetOf<RouteNode>()
        var lowestCost = MAX_VALUE

        fun visit() {
            nodesToConsider.remove(this)
            connections.forEach { node ->
                node.lowestCost = minOf(node.lowestCost, lowestCost + node.entryCost)
                nodesToConsider.add(node)
                node.connections.remove(this)
            }
        }
    }

    private val nodesToConsider: MutableSet<RouteNode>
    private val target: RouteNode

    init {
        val raw = input.split("\n").map { row -> row.toCharArray().map { it.digitToInt() } }
        val rawHeight = raw.size
        val rawWidth = raw.first().size
        val gridNodes = (0 until rawHeight * copies).map { y ->
            (0 until rawWidth * copies).map { x ->
                val inc = y/rawHeight + x/rawWidth
                RouteNode(
                    ((raw[y%rawHeight][x%rawWidth] - 1 + inc) % 9) + 1
                )
            }
        }

        val maxY = gridNodes.size - 1
        gridNodes.indices.forEach { y ->
            val maxX = gridNodes.size - 1
            gridNodes[y].indices.forEach { x ->
                val node = gridNodes[y][x]
                if (x > 0) { gridNodes[y][x-1].connections.add(node) }
                if (y > 0) { gridNodes[y-1][x].connections.add(node) }
                if (x < maxX) { gridNodes[y][x+1].connections.add(node) }
                if (y < maxY) { gridNodes[y+1][x].connections.add(node) }
            }
        }

        gridNodes.first().first().lowestCost = 0
        target = gridNodes.last().last()
        nodesToConsider = mutableSetOf(gridNodes.first().first())
    }


    fun solve(): Int {
        while (nodesToConsider.isNotEmpty()) {
            nodesToConsider.minByOrNull { it.lowestCost }!!.visit()
        }

        return target.lowestCost
    }
}