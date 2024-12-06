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
    return buildSet { patrol(grid, start) { add(it) } }.size
}

fun part2(grid: Grid): Int {
    val start = grid.findAll('^').single()
    return buildSet { patrol(grid, start) { if (it != start) add(it) } }
        .count { extra -> patrol(grid, start, extra) }
}

// returns true if cycle is found, false if exits the grid
private fun patrol(grid: Grid, start: Pos, extra: Pos? = null, op: (Pos) -> Unit = {}): Boolean {
    var pos = start
    var dir = Dir.U
    val turns = mutableSetOf<Pair<Pos, Pos>>()
    while (true) {
        op(pos)
        val forward = pos + dir
        if (forward !in grid) return false
        if (grid[forward] != '#' && forward != extra) pos = forward
        else {
            val turn = pos to forward
            if (turn in turns) return true
            turns += turn
            dir = dir.turnRight()
        }
    }
}
