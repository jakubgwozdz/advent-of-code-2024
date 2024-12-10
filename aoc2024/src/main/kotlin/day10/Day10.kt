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
    .groupBy { it.first }.mapValues { it.value.map { it.second }.toSet() }

fun part1(input: Input): Int = input[0].orEmpty().sumOf { start ->
    (1..9)
        .fold(listOf(start)) { acc, i ->
            acc.flatMap { it.adjacents() }.filter(input[i].orEmpty()::contains)
                .distinct()
        }
        .count()
}

fun part2(input: Input) = input[0].orEmpty().sumOf { start ->
    (1..9)
        .fold(listOf(start)) { acc, i ->
            acc.flatMap { it.adjacents() }.filter(input[i].orEmpty()::contains)
        }
        .count()
}

val test = """
    89010123
    78121874
    87430965
    96549874
    45678903
    32019012
    01329801
    10456732
""".trimIndent()

fun main() {
    val text = readAllText("local/day10_input.txt")
    val input = parse(text)
    go(36) { part1(parse(test)) }
    go(81) { part2(parse(test)) }
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
