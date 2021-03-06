package aoc.day11

import aoc.AdventOfCodeDay

object Day11 : AdventOfCodeDay {
    override fun String.solve(): Pair<String, String> =
        let(::OctopusGrid)
            .run {
                val part1 = (0 until 100).sumOf { performStep() }.toString()
                var part2 = 101
                while (performStep() != size) {
                    part2 += 1
                }
                part1 to part2.toString()
            }

    override val day = "11"
    override val test = "1656" to "195"
    override val solution = "1627" to "329"
}

private class OctopusGrid(input: String) {
    private var data: List<DumboOctopus>
    val size: Int

    init {
        val data = input.lines()
            .map { row ->
                row.map { char ->
                    DumboOctopus(char.digitToInt())
                }
            }

        val height = data.size
        val width = data.first().size

        fun get(x: Int, y: Int) =
            if (x in 0 until width && y in 0 until height) data[y][x] else null

        data.forEachIndexed { y, row ->
            row.forEachIndexed { x, oct ->
                get(x - 1, y - 1)?.connect(oct)
                get(x - 1, y)?.connect(oct)
                get(x - 1, y + 1)?.connect(oct)
                get(x, y - 1)?.connect(oct)
                get(x, y + 1)?.connect(oct)
                get(x + 1, y - 1)?.connect(oct)
                get(x + 1, y)?.connect(oct)
                get(x + 1, y + 1)?.connect(oct)
            }
        }

        this.data = data.flatten()
        size = data.sumOf(List<*>::size)
    }

    fun performStep(): Int {
        incrementEnergy()
        val ret = performFlashes()
        reset()
        return ret
    }

    private fun incrementEnergy() = data.forEach(DumboOctopus::incrementEnergy)

    private fun performFlashes(): Int {
        var flashes = 0

        do {
            val newFlashes = data.count(DumboOctopus::attemptFlash)
            flashes += newFlashes
        } while (newFlashes > 0)

        return flashes
    }

    private fun reset() = data.forEach(DumboOctopus::reset)
}

private class DumboOctopus(private var energyLevel: Int) {
    private var hasJustFlashed: Boolean = false
    private val connections = mutableSetOf<DumboOctopus>()

    fun connect(other: DumboOctopus) {
        connections.add(other)
    }

    fun reset() {
        if (energyLevel > 9) {
            energyLevel = 0
        }
        hasJustFlashed = false
    }

    fun attemptFlash(): Boolean {
        if (hasJustFlashed) return false
        if (energyLevel <= 9) return false
        hasJustFlashed = true
        connections.onEach(DumboOctopus::incrementEnergy)
        return true
    }

    fun incrementEnergy() {
        energyLevel += 1
    }
}
