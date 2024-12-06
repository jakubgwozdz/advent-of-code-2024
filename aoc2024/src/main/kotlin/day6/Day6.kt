package day6

import go
import readAllText

fun main() {
    val input = readAllText("local/day6_input.txt").lines()
    go(expected = 4819) { part1(input) }
    go(expected = 1796) { part2(input) }
}

fun part1(grid: Grid): Int {
    val start = grid.indexOf('^')
    return buildSet { patrol(grid, start) { add(it) } }.size
}

fun part2(grid: Grid): Int {
    val start = grid.indexOf('^')
    val valid = buildSet { patrol(grid, start) { if (it != start) add(it) } }
    return valid.count { extra -> patrol(grid, start, extra) }
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

typealias Pos = Pair<Int, Int>
typealias Grid = List<String>

operator fun Grid.get(p: Pos) = getOrNull(p.first)?.getOrNull(p.second)
operator fun Grid.contains(p: Pos) = p.first in indices && p.second in get(p.first).indices

fun Grid.indexOf(ch: Char):Pos = indexOfFirst { ch in it }.let { r-> r to this[r].indexOf(ch) }

enum class Dir { U, R, D, L }
fun Dir.turnRight() = Dir.entries[(ordinal + 1) % Dir.entries.size]
operator fun Pos.plus(d: Dir) = when (d) {
    Dir.U -> first - 1 to second
    Dir.R -> first to second + 1
    Dir.D -> first + 1 to second
    Dir.L -> first to second - 1
}