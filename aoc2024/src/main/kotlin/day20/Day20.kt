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
    val normal = dijkstra(input)!!

    return input.allowed.asSequence()
        .flatMap { pos ->
            pos.neighbors2().filter { (pos1, pos2) -> pos1 !in input.allowed && pos2 in input.allowed }
                .map { (pos1, pos2) -> Cheat(pos, pos2, 2) }
        }
        .count { cheat ->
            dijkstra(input, normal - minimumGain, cheat) != null
        }


}
// 2356522 too high
fun part2(input: Input, minimumGain: Int = 100): Int {
    val normal = dijkstra(input)!!
        .also { println("normal: $it") }
    return input.allowed.asSequence()
        .flatMapIndexed { i, pos ->
            println("$i of " + input.allowed.size)
            val (r, c) = pos
            (r - 20..r + 20).flatMap { r1 ->
                (c - 20..c + 20)
                    .map { c1 -> r1 to (r1 - r).absoluteValue + (c1 - c).absoluteValue }
                    .filter { (_, dist) -> dist in 2..20 }
                    .map { (c1, dist) -> Pos(r1, c1) to dist }
            }.filter { (pos1) -> pos1 in input.allowed }
//
//
//            pos.neighbors2().filter { (pos1, pos2) -> pos1 !in input.allowed && pos2 in input.allowed }
                .map { (pos1, dist) -> Cheat(pos, pos1,dist) }
        }
        .count { cheat ->
            dijkstra(input, normal - minimumGain, cheat) != null
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
    val text = readAllText("local/day20_input.txt")
    val input = parse(text)
    go(1293) { part1(input) }
    go() { part2(input) }
    TODO()
    measure(text, parse = ::parse, part1 = ::part1, part2 = ::part2)
}

