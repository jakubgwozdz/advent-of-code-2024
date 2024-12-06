package day6

import readAllText
import grid.*

fun main() {
    val input = readAllText("local/day6_input.txt").lines()
    println(part1(input).also { check(it == 4819) })
    println(part2(input).also { check(it == 1796) })
}

fun part1(grid: Grid): Int {
    val start = grid.findAll('^').single()
    return patrol(grid, start).visited.size
}

fun part2(grid: Grid): Int {
    val start = grid.findAll('^').single()
    return patrol(grid, start).visited.filter { grid[it] == '.' }
        .count { extra -> patrol(grid, start, extra).last in grid }
}

/**
 * returns a pair of all visited positions and the last one
 */
private fun patrol(grid: Grid, start: Pos, extra: Pos? = null): Patrol {
    var guard = start to Dir.U
    val visited = mutableSetOf<Pair<Pos, Dir>>()
    while (guard.first in grid && guard !in visited) {
        visited += guard
        val (pos, dir) = guard
        val forward = pos + dir
        guard = if (grid[forward] != '#' && forward != extra) forward to dir
        else pos to dir.turnRight()
    }
    return visited.map { it.first }.toSet() to guard.first
}

typealias Patrol = Pair<Set<Pos>, Pos>

val Patrol.visited get() = first
val Patrol.last get() = second

