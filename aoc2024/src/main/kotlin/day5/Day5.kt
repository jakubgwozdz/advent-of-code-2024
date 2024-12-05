package day5

import readAllText

fun main() {
    val input = readAllText("local/day5_input.txt")
    val parsed = parse(input)
    println(part1(parsed))
    println(part2(parsed))
}

data class Input(
    val ordering: List<List<String>>,
    val updates: List<List<String>>,
)

// 5248
fun part1(input: Input) = input.let { (ordering, updates) ->
    val afters = afters(ordering)
    updates.filter { isCorrect(it, afters) }
        .sumOf { it[it.size / 2].toLong() }
}

// 4507
fun part2(input: Input) = input.let { (ordering, updates) ->
    val afters = afters(ordering)
    updates.filterNot { isCorrect(it, afters) }
        .map { it.sortedWith(Day5Comparator(afters)) }
        .sumOf { it[it.size / 2].toLong() }
}

private fun parse(input: String) = input.lines().let { lines ->
    val s = lines.indexOfFirst(String::isBlank)
    Input(
        lines.take(s).map { it.split("|") },
        lines.drop(s+1).dropLastWhile(String::isBlank).map { it.split(",") },
    )
}

private fun afters(ordering: List<List<String>>): Map<String, Set<String>> {
    val afters = mutableMapOf<String, MutableSet<String>>()
    ordering.forEach { (x, y) -> afters.getOrPut(x) { mutableSetOf() }.add(y) }
    return afters
}

private fun isCorrect(
    update: List<String>,
    afters: Map<String, Set<String>>
) = update.indices.all { i ->
    val s = update[i]
    val a = afters[s].orEmpty()
    update.take(i).none { x -> x in a } && update.drop(i + 1).none { y -> s in afters[y].orEmpty() }
}

class Day5Comparator(val afters: Map<String, Set<String>>):Comparator<String> {
    override fun compare(x: String?, y: String?): Int = when {
        y in afters[x].orEmpty() -> -1
        x in afters[y].orEmpty() -> 1
        else -> 0
    }
}
