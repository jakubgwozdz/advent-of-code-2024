package day6

import readAllText
import grid.*

fun main() {
    val input = readAllText("local/day6_input.txt").lines()
    println(part1(input).also { check(it == 4819) })
    println(part2(input).also { check(it == 1796) })
}

fun part1(grid: Grid): Int = visited(grid).size

fun part2(grid: Grid): Int = visited(grid).filter { grid[it] == '.' }.count { extra ->
    val start = grid.findAll('^').single()
    var guard = start to Dir.U
    val visited = mutableSetOf<Pair<Pos, Dir>>()
    while (guard.first in grid) {
        visited += guard
        guard = step(guard) { grid[it] != '#' && it != extra }
        if (guard in visited) return@count true
    }
    false
}

fun step(guard: Pair<Pos, Dir>, availableOp: (Pos) -> Boolean): Pair<Pos, Dir> {
    val (pos, dir) = guard
    val forward = pos + dir
    return if (availableOp(forward)) forward to dir
    else pos to dir.turnRight()
}

private fun visited(grid: Grid): Set<Pos> {
    val start = grid.findAll('^').single()
    var guard = start to Dir.U
    return buildSet {
        while (guard.first in grid) {
            add(guard.first)
            guard = step(guard) { grid[it] != '#' }
        }
    }
}
