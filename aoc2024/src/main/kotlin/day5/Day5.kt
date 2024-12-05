package day5

import readAllText

fun main() {
    val input = readAllText("local/day5_input.txt")
    val parsed = parse(input)
    println(part1(parsed))
    println(part2(parsed))
}

data class Input(
    val ordering: List<Pair<String, String>>,
    val updates: List<List<String>>,
)

fun part1(input: Input): Long {
    val comparator = Day5Comparator(input.ordering)
    val correct = input.updates
        .filter { it == it.sortedWith(comparator) }
    return correct.sumOf { it[it.size / 2].toLong() }
}

fun part2(input: Input): Long {
    val comparator = Day5Comparator(input.ordering)
    val incorrectSorted = input.updates
        .mapNotNull { it.sortedWith(comparator).takeIf { sorted -> sorted != it } }
    return incorrectSorted.sumOf { it[it.size / 2].toLong() }
}

class Day5Comparator(private val afters: Map<String, Set<String>>) : Comparator<String> {
    constructor(ordering: List<Pair<String, String>>) : this(buildMap<String, MutableSet<String>> {
        ordering.forEach { (x, y) -> getOrPut(x) { mutableSetOf() }.add(y) }
    })

    override fun compare(x: String?, y: String?): Int = when {
        y in afters[x].orEmpty() -> -1
        x in afters[y].orEmpty() -> 1
        else -> 0
    }
}

private fun parse(input: String) = input.lines().let { lines ->
    val s = lines.indexOfFirst(String::isBlank)
    Input(
        lines.take(s).map { it.split("|").let { (a, b) -> a to b } },
        lines.drop(s + 1).dropLastWhile(String::isBlank).map { it.split(",") },
    )
}
