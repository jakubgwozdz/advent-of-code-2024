package day20

import go
import linesWithoutLastBlanks
import measure
import readAllText
import kotlin.math.absoluteValue

data class Pos(val row: Int, val col: Int)

fun Pos.neighbors1() = sequenceOf(
    Pos(row - 1, col),
    Pos(row, col + 1),
    Pos(row + 1, col),
    Pos(row, col - 1),
)

fun Pos.neighbors2() = sequenceOf(
    Pos(row - 1, col) to Pos(row - 2, col),
    Pos(row, col + 1) to Pos(row, col + 2),
    Pos(row + 1, col) to Pos(row + 2, col),
    Pos(row, col - 1) to Pos(row, col - 2),
)

data class Input(val start: Pos, val end: Pos, val allowed: Set<Pos>)
data class Cheat(val src: Pos, val dst: Pos, val dist: Int)

fun dijkstra(input: Input, max: Int = Int.MAX_VALUE, cheat: Cheat? = null): Int? {
    val queue = mutableListOf(input.start to 0)
    val visited = mutableSetOf(input.start)
    while (queue.isNotEmpty()) {
        val (last, count) = queue.removeFirst()
        if (count > max) return null
        if (last == input.end) return count
        last.neighbors1().forEach { next ->
            if (next !in visited && next in input.allowed) {
                visited += next
                queue.add(next to count + 1)
            }
        }
        if (last == cheat?.src) {
            queue.add(cheat.dst to count + cheat.dist)
        }
    }
    error("No solution found")
}

fun part1(input: Input, minimumGain: Int = 100): Int {
    val distToStart = distancesTo(input.start, input.allowed)
    val distToEnd = distancesTo(input.end, input.allowed)

    val noCheat = distToStart[input.end]!!
    println("distToStart(end): ${distToStart[input.end]}")
    println("distToEnd(start): ${distToEnd[input.start]}")

    return input.allowed.sumOf { jumpFrom ->
        input.allowed.count { jumpTo ->
            (jumpFrom.row - jumpTo.row).absoluteValue + (jumpFrom.col - jumpTo.col).absoluteValue == 2 &&
                    (distToStart[jumpFrom] ?: 10000000) + (distToEnd[jumpTo] ?: 10000000) <= noCheat - minimumGain - 1
        }
    }
}

private fun distancesTo(pos: Pos, allowed: Set<Pos>) = buildMap {
    val q = mutableListOf(pos to 0)
    this[pos] = 0
    while (q.isNotEmpty()) {
        val (prev, count) = q.removeFirst()
        prev.neighbors1().forEach { next ->
            if (next !in this && next in allowed) {
                this[next] = count + 1
                q.add(next to count + 1)
            }
        }
    }
}


// 2356522 too high
// 1025367 too high
fun part2(input: Input, minimumGain: Int = 100): Int {
    return solve(input, minimumGain, 20)
}

private fun solve(input: Input, minimumGain: Int, maxCheat: Int): Int {
    val distToStart = distancesTo(input.start, input.allowed)
    val distToEnd = distancesTo(input.end, input.allowed)

    val noCheat = distToStart[input.end]!!
    println("distToStart(end): ${distToStart[input.end]}")
    println("distToEnd(start): ${distToEnd[input.start]}")

    return input.allowed.sumOf { jumpFrom ->
        input.allowed.filter { jumpTo ->
            (jumpFrom.row - jumpTo.row).absoluteValue + (jumpFrom.col - jumpTo.col).absoluteValue <= 20
        }.count { jumpTo ->
            val dist = (jumpFrom.row - jumpTo.row).absoluteValue + (jumpFrom.col - jumpTo.col).absoluteValue
            val len = (distToStart[jumpFrom] ?: 10000000) + (distToEnd[jumpTo] ?: 10000000)
            len <= noCheat - minimumGain - dist
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

val test = """
    ###############
    #...#...#.....#
    #.#.#.#.#.###.#
    #S#...#.#.#...#
    #######.#.#.###
    #######.#.#...#
    #######.#.###.#
    ###..E#...#...#
    ###.#######.###
    #...###...#...#
    #.#####.#.###.#
    #.#...#.#.#...#
    #.#.#.#.#.#.###
    #...#...#...###
    ###############
""".trimIndent()

fun main() {
    go(8) { part1(parse(test), 12) }
    go(3) { part2(parse(test), 76) }
    val text = readAllText("local/day20_input.txt")
    val input = parse(text)
    go(1293) { part1(input) }
    go(977747) { part2(input) }
    TODO()
    measure(text, parse = ::parse, part1 = ::part1, part2 = ::part2)
}

