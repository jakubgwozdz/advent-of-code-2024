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

    fun shortestPaths(startC: Char, endC: Char): Sequence<String> = sequence {
        val start = get(startC)
        val end = get(endC)
        val queue = mutableListOf(Pair(start, ""))
        while (queue.isNotEmpty()) {
            val (pos, path) = queue.removeFirst()
            if (pos == end) yield(path + 'A')
            if (pos.row < end.row) {
                val c = 'v'
                val newPos = pos + c
                if (newPos in this@KeyPad) queue += Pair(newPos, path + c)
            }
            if (pos.row > end.row) {
                val c = '^'
                val newPos = pos + c
                if (newPos in this@KeyPad) queue += Pair(newPos, path + c)
            }
            if (pos.col < end.col) {
                val c = '>'
                val newPos = pos + c
                if (newPos in this@KeyPad) queue += Pair(newPos, path + c)
            }
            if (pos.col > end.col) {
                val c = '<'
                val newPos = pos + c
                if (newPos in this@KeyPad) queue += Pair(newPos, path + c)
            }
        }
    }

    fun minSteps(code: String): Int =
        "A$code".zipWithNext().sumOf { (from, to) -> shortestPaths(from, to).first().length }

    fun shortestPaths(code: String): List<String> =
        "A$code".asSequence().zipWithNext().map { (from, to) -> shortestPaths(from, to) }
            .reduce { acc, list -> acc.flatMap { a -> list.map { b -> a + b } } }
            .toList()

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

// <v<A>>^AA<vA<A>>^AAvAA<^A>A<vA>^A<A>A<vA>^A<A>A<v<A>A>^AAvA<^A>A
// <AAv<AA>>^A

fun shortestPath(code: String, robots: List<Robot>): Int =
    numeric.shortestPaths(code)
        .flatMap { directional.shortestPaths(it) }
        .minOf { directional.minSteps(it) }

fun part1(input: Input) = input.sumOf {
    val numPart = it.substringBefore('A').toInt()
    val shortest = shortestPath(it, listOf(Robot(numeric), Robot(directional), Robot(directional)))
    println("$it: $shortest * $numPart")
    numPart * shortest
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

    println("directional:")
    directional.keys.forEach { (_, from) ->
        directional.keys.forEach { (_, to) ->
            println(
                "$from -> $to: ${
                    directional.shortestPaths(from, to).toList().map { it to directional.minSteps(it) }
                }"
            )
        }
    }
    println()
    println("numeric:")
    numeric.keys.forEach { (_, from) ->
        numeric.keys.forEach { (_, to) ->
            println("$from -> $to: ${numeric.shortestPaths(from, to).toList().map { it to directional.minSteps(it) }}")
        }
    }

    go(126384) { part1(parse(test)) }
    val text = readAllText("local/day21_input.txt")
    val input = parse(text)
    go(157892) { part1(input) }
    go() { part2(input) }
    TODO()
    measure(text, parse = ::parse, part1 = ::part1, part2 = ::part2)
}

