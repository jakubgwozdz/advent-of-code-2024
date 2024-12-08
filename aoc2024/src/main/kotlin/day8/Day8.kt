package day8

import go
import linesWithoutLastBlanks
import measure
import readAllText

data class Grid(
    val lines: List<String>,
    val colRange: IntRange,
    val rowRange: IntRange,
    val antennas: Map<Char, Set<Pos>>
)

operator fun Grid.contains(p: Pos) = p.first in rowRange && p.second in rowRange

fun part1(grid: Grid) = grid
    .antennas
    .flatMap { (ch, positions) ->
        val list = positions.toList()
        buildSet {
            list.forEachIndexed { first, pos1 ->
                (first + 1..list.lastIndex).forEach { second ->
                    val pos2 = list[second]
                    (pos1 + pos1 - pos2).takeIf { it in grid }?.let { add(it) }
                    (pos2 + pos2 - pos1).takeIf { it in grid }?.let { add(it) }
                }
            }
        }
    }
    .toSet()
    .count()

fun part2(grid: Grid) = grid
    .antennas
    .flatMap { (ch, positions) ->
        val list = positions.toList()
        buildSet {
            list.forEachIndexed { first, pos1 ->
                (first + 1..list.lastIndex).forEach { second ->
                    val pos2 = list[second]
                    val delta = (pos2 - pos1).let { (a, b) ->
                        val gcd = gcd(a, b)
                        Pos(a / gcd, b / gcd)
                    }
                    var p = pos1
                    while (p in grid) p -= delta
                    do {
                        if (p in grid) add(p)
                        p+=delta
                    } while (p in grid)
                }
            }
        }
    }
    .toSet()
    .count()

tailrec fun gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b)

typealias Pos = Pair<Int, Int>

operator fun Pos.plus(d: Pos) = Pos(first + d.first, second + d.second)
operator fun Pos.minus(d: Pos) = Pos(first - d.first, second - d.second)

fun parse(text: String): Grid {
    val lines = text.linesWithoutLastBlanks()
    val antennas = lines.flatMapIndexed { row, line -> line.mapIndexed { col, ch -> ch to Pos(row, col) } }
        .groupBy { (ch, _) -> ch }.mapValues { (k, v) -> v.map { (_, pos) -> pos }.toSet() }
    return Grid(lines, lines[0].indices, lines.indices, antennas - '.')
}

val test = """
    ............
    ........0...
    .....0......
    .......0....
    ....0.......
    ......A.....
    ............
    ............
    ........A...
    .........A..
    ............
    ............
""".trimIndent()

fun main() {
    val text = readAllText("local/day8_input.txt")
    val input = parse(text)
    go(14) { part1(parse(test)) }
    go(413) { part1(input) }
    go(34) { part2(parse(test)) }
    go(1417) { part2(input) }
//    TODO()
    measure(text, parse = ::parse, part1 = ::part1, part2 = ::part2)
}
