package day5

import readAllText

fun main() {
    val input = readAllText("local/day5_input.txt")
    val parsed = parse(input)
    println(part1(parsed))
    println(part2(parsed))
}

data class Input(
    val ordering: Set<Pair<Long, Long>>,
    val updates: List<List<Long>>,
)

fun part1(input: Input) = input.updates
    .filter { it == it.sortedWith(comparator(input.ordering)) }
    .sumOf { it.middle() }

fun part2(input: Input) = input.updates
    .mapNotNull { it.sortedWith(comparator(input.ordering)).takeIf { sorted -> sorted != it } }
    .sumOf { it.middle() }

fun <T> comparator(ordering: Set<Pair<T, T>>) = Comparator<T> { o1, o2 ->
    when {
        (o1 to o2) in ordering -> -1
        (o2 to o1) in ordering -> 1
        else -> 0
    }
}

fun <T> List<T>.middle() = this[size / 2]

fun parse(input: String): Input {
    val lines = input.lines()
    val s = lines.indexOfFirst(String::isBlank)
    return Input(
        lines.take(s).map { it.split("|").let { (a, b) -> a.toLong() to b.toLong() } }.toSet(),
        lines.drop(s + 1).dropLastWhile(String::isBlank).map { it.split(",").map(String::toLong) },
    )
}
