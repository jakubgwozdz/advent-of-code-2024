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

data class KeyPad(val keys: List<Pair<Pos, Char>>, val steps: Map<Pair<Char, Char>, String>) {
    private val byKey = keys.associate { (k, v) -> v to k }
    private val byPos = keys.toMap()
    operator fun get(c: Char) = byKey[c]!!
    operator fun get(pos: Pos) = byPos[pos]!!
    operator fun contains(pos: Pos) = pos in byPos

    fun shortestPreCalc(startC: Char, endC: Char) = steps[startC to endC]!!

    fun shortestCalc(startC: Char, endC: Char): Sequence<String> = sequence {
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

    fun shortestPaths(code: String): Sequence<String> =
        "A$code".asSequence().zipWithNext().map { (from, to) -> shortestCalc(from, to) }
            .reduce { acc, list -> acc.flatMap { a -> list.map { b -> a + b } } }

    fun shortestPathSingle(code: String): String =
        "A$code".asSequence().zipWithNext().map { (from, to) -> shortestPreCalc(from, to) }
            .reduce { acc, list -> acc + list }

}

sealed interface Node {
    fun unroll(): Unrolled
    fun size(): Long
}

data class Path(val steps: String) : Node {
    override fun unroll(): Unrolled {
        val nodes = "A$steps".zipWithNext()
            .groupingBy { it }.eachCount()
            .map { (fromTo, count) ->
                val (from, to) = fromTo
                val path = directional.shortestPreCalc(from, to)
                Path(path) to count.toLong()
            }
        return Unrolled(nodes)
    }

    override fun size() = steps.length.toLong()
}

data class Unrolled(val nodes: List<Pair<Path, Long>>) : Node {
    override fun unroll(): Unrolled {
        val nodes1 = nodes.map { (node, count) -> node.unroll() to count }
            .flatMap { (unrolled, count) -> unrolled.nodes.map { it.first to it.second * count } }
            .groupBy { (path) -> path }
            .mapValues { (_, list) -> list.sumOf { it.second } }
            .toList()
        return Unrolled(nodes1)
    }

    override fun size(): Long = nodes.sumOf { (node, count) -> node.size() * count }
}

fun shortestPath(code: String, intermediate: Int): Long {
    var seq: Node = Path(numeric.shortestPathSingle(code))
    repeat(intermediate) {
        seq = seq.unroll()
    }
    return seq.size()
}

fun part1(input: Input) = input.sumOf {
    val numPart = it.substringBefore('A').toInt()
    val shortest = shortestPath(it, 2)
    numPart * shortest
}

fun part2(input: Input) = input.sumOf {
    val numPart = it.substringBefore('A').toInt()
    val shortest = shortestPath(it, 25)
    numPart * shortest
}

fun parse(text: String): Input = text.linesWithoutLastBlanks()

fun main() {

//    printCode(directional, "directionalSteps", "A^>v<")
//    printCode(numeric, "numericSteps", "A0123456789")

    val text = readAllText("local/day21_input.txt")
    val input = parse(text)
    go(157892) { part1(input) }
    go(197015606336332) { part2(input) }
//    TODO()
    measure(text, parse = ::parse, part1 = ::part1, part2 = ::part2)
}


private fun printCode(keyPad: KeyPad, valName: String, keys: String) {
    println("val $valName = mapOf(")
    keys.forEach { from ->
        keys.forEach { to ->
            val possible = keyPad.shortestCalc(from, to)
            val byLen = possible.groupBy {
                var s = it
                repeat(5) { s = directional.shortestPathSingle(s) }
                s.length
            }
            val min = byLen.keys.min()
            val path = byLen[min]!!.single()
            println("  ('$from' to '$to') to \"$path\",")
        }
    }
    println(")")
}


val directionalSteps = mapOf(
    ('A' to 'A') to "A",
    ('A' to '^') to "<A",
    ('A' to '>') to "vA",
    ('A' to 'v') to "<vA",
    ('A' to '<') to "v<<A",
    ('^' to 'A') to ">A",
    ('^' to '^') to "A",
    ('^' to '>') to "v>A",
    ('^' to 'v') to "vA",
    ('^' to '<') to "v<A",
    ('>' to 'A') to "^A",
    ('>' to '^') to "<^A",
    ('>' to '>') to "A",
    ('>' to 'v') to "<A",
    ('>' to '<') to "<<A",
    ('v' to 'A') to "^>A",
    ('v' to '^') to "^A",
    ('v' to '>') to ">A",
    ('v' to 'v') to "A",
    ('v' to '<') to "<A",
    ('<' to 'A') to ">>^A",
    ('<' to '^') to ">^A",
    ('<' to '>') to ">>A",
    ('<' to 'v') to ">A",
    ('<' to '<') to "A",
)
val numericSteps = mapOf(
    ('A' to 'A') to "A",
    ('A' to '0') to "<A",
    ('A' to '1') to "^<<A",
    ('A' to '2') to "<^A",
    ('A' to '3') to "^A",
    ('A' to '4') to "^^<<A",
    ('A' to '5') to "<^^A",
    ('A' to '6') to "^^A",
    ('A' to '7') to "^^^<<A",
    ('A' to '8') to "<^^^A",
    ('A' to '9') to "^^^A",
    ('0' to 'A') to ">A",
    ('0' to '0') to "A",
    ('0' to '1') to "^<A",
    ('0' to '2') to "^A",
    ('0' to '3') to "^>A",
    ('0' to '4') to "^^<A",
    ('0' to '5') to "^^A",
    ('0' to '6') to "^^>A",
    ('0' to '7') to "^^^<A",
    ('0' to '8') to "^^^A",
    ('0' to '9') to "^^^>A",
    ('1' to 'A') to ">>vA",
    ('1' to '0') to ">vA",
    ('1' to '1') to "A",
    ('1' to '2') to ">A",
    ('1' to '3') to ">>A",
    ('1' to '4') to "^A",
    ('1' to '5') to "^>A",
    ('1' to '6') to "^>>A",
    ('1' to '7') to "^^A",
    ('1' to '8') to "^^>A",
    ('1' to '9') to "^^>>A",
    ('2' to 'A') to "v>A",
    ('2' to '0') to "vA",
    ('2' to '1') to "<A",
    ('2' to '2') to "A",
    ('2' to '3') to ">A",
    ('2' to '4') to "<^A",
    ('2' to '5') to "^A",
    ('2' to '6') to "^>A",
    ('2' to '7') to "<^^A",
    ('2' to '8') to "^^A",
    ('2' to '9') to "^^>A",
    ('3' to 'A') to "vA",
    ('3' to '0') to "<vA",
    ('3' to '1') to "<<A",
    ('3' to '2') to "<A",
    ('3' to '3') to "A",
    ('3' to '4') to "<<^A",
    ('3' to '5') to "<^A",
    ('3' to '6') to "^A",
    ('3' to '7') to "<<^^A",
    ('3' to '8') to "<^^A",
    ('3' to '9') to "^^A",
    ('4' to 'A') to ">>vvA",
    ('4' to '0') to ">vvA",
    ('4' to '1') to "vA",
    ('4' to '2') to "v>A",
    ('4' to '3') to "v>>A",
    ('4' to '4') to "A",
    ('4' to '5') to ">A",
    ('4' to '6') to ">>A",
    ('4' to '7') to "^A",
    ('4' to '8') to "^>A",
    ('4' to '9') to "^>>A",
    ('5' to 'A') to "vv>A",
    ('5' to '0') to "vvA",
    ('5' to '1') to "<vA",
    ('5' to '2') to "vA",
    ('5' to '3') to "v>A",
    ('5' to '4') to "<A",
    ('5' to '5') to "A",
    ('5' to '6') to ">A",
    ('5' to '7') to "<^A",
    ('5' to '8') to "^A",
    ('5' to '9') to "^>A",
    ('6' to 'A') to "vvA",
    ('6' to '0') to "<vvA",
    ('6' to '1') to "<<vA",
    ('6' to '2') to "<vA",
    ('6' to '3') to "vA",
    ('6' to '4') to "<<A",
    ('6' to '5') to "<A",
    ('6' to '6') to "A",
    ('6' to '7') to "<<^A",
    ('6' to '8') to "<^A",
    ('6' to '9') to "^A",
    ('7' to 'A') to ">>vvvA",
    ('7' to '0') to ">vvvA",
    ('7' to '1') to "vvA",
    ('7' to '2') to "vv>A",
    ('7' to '3') to "vv>>A",
    ('7' to '4') to "vA",
    ('7' to '5') to "v>A",
    ('7' to '6') to "v>>A",
    ('7' to '7') to "A",
    ('7' to '8') to ">A",
    ('7' to '9') to ">>A",
    ('8' to 'A') to "vvv>A",
    ('8' to '0') to "vvvA",
    ('8' to '1') to "<vvA",
    ('8' to '2') to "vvA",
    ('8' to '3') to "vv>A",
    ('8' to '4') to "<vA",
    ('8' to '5') to "vA",
    ('8' to '6') to "v>A",
    ('8' to '7') to "<A",
    ('8' to '8') to "A",
    ('8' to '9') to ">A",
    ('9' to 'A') to "vvvA",
    ('9' to '0') to "<vvvA",
    ('9' to '1') to "<<vvA",
    ('9' to '2') to "<vvA",
    ('9' to '3') to "vvA",
    ('9' to '4') to "<<vA",
    ('9' to '5') to "<vA",
    ('9' to '6') to "vA",
    ('9' to '7') to "<<A",
    ('9' to '8') to "<A",
    ('9' to '9') to "A",
)

val directional = """
    .^A
    <v>
""".trimIndent().lines().mapIndexed { row, line ->
    line.mapIndexedNotNull { col, c ->
        if (c == '.') null else Pos(row, col) to c
    }
}.flatten().let { KeyPad(it, directionalSteps) }

val numeric = """
    789
    456
    123
    .0A
""".trimIndent().lines().mapIndexed { row, line ->
    line.mapIndexedNotNull { col, c ->
        if (c == '.') null else Pos(row, col) to c
    }
}.flatten().let { KeyPad(it, numericSteps) }

