package day4

import readAllText

fun main() {
    val input = readAllText("local/day4_input.txt").lines()
    println(part1(input))
    println(part2(input))
}

fun part1(grid: Grid) = grid.findAll('X')
    .flatMap { (horizontal + vertical + diagonals).map { m -> it to m } }
    .mapNotNull { (p, m) -> (p + m).takeIf { grid[it] == 'M' }?.let { it to m } }
    .mapNotNull { (p, m) -> (p + m).takeIf { grid[it] == 'A' }?.let { it to m } }
    .mapNotNull { (p, m) -> (p + m).takeIf { grid[it] == 'S' }?.let { it to m } }
    .count()

fun part2(grid: Grid): Int = grid.findAll('A').count { p ->
    diagonals.count { m -> grid[p - m] == 'M' && grid[p + m] == 'S' } == 2
}

typealias Move = Pair<Int, Int>

val diagonals: Set<Move> = setOf(-1 to -1, -1 to 1, 1 to -1, 1 to 1)
val vertical: Set<Move> = setOf(-1 to 0, 1 to 0)
val horizontal: Set<Move> = setOf(0 to -1, 0 to 1)

typealias Pos = Pair<Int, Int>

operator fun Pos.plus(d: Move) = Pos(first + d.first, second + d.second)
operator fun Pos.minus(d: Move) = Pos(first - d.first, second - d.second)

typealias Grid = List<String>

operator fun Grid.get(p: Pos) = getOrNull(p.first)?.getOrNull(p.second)
fun Grid.findAll(ch: Char): Sequence<Pos> = asSequence().flatMapIndexed { r, line ->
    line.indices.filter { line[it] == ch }.map { c -> Pos(r, c) }
}
