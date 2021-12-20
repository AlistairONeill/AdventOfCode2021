package aoc.day12

import aoc.AdventOfCodeDay
import aoc.day12.CaveId.Companion.END
import aoc.day12.CaveId.Companion.START
import java.io.File
import kotlin.math.max

object Day12 : AdventOfCodeDay {
    override fun String.solve(): Pair<String, String> =
        solve(0) to solve(1)

    private fun String.solve(maxSmallRevisits: Int): String =
        RouteSolver(this, maxSmallRevisits)
            .getAllRoutes()
            .size
            .toString()

    override val day = "12"
    override val test = "226" to "3509"
    override val solution = "4241" to "122134"
}


fun day12() {
    File("data/2021/real/12_01.txt")
        .readText()
        .let { RouteSolver(it, 1) }
        .getAllRoutes()
        .size
        .let(::println)
}



private data class CaveId(val value: String) {
    companion object {
        val START = CaveId("start")
        val END = CaveId("end")
    }
}

private class RouteSolver(input: String, private val maxSmallRevisits: Int) {

    private abstract inner class CaveNode(val id: CaveId) {
        protected val links = mutableSetOf<CaveNode>()
        abstract fun headTowards(history: List<CaveNode>): List<List<CaveNode>>

        fun linkTo(other: CaveNode) {
            links.add(other)
        }
    }

    private fun factoryNode(id: CaveId) =
        when {
            id == START -> StartNode()
            id == END -> EndNode()
            id.value.all(Char::isUpperCase) -> BigNode(id)
            id.value.all(Char::isLowerCase) -> SmallNode(id)
            else -> error("Invalid CaveId[${id.value}]")
        }

    private inner class StartNode: CaveNode(START) {
        fun start(): List<List<CaveNode>> = links.flatMap { it.headTowards(listOf(this)) }
        override fun headTowards(history: List<CaveNode>): List<List<CaveNode>> = emptyList()
    }

    private inner class BigNode(id: CaveId): CaveNode(id) {
        override fun headTowards(history: List<CaveNode>): List<List<CaveNode>> = links.flatMap { it.headTowards(history + this) }
    }

    private inner class SmallNode(id: CaveId): CaveNode(id) {
        override fun headTowards(history: List<CaveNode>) =
            if (history.smallRevisits >= maxSmallRevisits && history.contains(this)) emptyList()
            else links.flatMap { it.headTowards(history + this) }
    }

    private inner class EndNode: CaveNode(END) {
        override fun headTowards(history: List<CaveNode>) = listOf(history)
    }

    private val start: StartNode

    private val List<CaveNode>.smallRevisits get() =
        filterIsInstance<SmallNode>().run {
            size - distinct().size
        }

    init {
        val links = input.split("\n").map {
            val split = it.split("-")
            CaveId(split[0]) to CaveId(split[1])
        }

        val nodes = links.flatMap(Pair<CaveId, CaveId>::toList).toSet().associateWith(::factoryNode)

        fun get(id: CaveId): CaveNode = nodes[id]!!

        links.forEach { (fromId, toId) ->
            val from = get(fromId)
            val to = get(toId)
            from.linkTo(to)
            to.linkTo(from)
        }

        start = get(START) as StartNode
    }

    fun getAllRoutes(): List<List<CaveId>> = start.start().map { it.map(CaveNode::id) }
}

