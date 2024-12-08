package day8

import go
import linesWithoutLastBlanks
import measure
import readAllText

fun solve(grid: Grid, op: (Pos, Pos) -> Sequence<Pos>) = buildSet {
    grid.antennas.forEach { (_, positions) ->
        positions.indices.forEach { i1 ->
            val pos1 = positions[i1]
            (i1 + 1..positions.lastIndex).forEach { i2 ->
                val pos2 = positions[i2]
                op(pos1, pos2).forEach { add(it) }
            }
        }
    }
}.count()

fun part1(grid: Grid) = solve(grid) { pos1, pos2 ->
    val delta = pos2 - pos1
    sequenceOf(pos1 - delta, pos2 + delta).filter { it in grid }
}

fun part2(grid: Grid) = solve(grid) { pos1, pos2 ->
    val delta = (pos2 - pos1).gcded()
    var p = pos1 - delta
    while (p in grid) p -= delta
    generateSequence(p) { it + delta }
        .dropWhile { it !in grid }
        .takeWhile { it in grid }
}

data class Grid(
    val rowRange: IntRange,
    val colRange: IntRange,
    val antennas: Map<Char, List<Pos>>
) {
    operator fun contains(p: Pos) = p.first in rowRange && p.second in rowRange
}

typealias Pos = Pair<Int, Int>

operator fun Pos.plus(d: Pos) = Pos(first + d.first, second + d.second)
operator fun Pos.minus(d: Pos) = Pos(first - d.first, second - d.second)
fun Pos.gcded(): Pos {
    val gcd = gcd(first, second)
    return Pos(first / gcd, second / gcd)
}

tailrec fun gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b)

fun parse(text: String): Grid {
    val lines = text.linesWithoutLastBlanks()
    val antennas = lines.flatMapIndexed { row, line -> line.mapIndexed { col, ch -> ch to Pos(row, col) } }
        .groupingBy { (ch, _) -> ch }
        .aggregate { _, accumulator: MutableSet<Pos>?, (_, pos), _ ->
            (accumulator ?: mutableSetOf()).apply { add(pos) }
        }
    return Grid(lines.indices, lines[0].indices, (antennas - '.').mapValues { (_, v) -> v.toList() })
}

fun main() {
    val text = readAllText("local/day8_input.txt")
    val input = parse(text)
    go(413) { part1(input) }
    go(1417) { part2(input) }
    measure(text, parse = ::parse, part1 = ::part1, part2 = ::part2)
}
