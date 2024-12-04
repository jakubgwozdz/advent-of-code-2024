package day4

import readAllText

fun main() {
    println(part1(readAllText("local/day4_input.txt")))
    println(part2(readAllText("local/day4_input.txt")))
}

typealias Grid = List<String>
typealias Pos = Pair<Int, Int>
typealias Move = Pair<Int, Int>

private operator fun Pos.plus(d: Move) = Pos(first + d.first, second + d.second)
private operator fun Pos.minus(d: Move) = Pos(first - d.first, second - d.second)
private operator fun Int.times(m: Move) = Move(this * m.first, this * m.second)

fun Grid.findAll(ch: Char): List<Pos> = flatMapIndexed { r, line ->
    line.indices.filter { line[it] == ch }.map { c -> Pos(r, c) }
}

operator fun Grid.get(p: Pos) = getOrNull(p.first)?.getOrNull(p.second)

fun part1(input: String) = input.lines().let { grid ->
    val moves: List<Move> = (-1..1).flatMap { dr -> (-1..1).map { dc -> dr to dc } }
    grid.findAll('X').sumOf { p ->
        moves.count { d -> grid[p + d] == 'M' && grid[p + 2 * d] == 'A' && grid[p + 3 * d] == 'S' }
    }
}

fun part2(input: String) = input.lines().let { grid ->
    val moves: List<Move> = listOf(-1 to -1, -1 to 1, 1 to -1, 1 to 1)
    grid.findAll('A').count { p ->
        moves.count { d -> grid[p - d] == 'M' && grid[p + d] == 'S' } == 2
    }
}
