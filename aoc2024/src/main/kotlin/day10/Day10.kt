package day10

import go
import linesWithoutLastBlanks
import measure
import readAllText

typealias Input = Map<Int, Set<Pos>>
typealias Pos = Pair<Int, Int>

enum class Dir { U, R, D, L }

fun parse(text: String): Input = text.linesWithoutLastBlanks()
    .flatMapIndexed { row, s -> s.mapIndexed { col, char -> char.digitToInt() to (row to col) } }
    .groupingBy { it.first }
    .aggregate { _, acc: MutableSet<Pos>?, (_, pos), _ -> acc?.apply { add(pos) } ?: mutableSetOf(pos) }

fun part1(input: Input): Int = calculate(input).sumOf { it.size }

fun part2(input: Input) = calculate(input).sumOf { it.sum() }

private fun calculate(input: Input) = input[0].orEmpty().map { start ->
    (1..9).fold(mapOf(start to 1)) { acc, i ->
        acc.flatMap { (pos, count) -> pos.adjacents().filter(input[i].orEmpty()::contains).map { it to count } }
            .groupingBy { it.first }.fold(0) { a, (_, e) -> a + e }
    }.values
}

fun main() {
    val text = readAllText("local/day10_input.txt")
    val input = parse(text)
    go(574) { part1(input) }
    go(1238) { part2(input) }
    measure(text, parse = ::parse, part1 = ::part1, part2 = ::part2)
}

fun Pos.adjacents() = Dir.entries.map(this::plus)

operator fun Pos.plus(d: Dir) = when (d) {
    Dir.U -> first - 1 to second
    Dir.R -> first to second + 1
    Dir.D -> first + 1 to second
    Dir.L -> first to second - 1
}
