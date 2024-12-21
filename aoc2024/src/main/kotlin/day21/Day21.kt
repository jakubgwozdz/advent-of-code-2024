package day21

import go
import linesWithoutLastBlanks
import measure
import readAllText

typealias Input = List<String>

data class Pos(val row: Int, val col: Int) {
    operator fun plus(c: Char) = when (c) {
        '^' -> copy(row = row - 1)
        'v' -> copy(row = row + 1)
        '<' -> copy(col = col - 1)
        '>' -> copy(col = col + 1)
        else -> error("Invalid direction: $c")
    }
}

data class KeyPad(val keys: List<Pair<Pos, Char>>) {
    private val byKey = keys.associate { (k, v) -> v to k }
    private val byPos = keys.toMap()
    operator fun get(c: Char) = byKey[c]!!
    operator fun get(pos: Pos) = byPos[pos]!!
    operator fun contains(pos: Pos) = pos in byPos

    fun perform(pos: Pos, c: Char): Pair<Pos, Char?> = when (c) {
        in "v^<>" -> pos + c to null
        'A' -> pos to this[pos]
        else -> error("Invalid char: $c")
    }

    fun shortestPath(start: Pos, end: Pos): List<Char> {
        val queue = mutableListOf(start to emptyList<Char>())
        val visited = mutableSetOf(start)
        while (queue.isNotEmpty()) {
            val (pos, path) = queue.removeFirst()
            if (pos == end) return path
            for (c in "><^v") {
                val newPos = pos + c
                if (newPos in this && newPos !in visited) {
                    visited += newPos
                    queue += newPos to path + c
                }
            }
        }
        error("No path found")
    }

    fun shortestPath(code: String) =
        "A$code".zipWithNext().map { (from, to) -> shortestPath(this[from], this[to]) + 'A' }.flatten()
            .joinToString("")
}

val directional = """
    .^A
    <v>
""".trimIndent().lines().mapIndexed { row, line ->
    line.mapIndexedNotNull { col, c ->
        if (c == '.') null else Pos(row, col) to c
    }
}.flatten().let { KeyPad(it) }.also { println(it) }

val numeric = """
    789
    456
    123
    .0A
""".trimIndent().lines().mapIndexed { row, line ->
    line.mapIndexedNotNull { col, c ->
        if (c == '.') null else Pos(row, col) to c
    }
}.flatten().let { KeyPad(it) }.also { println(it) }

data class Robot(val keyPad: KeyPad, val pos: Pos = keyPad['A'])

data class State(
    val p0presses: String = "",
    val p1pos: Pos = directional['A'],
    val p1presses: String = "",
    val p2pos: Pos = directional['A'],
    val p2presses: String = "",
    val p3pos: Pos = numeric['A'],
    val p3presses: String = "",
) {
    fun isLegal() = p1pos in directional && p2pos in directional && p3pos in numeric
    fun addPress(c: Char): State {
        val newP0presses = p0presses + c
        val (newP1pos, newP1press) = directional.perform(p1pos, c)
        if (newP1press == null) return copy(p0presses = newP0presses, p1pos = newP1pos)
        val (newP2pos, newP2press) = directional.perform(p2pos, newP1press)
        if (newP2press == null) return copy(
            p0presses = newP0presses,
            p1pos = newP1pos,
            p1presses = p1presses + newP1press,
            p2pos = newP2pos
        )
        val (newP3pos, newP3press) = numeric.perform(p3pos, newP2press)
        if (newP3press == null) return copy(
            p0presses = newP0presses,
            p1pos = newP1pos,
            p1presses = p1presses + newP1press,
            p2pos = newP2pos,
            p2presses = p2presses + newP2press,
            p3pos = newP3pos
        ) else return copy(
            p0presses = newP0presses,
            p1pos = newP1pos,
            p1presses = p1presses + newP1press,
            p2pos = newP2pos,
            p2presses = p2presses + newP2press,
            p3pos = newP3pos,
            p3presses = p3presses + newP3press
        )
    }
}

fun shortestPath(code: String, robots: List<Robot>): String {
    println("code: $code")
    val r0 = numeric.shortestPath(code)
    println("r0: $r0 (${r0.length})")
    val r1 = directional.shortestPath(r0)
    println("r1: $r1 (${r1.length})")
    val r2 = directional.shortestPath(r1)
    println("r2: $r2 (${r2.length})")
    return r2
}
//<vA<AA>>^AvAA<^A>A<v<A>>^AvA^A<vA>^A<v<A>^A>AAvA^A<v<A>A>^AAAvA<^A>A
//v<A<AA>^>AvA^<A>vA^Av<<A>^>AvA^Av<<A>^>AAvA<A^>A<A>Av<A<A>^>AAA<A>vA^A

fun part1(input: Input) = input.sumOf {
    val numPart = it.substringBefore('A').toInt()
    val shortest = shortestPath(it, listOf(Robot(numeric), Robot(directional), Robot(directional)))
    numPart * shortest.length
}

fun part2(input: Input) = part1(input).also { TODO() }

fun parse(text: String): Input = text.linesWithoutLastBlanks()

val test = """
    029A
    980A
    179A
    456A
    379A
""".trimIndent()

fun main() {
    go(126384) { part1(parse(test)) }
    val text = readAllText("local/day21_input.txt")
    val input = parse(text)
    go() { part1(input) }
    go() { part2(input) }
    TODO()
    measure(text, parse = ::parse, part1 = ::part1, part2 = ::part2)
}

