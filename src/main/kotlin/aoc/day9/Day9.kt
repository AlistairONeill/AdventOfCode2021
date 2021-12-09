package aoc.day9

import java.io.File

fun day9() {
    File("data/2021/real/09_01.txt")
        .readLines()
        .map { it.toCharArray().map(Char::digitToInt) }
        .let { input ->
            input.findLowPoints().sumOf(Int::inc).let(::println)
            input.findBasinSizes().sortedDescending().take(3).reduce { a, b -> a*b }.let(::println)
        }
}

private fun List<List<Int>>.findLowPoints(): List<Int> =
    indices.flatMap { y ->
        get(y).indices.mapNotNull { x ->
            if (listOfNotNull(
                if (x==0) null else get(y)[x-1],
                if (y==0) null else get(y-1)[x],
                if (x==get(y).size-1) null else get(y)[x+1],
                if (y==size-1) null else get(y+1)[x]
            ).none { it <= get(y)[x] }) {
                get(y)[x]
            } else {
                null
            }
        }
    }

private class BasinFinder(input: List<List<Int>>) {
    private val basinClassifications = input.map { row ->
        row.map { height ->
            if (height == 9) null
            else BasinClassification(null)
        }
    }

    private val maxX = input.first().size - 1
    private val maxY = input.size - 1

    private val solved get() = basinClassifications.flatten().filterNotNull().all { it.basin != null }
    private val maxBasinId get() = basinClassifications.flatMap { it.mapNotNull { it?.basin } }.maxOfOrNull { it } ?: -1

    private fun solve() {
        while (!solved) {
            if (!performSolveStep()) {
                basinClassifications.flatten().filterNotNull().firstOrNull { it.basin == null }?.basin = maxBasinId + 1
            }
        }
    }

    private fun performSolveStep(): Boolean {
        var madeChange = false
        basinClassifications.forEachIndexed { y, row ->
            row.forEachIndexed { x, basinClassification ->
                if (basinClassification != null && basinClassification.basin == null) {
                        madeChange = ((x!=0&&basinClassifications[y][x-1]?.basin?.also{basinClassification.basin=it}!=null) ||
                                (y!=0&&basinClassifications[y-1][x]?.basin?.also{basinClassification.basin=it}!=null) ||
                                (x!=maxX&&basinClassifications[y][x+1]?.basin?.also{basinClassification.basin=it}!=null) ||
                                (y!=maxY&&basinClassifications[y+1][x]?.basin?.also{basinClassification.basin=it}!=null))
                                || madeChange

                }
            }
        }
        return madeChange
    }

    fun getBasinSizes(): List<Int> {
        solve()

        return basinClassifications.flatten().filterNotNull().groupBy {
            it.basin!!
        }.map { (_, value) -> value.size }
    }
}

private data class BasinClassification(var basin: Int?)

private fun List<List<Int>>.findBasinSizes(): List<Int> =
    BasinFinder(this)
        .getBasinSizes()


