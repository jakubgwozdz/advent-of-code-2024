package day6

import go
import readAllText
import measure

fun main() {
    val input = readAllText("local/day6_input.txt")
    val parsed = parse(input)
    go(expected = 4819) { part1(parsed) }
    go(expected = 1796) { part2(parsed) }
    measure(input, parse = ::parse, part1 = ::part1, part2 = ::part2)
}

fun parse(string: String) = string.lines()
fun part1(grid: Grid): Int {
    val start = grid.indexOf('^')
    return buildSet { patrol(grid, start) { add(it) } }.size
}

fun part2(grid: Grid): Int {
    val start = grid.indexOf('^')
    val cache = mutableMapOf<Pair<Dir, Pos>, Pos>()
    val valid = buildSet { patrol(grid, start, cache) { if (it != start) add(it) } }
    return valid.count { extra -> patrol(grid, start, cache, extra) }
}

// returns true if cycle is found, false if exits the grid
private fun patrol(
    grid: Grid, start: Pos,
    cache: MutableMap<Pair<Dir, Pos>, Pos>? = null,
    extra: Pos? = null,
    op: (Pos) -> Unit = {}
): Boolean {
    var pos = start
    var dir = Dir.U
    val turns = mutableSetOf<Pair<Dir, Pos>>()
    val cacheWritable = cache != null && extra == null // written only in the "no extra obstacle" mode
    val cacheReadable = cache != null && extra != null // read only in the "extra obstacle" mode
    val toBeCached = if (cacheWritable) mutableSetOf<Pos>() else null
    while (true) {
        var prev: Pos
        do {
            prev = pos
            op(pos)
            if (cacheWritable) toBeCached!! += pos
            val cached = if (cacheReadable && !commonLine(pos, extra)) cache!![dir to pos] else null
            if (cached != null) {
                prev = cached
                pos = prev + dir
            } else pos += dir
        } while (pos in grid && grid[pos] != '#' && pos != extra)
        if (pos !in grid) return false
        pos = prev
        if (cacheWritable) {
            toBeCached!!.forEach { cache!![dir to it] = pos }
            toBeCached.clear()
        }
        val turn = dir to pos
        if (dir == Dir.U && turn in turns) return true
        if (dir == Dir.U) turns += turn
        dir = dir.turnRight()
    }
}

private fun commonLine(pos: Pos, extra: Pos?) = pos.first == extra?.first || pos.second == extra?.second

typealias Pos = Pair<Int, Int>
typealias Grid = List<String>

operator fun Grid.get(p: Pos) = getOrNull(p.first)?.getOrNull(p.second)
operator fun Grid.contains(p: Pos) = p.first in indices && p.second in get(p.first).indices

fun Grid.indexOf(ch: Char): Pos = indexOfFirst { ch in it }.let { r -> r to this[r].indexOf(ch) }

enum class Dir { U, R, D, L }

fun Dir.turnRight() = Dir.entries[(ordinal + 1) % Dir.entries.size]
operator fun Pos.plus(d: Dir) = when (d) {
    Dir.U -> first - 1 to second
    Dir.R -> first to second + 1
    Dir.D -> first + 1 to second
    Dir.L -> first to second - 1
}
