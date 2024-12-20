@file:Suppress("SameParameterValue")

package day20

import go
import linesWithoutLastBlanks
import measure
import readAllText
import kotlin.math.absoluteValue

data class Pos(val row: Int, val col: Int)

data class Input(val start: Pos, val end: Pos, val allowed: Set<Pos>)

fun part1(input: Input): Int = solve(input, 100, 2)

fun part2(input: Input): Int = solve(input, 100, 20)

fun solve(input: Input, minimumGain: Int, maxCheat: Int): Int {
    val distToStart = floodFill(input.start, input.allowed)
    val distToEnd = floodFill(input.end, input.allowed)
    val reachables = reachables(2, maxCheat)

    val noCheat = distToStart[input.end]!!

    return input.allowed.sumOf { jumpFrom ->
        val startDist = distToStart[jumpFrom]!!
        reachables.count { (dr, dc) ->
            val cheatDist = dr.absoluteValue + dc.absoluteValue
            val jumpTo = Pos(jumpFrom.row + dr, jumpFrom.col + dc)
            val endDist = distToEnd[jumpTo]
            endDist != null && startDist + cheatDist + endDist <= noCheat - minimumGain
        }
    }
}

fun floodFill(from: Pos, allowed: Set<Pos>) = buildMap {
    val q = mutableListOf(from to 0)
    this[from] = 0
    while (q.isNotEmpty()) {
        val (prev, count) = q.removeFirst()
        listOf(
            Pos(prev.row - 1, prev.col),
            Pos(prev.row, prev.col + 1),
            Pos(prev.row + 1, prev.col),
            Pos(prev.row, prev.col - 1),
        ).forEach { next ->
            if (next !in this && next in allowed) {
                this[next] = count + 1
                q.add(next to count + 1)
            }
        }
    }
}

fun reachables(minDist: Int, maxDist: Int) = buildList {
    (-maxDist..maxDist).forEach { dr ->
        val abs = dr.absoluteValue
        (-maxDist + abs..maxDist - abs).forEach { dc ->
            if (abs + dc.absoluteValue >= minDist) add(Pos(dr, dc))
        }
    }
}

fun List<String>.findAll(ch: Char): Sequence<Pos> = asSequence().flatMapIndexed { r, line ->
    line.indices.filter { line[it] == ch }.map { c -> Pos(r, c) }
}

fun parse(text: String): Input = text.linesWithoutLastBlanks().let { grid ->
    val start = grid.findAll('S').single()
    val end = grid.findAll('E').single()
    val dots = grid.findAll('.').toSet()
    Input(start, end, dots + start + end)
}

fun main() {
    val text = readAllText("local/day20_input.txt")
    val input = parse(text)
    go(1293) { part1(input) }
    go(977747) { part2(input) }
    measure(text, parse = ::parse, part1 = ::part1, part2 = ::part2)
}

