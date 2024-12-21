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

data class KeyPad(val keys: List<Pair<Pos, Char>>, val steps: Map<Pair<Char, Char>, List<String>>) {
    private val byKey = keys.associate { (k, v) -> v to k }
    private val byPos = keys.toMap()
    operator fun get(c: Char) = byKey[c]!!
    operator fun get(pos: Pos) = byPos[pos]!!
    operator fun contains(pos: Pos) = pos in byPos

    fun shortestPreCalc(startC: Char, endC: Char) = steps[startC to endC]!!.asSequence()

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
        "A$code".asSequence().zipWithNext().map { (from, to) -> shortestPreCalc(from, to) }
            .reduce { acc, list -> acc.flatMap { a -> list.map { b -> a + b } } }

    fun shortestPaths2(code: String): String =
        "A$code".asSequence().zipWithNext().map { (from, to) -> shortestPreCalc(from, to).single() }
            .reduce { acc, list -> acc+list }

}

fun shortestPath(code: String, intermediate: Int): Int {
    var seq = numeric.shortestPaths(code)
    repeat(intermediate) {
        seq = seq.map { directional.shortestPaths2(it) }
    }
    return seq.minOf { it.length }

//    return seq
////        .flatMap { directional.shortestPaths(it) }
//        .flatMap { directional.shortestPaths(it) }
//        .minOf { directional.shortestPaths(it).first().length }
}

fun part1(input: Input) = input.sumOf {
    val numPart = it.substringBefore('A').toInt()
    val shortest = shortestPath(it, 2)
    println("$it: $shortest * $numPart")
    numPart * shortest
}

fun part2(input: Input) = input.sumOf {
    val numPart = it.substringBefore('A').toInt()
    val shortest = shortestPath(it, 25)
    println("$it: $shortest * $numPart")
    numPart * shortest
}

fun parse(text: String): Input = text.linesWithoutLastBlanks()

val test = """
    029A
    980A
    179A
    456A
    379A
""".trimIndent()

fun main() {

//    printCode(directional, "directionalSteps", "A^>v<")
//    printCode(numeric, "numericSteps", "A0123456789")

    "^>v<".forEach { char ->
        val str = "${char}A"
        println("cost of $str:")
        directional.shortestPaths(str).forEach { s1 ->
            println("  $s1 (${s1.length}):")
            directional.shortestPaths(s1).forEach { s2 ->
                println("    $s2 (${s2.length}):")
                directional.shortestPaths(s2).forEach { s3 ->
                    println("      $s3 (${s3.length}):")
                    directional.shortestPaths(s3).forEach { s4 ->
                        println("        $s4 (${s4.length}):")
                    }
                }
            }
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

private fun printCode(keyPad: KeyPad, valName: String, keys: String) {
    println("val $valName = mapOf(")
    keys.forEach { from ->
        keys.forEach { to ->
            println("  ('$from' to '$to') to listOf(")
            keyPad.shortestCalc(from, to).forEach {
                println("    \"${it}\", // ${directional.shortestPaths(it).toList()}")
            }
            println("  ),")
        }
    }
    println(")")
}


val directionalSteps = mapOf(
    ('A' to 'A') to listOf(
        "A", // [A]
    ),
    ('A' to '^') to listOf(
        "<A", // [v<<A>^>A, v<<A>>^A, <v<A>^>A, <v<A>>^A]
    ),
    ('A' to '>') to listOf(
        "vA", // [v<A^>A, v<A>^A, <vA^>A, <vA>^A]
    ),
    ('A' to 'v') to listOf(
//        "v<A", // [v<A<A>^>A, v<A<A>>^A, <vA<A>^>A, <vA<A>>^A]
        "<vA", // [v<<A>A^>A, v<<A>A>^A, <v<A>A^>A, <v<A>A>^A]
    ),
    ('A' to '<') to listOf(
        "v<<A", // [v<A<AA>^>A, v<A<AA>>^A, <vA<AA>^>A, <vA<AA>>^A]
//        "<v<A", // [v<<A>A<A>^>A, v<<A>A<A>>^A, <v<A>A<A>^>A, <v<A>A<A>>^A]
    ),
    ('^' to 'A') to listOf(
        ">A", // [vA^A]
    ),
    ('^' to '^') to listOf(
        "A", // [A]
    ),
    ('^' to '>') to listOf(
        "v>A", // [v<A>A^A, <vA>A^A]
//        ">vA", // [vA<A^>A, vA<A>^A]
    ),
    ('^' to 'v') to listOf(
        "vA", // [v<A^>A, v<A>^A, <vA^>A, <vA>^A]
    ),
    ('^' to '<') to listOf(
        "v<A", // [v<A<A>^>A, v<A<A>>^A, <vA<A>^>A, <vA<A>>^A]
    ),
    ('>' to 'A') to listOf(
        "^A", // [<A>A]
    ),
    ('>' to '^') to listOf(
        "^<A", // [<Av<A>^>A, <Av<A>>^A]
//        "<^A", // [v<<A>^A>A, <v<A>^A>A]
    ),
    ('>' to '>') to listOf(
        "A", // [A]
    ),
    ('>' to 'v') to listOf(
        "<A", // [v<<A>^>A, v<<A>>^A, <v<A>^>A, <v<A>>^A]
    ),
    ('>' to '<') to listOf(
        "<<A", // [v<<AA>^>A, v<<AA>>^A, <v<AA>^>A, <v<AA>>^A]
    ),
    ('v' to 'A') to listOf(
        "^>A", // [<Av>A^A, <A>vA^A]
//        ">^A", // [vA^<A>A, vA<^A>A]
    ),
    ('v' to '^') to listOf(
        "^A", // [<A>A]
    ),
    ('v' to '>') to listOf(
        ">A", // [vA^A]
    ),
    ('v' to 'v') to listOf(
        "A", // [A]
    ),
    ('v' to '<') to listOf(
        "<A", // [v<<A>^>A, v<<A>>^A, <v<A>^>A, <v<A>>^A]
    ),
    ('<' to 'A') to listOf(
//        ">^>A", // [vA^<Av>A^A, vA^<A>vA^A, vA<^Av>A^A, vA<^A>vA^A]
        ">>^A", // [vAA^<A>A, vAA<^A>A]
    ),
    ('<' to '^') to listOf(
        ">^A", // [vA^<A>A, vA<^A>A]
    ),
    ('<' to '>') to listOf(
        ">>A", // [vAA^A]
    ),
    ('<' to 'v') to listOf(
        ">A", // [vA^A]
    ),
    ('<' to '<') to listOf(
        "A", // [A]
    ),
)
val numericSteps = mapOf(
    ('A' to 'A') to listOf(
        "A", // [A]
    ),
    ('A' to '0') to listOf(
        "<A", // [v<<A>^>A, v<<A>>^A, <v<A>^>A, <v<A>>^A]
    ),
    ('A' to '1') to listOf(
        "^<<A", // [<Av<AA>^>A, <Av<AA>>^A]
//        "<^<A", // [v<<A>^Av<A>^>A, v<<A>^Av<A>>^A, <v<A>^Av<A>^>A, <v<A>^Av<A>>^A]
    ),
    ('A' to '2') to listOf(
        "^<A", // [<Av<A>^>A, <Av<A>>^A]
        "<^A", // [v<<A>^A>A, <v<A>^A>A]
    ),
    ('A' to '3') to listOf(
        "^A", // [<A>A]
    ),
    ('A' to '4') to listOf(
        "^^<<A", // [<AAv<AA>^>A, <AAv<AA>>^A]
//        "^<^<A", // [<Av<A>^Av<A>^>A, <Av<A>^Av<A>>^A]
        "^<<^A", // [<Av<AA>^A>A]
//        "<^^<A", // [v<<A>^AAv<A>^>A, v<<A>^AAv<A>>^A, <v<A>^AAv<A>^>A, <v<A>^AAv<A>>^A]
//        "<^<^A", // [v<<A>^Av<A>^A>A, <v<A>^Av<A>^A>A]
    ),
    ('A' to '5') to listOf(
        "^^<A", // [<AAv<A>^>A, <AAv<A>>^A]
        "^<^A", // [<Av<A>^A>A]
        "<^^A", // [v<<A>^AA>A, <v<A>^AA>A]
    ),
    ('A' to '6') to listOf(
        "^^A", // [<AA>A]
    ),
    ('A' to '7') to listOf(
        "^^^<<A", // [<AAAv<AA>^>A, <AAAv<AA>>^A]
//        "^^<^<A", // [<AAv<A>^Av<A>^>A, <AAv<A>^Av<A>>^A]
        "^^<<^A", // [<AAv<AA>^A>A]
//        "^<^^<A", // [<Av<A>^AAv<A>^>A, <Av<A>^AAv<A>>^A]
//        "^<^<^A", // [<Av<A>^Av<A>^A>A]
        "^<<^^A", // [<Av<AA>^AA>A]
//        "<^^^<A", // [v<<A>^AAAv<A>^>A, v<<A>^AAAv<A>>^A, <v<A>^AAAv<A>^>A, <v<A>^AAAv<A>>^A]
//        "<^^<^A", // [v<<A>^AAv<A>^A>A, <v<A>^AAv<A>^A>A]
//        "<^<^^A", // [v<<A>^Av<A>^AA>A, <v<A>^Av<A>^AA>A]
    ),
    ('A' to '8') to listOf(
        "^^^<A", // [<AAAv<A>^>A, <AAAv<A>>^A]
        "^^<^A", // [<AAv<A>^A>A]
        "^<^^A", // [<Av<A>^AA>A]
        "<^^^A", // [v<<A>^AAA>A, <v<A>^AAA>A]
    ),
    ('A' to '9') to listOf(
        "^^^A", // [<AAA>A]
    ),
    ('0' to 'A') to listOf(
        ">A", // [vA^A]
    ),
    ('0' to '0') to listOf(
        "A", // [A]
    ),
    ('0' to '1') to listOf(
        "^<A", // [<Av<A>^>A, <Av<A>>^A]
    ),
    ('0' to '2') to listOf(
        "^A", // [<A>A]
    ),
    ('0' to '3') to listOf(
        "^>A", // [<Av>A^A, <A>vA^A]
        ">^A", // [vA^<A>A, vA<^A>A]
    ),
    ('0' to '4') to listOf(
        "^^<A", // [<AAv<A>^>A, <AAv<A>>^A]
        "^<^A", // [<Av<A>^A>A]
    ),
    ('0' to '5') to listOf(
        "^^A", // [<AA>A]
    ),
    ('0' to '6') to listOf(
        "^^>A", // [<AAv>A^A, <AA>vA^A]
//        "^>^A", // [<Av>A^<A>A, <Av>A<^A>A, <A>vA^<A>A, <A>vA<^A>A]
        ">^^A", // [vA^<AA>A, vA<^AA>A]
    ),
    ('0' to '7') to listOf(
        "^^^<A", // [<AAAv<A>^>A, <AAAv<A>>^A]
        "^^<^A", // [<AAv<A>^A>A]
        "^<^^A", // [<Av<A>^AA>A]
    ),
    ('0' to '8') to listOf(
        "^^^A", // [<AAA>A]
    ),
    ('0' to '9') to listOf(
        "^^^>A", // [<AAAv>A^A, <AAA>vA^A]
//        "^^>^A", // [<AAv>A^<A>A, <AAv>A<^A>A, <AA>vA^<A>A, <AA>vA<^A>A]
//        "^>^^A", // [<Av>A^<AA>A, <Av>A<^AA>A, <A>vA^<AA>A, <A>vA<^AA>A]
        ">^^^A", // [vA^<AAA>A, vA<^AAA>A]
    ),
    ('1' to 'A') to listOf(
        ">v>A", // [vA<A>A^A]
        ">>vA", // [vAA<A^>A, vAA<A>^A]
    ),
    ('1' to '0') to listOf(
        ">vA", // [vA<A^>A, vA<A>^A]
    ),
    ('1' to '1') to listOf(
        "A", // [A]
    ),
    ('1' to '2') to listOf(
        ">A", // [vA^A]
    ),
    ('1' to '3') to listOf(
        ">>A", // [vAA^A]
    ),
    ('1' to '4') to listOf(
        "^A", // [<A>A]
    ),
    ('1' to '5') to listOf(
        "^>A", // [<Av>A^A, <A>vA^A]
        ">^A", // [vA^<A>A, vA<^A>A]
    ),
    ('1' to '6') to listOf(
        "^>>A", // [<Av>AA^A, <A>vAA^A]
//        ">^>A", // [vA^<Av>A^A, vA^<A>vA^A, vA<^Av>A^A, vA<^A>vA^A]
        ">>^A", // [vAA^<A>A, vAA<^A>A]
    ),
    ('1' to '7') to listOf(
        "^^A", // [<AA>A]
    ),
    ('1' to '8') to listOf(
        "^^>A", // [<AAv>A^A, <AA>vA^A]
//        "^>^A", // [<Av>A^<A>A, <Av>A<^A>A, <A>vA^<A>A, <A>vA<^A>A]
        ">^^A", // [vA^<AA>A, vA<^AA>A]
    ),
    ('1' to '9') to listOf(
        "^^>>A", // [<AAv>AA^A, <AA>vAA^A]
//        "^>^>A", // [<Av>A^<Av>A^A, <Av>A^<A>vA^A, <Av>A<^Av>A^A, <Av>A<^A>vA^A, <A>vA^<Av>A^A, <A>vA^<A>vA^A, <A>vA<^Av>A^A, <A>vA<^A>vA^A]
//        "^>>^A", // [<Av>AA^<A>A, <Av>AA<^A>A, <A>vAA^<A>A, <A>vAA<^A>A]
//        ">^^>A", // [vA^<AAv>A^A, vA^<AA>vA^A, vA<^AAv>A^A, vA<^AA>vA^A]
//        ">^>^A", // [vA^<Av>A^<A>A, vA^<Av>A<^A>A, vA^<A>vA^<A>A, vA^<A>vA<^A>A, vA<^Av>A^<A>A, vA<^Av>A<^A>A, vA<^A>vA^<A>A, vA<^A>vA<^A>A]
        ">>^^A", // [vAA^<AA>A, vAA<^AA>A]
    ),
    ('2' to 'A') to listOf(
        "v>A", // [v<A>A^A, <vA>A^A]
        ">vA", // [vA<A^>A, vA<A>^A]
    ),
    ('2' to '0') to listOf(
        "vA", // [v<A^>A, v<A>^A, <vA^>A, <vA>^A]
    ),
    ('2' to '1') to listOf(
        "<A", // [v<<A>^>A, v<<A>>^A, <v<A>^>A, <v<A>>^A]
    ),
    ('2' to '2') to listOf(
        "A", // [A]
    ),
    ('2' to '3') to listOf(
        ">A", // [vA^A]
    ),
    ('2' to '4') to listOf(
        "^<A", // [<Av<A>^>A, <Av<A>>^A]
        "<^A", // [v<<A>^A>A, <v<A>^A>A]
    ),
    ('2' to '5') to listOf(
        "^A", // [<A>A]
    ),
    ('2' to '6') to listOf(
        "^>A", // [<Av>A^A, <A>vA^A]
        ">^A", // [vA^<A>A, vA<^A>A]
    ),
    ('2' to '7') to listOf(
        "^^<A", // [<AAv<A>^>A, <AAv<A>>^A]
        "^<^A", // [<Av<A>^A>A]
        "<^^A", // [v<<A>^AA>A, <v<A>^AA>A]
    ),
    ('2' to '8') to listOf(
        "^^A", // [<AA>A]
    ),
    ('2' to '9') to listOf(
        "^^>A", // [<AAv>A^A, <AA>vA^A]
//        "^>^A", // [<Av>A^<A>A, <Av>A<^A>A, <A>vA^<A>A, <A>vA<^A>A]
        ">^^A", // [vA^<AA>A, vA<^AA>A]
    ),
    ('3' to 'A') to listOf(
        "vA", // [v<A^>A, v<A>^A, <vA^>A, <vA>^A]
    ),
    ('3' to '0') to listOf(
        "v<A", // [v<A<A>^>A, v<A<A>>^A, <vA<A>^>A, <vA<A>>^A]
        "<vA", // [v<<A>A^>A, v<<A>A>^A, <v<A>A^>A, <v<A>A>^A]
    ),
    ('3' to '1') to listOf(
        "<<A", // [v<<AA>^>A, v<<AA>>^A, <v<AA>^>A, <v<AA>>^A]
    ),
    ('3' to '2') to listOf(
        "<A", // [v<<A>^>A, v<<A>>^A, <v<A>^>A, <v<A>>^A]
    ),
    ('3' to '3') to listOf(
        "A", // [A]
    ),
    ('3' to '4') to listOf(
        "^<<A", // [<Av<AA>^>A, <Av<AA>>^A]
//        "<^<A", // [v<<A>^Av<A>^>A, v<<A>^Av<A>>^A, <v<A>^Av<A>^>A, <v<A>^Av<A>>^A]
        "<<^A", // [v<<AA>^A>A, <v<AA>^A>A]
    ),
    ('3' to '5') to listOf(
        "^<A", // [<Av<A>^>A, <Av<A>>^A]
        "<^A", // [v<<A>^A>A, <v<A>^A>A]
    ),
    ('3' to '6') to listOf(
        "^A", // [<A>A]
    ),
    ('3' to '7') to listOf(
        "^^<<A", // [<AAv<AA>^>A, <AAv<AA>>^A]
//        "^<^<A", // [<Av<A>^Av<A>^>A, <Av<A>^Av<A>>^A]
        "^<<^A", // [<Av<AA>^A>A]
//        "<^^<A", // [v<<A>^AAv<A>^>A, v<<A>^AAv<A>>^A, <v<A>^AAv<A>^>A, <v<A>^AAv<A>>^A]
//        "<^<^A", // [v<<A>^Av<A>^A>A, <v<A>^Av<A>^A>A]
        "<<^^A", // [v<<AA>^AA>A, <v<AA>^AA>A]
    ),
    ('3' to '8') to listOf(
        "^^<A", // [<AAv<A>^>A, <AAv<A>>^A]
        "^<^A", // [<Av<A>^A>A]
        "<^^A", // [v<<A>^AA>A, <v<A>^AA>A]
    ),
    ('3' to '9') to listOf(
        "^^A", // [<AA>A]
    ),
    ('4' to 'A') to listOf(
//        "v>v>A", // [v<A>A<A>A^A, <vA>A<A>A^A]
//        "v>>vA", // [v<A>AA<A^>A, v<A>AA<A>^A, <vA>AA<A^>A, <vA>AA<A>^A]
        ">vv>A", // [vA<AA>A^A]
//        ">v>vA", // [vA<A>A<A^>A, vA<A>A<A>^A]
        ">>vvA", // [vAA<AA^>A, vAA<AA>^A]
    ),
    ('4' to '0') to listOf(
//        "v>vA", // [v<A>A<A^>A, v<A>A<A>^A, <vA>A<A^>A, <vA>A<A>^A]
        ">vvA", // [vA<AA^>A, vA<AA>^A]
    ),
    ('4' to '1') to listOf(
        "vA", // [v<A^>A, v<A>^A, <vA^>A, <vA>^A]
    ),
    ('4' to '2') to listOf(
        "v>A", // [v<A>A^A, <vA>A^A]
        ">vA", // [vA<A^>A, vA<A>^A]
    ),
    ('4' to '3') to listOf(
        "v>>A", // [v<A>AA^A, <vA>AA^A]
        ">v>A", // [vA<A>A^A]
        ">>vA", // [vAA<A^>A, vAA<A>^A]
    ),
    ('4' to '4') to listOf(
        "A", // [A]
    ),
    ('4' to '5') to listOf(
        ">A", // [vA^A]
    ),
    ('4' to '6') to listOf(
        ">>A", // [vAA^A]
    ),
    ('4' to '7') to listOf(
        "^A", // [<A>A]
    ),
    ('4' to '8') to listOf(
        "^>A", // [<Av>A^A, <A>vA^A]
        ">^A", // [vA^<A>A, vA<^A>A]
    ),
    ('4' to '9') to listOf(
        "^>>A", // [<Av>AA^A, <A>vAA^A]
//        ">^>A", // [vA^<Av>A^A, vA^<A>vA^A, vA<^Av>A^A, vA<^A>vA^A]
        ">>^A", // [vAA^<A>A, vAA<^A>A]
    ),
    ('5' to 'A') to listOf(
        "vv>A", // [v<AA>A^A, <vAA>A^A]
//        "v>vA", // [v<A>A<A^>A, v<A>A<A>^A, <vA>A<A^>A, <vA>A<A>^A]
        ">vvA", // [vA<AA^>A, vA<AA>^A]
    ),
    ('5' to '0') to listOf(
        "vvA", // [v<AA^>A, v<AA>^A, <vAA^>A, <vAA>^A]
    ),
    ('5' to '1') to listOf(
        "v<A", // [v<A<A>^>A, v<A<A>>^A, <vA<A>^>A, <vA<A>>^A]
        "<vA", // [v<<A>A^>A, v<<A>A>^A, <v<A>A^>A, <v<A>A>^A]
    ),
    ('5' to '2') to listOf(
        "vA", // [v<A^>A, v<A>^A, <vA^>A, <vA>^A]
    ),
    ('5' to '3') to listOf(
        "v>A", // [v<A>A^A, <vA>A^A]
        ">vA", // [vA<A^>A, vA<A>^A]
    ),
    ('5' to '4') to listOf(
        "<A", // [v<<A>^>A, v<<A>>^A, <v<A>^>A, <v<A>>^A]
    ),
    ('5' to '5') to listOf(
        "A", // [A]
    ),
    ('5' to '6') to listOf(
        ">A", // [vA^A]
    ),
    ('5' to '7') to listOf(
        "^<A", // [<Av<A>^>A, <Av<A>>^A]
        "<^A", // [v<<A>^A>A, <v<A>^A>A]
    ),
    ('5' to '8') to listOf(
        "^A", // [<A>A]
    ),
    ('5' to '9') to listOf(
        "^>A", // [<Av>A^A, <A>vA^A]
        ">^A", // [vA^<A>A, vA<^A>A]
    ),
    ('6' to 'A') to listOf(
        "vvA", // [v<AA^>A, v<AA>^A, <vAA^>A, <vAA>^A]
    ),
    ('6' to '0') to listOf(
        "vv<A", // [v<AA<A>^>A, v<AA<A>>^A, <vAA<A>^>A, <vAA<A>>^A]
        "v<vA", // [v<A<A>A^>A, v<A<A>A>^A, <vA<A>A^>A, <vA<A>A>^A]
        "<vvA", // [v<<A>AA^>A, v<<A>AA>^A, <v<A>AA^>A, <v<A>AA>^A]
    ),
    ('6' to '1') to listOf(
        "v<<A", // [v<A<AA>^>A, v<A<AA>>^A, <vA<AA>^>A, <vA<AA>>^A]
//        "<v<A", // [v<<A>A<A>^>A, v<<A>A<A>>^A, <v<A>A<A>^>A, <v<A>A<A>>^A]
        "<<vA", // [v<<AA>A^>A, v<<AA>A>^A, <v<AA>A^>A, <v<AA>A>^A]
    ),
    ('6' to '2') to listOf(
        "v<A", // [v<A<A>^>A, v<A<A>>^A, <vA<A>^>A, <vA<A>>^A]
        "<vA", // [v<<A>A^>A, v<<A>A>^A, <v<A>A^>A, <v<A>A>^A]
    ),
    ('6' to '3') to listOf(
        "vA", // [v<A^>A, v<A>^A, <vA^>A, <vA>^A]
    ),
    ('6' to '4') to listOf(
        "<<A", // [v<<AA>^>A, v<<AA>>^A, <v<AA>^>A, <v<AA>>^A]
    ),
    ('6' to '5') to listOf(
        "<A", // [v<<A>^>A, v<<A>>^A, <v<A>^>A, <v<A>>^A]
    ),
    ('6' to '6') to listOf(
        "A", // [A]
    ),
    ('6' to '7') to listOf(
        "^<<A", // [<Av<AA>^>A, <Av<AA>>^A]
//        "<^<A", // [v<<A>^Av<A>^>A, v<<A>^Av<A>>^A, <v<A>^Av<A>^>A, <v<A>^Av<A>>^A]
        "<<^A", // [v<<AA>^A>A, <v<AA>^A>A]
    ),
    ('6' to '8') to listOf(
        "^<A", // [<Av<A>^>A, <Av<A>>^A]
        "<^A", // [v<<A>^A>A, <v<A>^A>A]
    ),
    ('6' to '9') to listOf(
        "^A", // [<A>A]
    ),
    ('7' to 'A') to listOf(
        "vv>v>A", // [v<AA>A<A>A^A, <vAA>A<A>A^A]
        "vv>>vA", // [v<AA>AA<A^>A, v<AA>AA<A>^A, <vAA>AA<A^>A, <vAA>AA<A>^A]
        "v>vv>A", // [v<A>A<AA>A^A, <vA>A<AA>A^A]
//        "v>v>vA", // [v<A>A<A>A<A^>A, v<A>A<A>A<A>^A, <vA>A<A>A<A^>A, <vA>A<A>A<A>^A]
        "v>>vvA", // [v<A>AA<AA^>A, v<A>AA<AA>^A, <vA>AA<AA^>A, <vA>AA<AA>^A]
        ">vvv>A", // [vA<AAA>A^A]
//        ">vv>vA", // [vA<AA>A<A^>A, vA<AA>A<A>^A]
//        ">v>vvA", // [vA<A>A<AA^>A, vA<A>A<AA>^A]
        ">>vvvA", // [vAA<AAA^>A, vAA<AAA>^A]
    ),
    ('7' to '0') to listOf(
//        "vv>vA", // [v<AA>A<A^>A, v<AA>A<A>^A, <vAA>A<A^>A, <vAA>A<A>^A]
//        "v>vvA", // [v<A>A<AA^>A, v<A>A<AA>^A, <vA>A<AA^>A, <vA>A<AA>^A]
        ">vvvA", // [vA<AAA^>A, vA<AAA>^A]
    ),
    ('7' to '1') to listOf(
        "vvA", // [v<AA^>A, v<AA>^A, <vAA^>A, <vAA>^A]
    ),
    ('7' to '2') to listOf(
        "vv>A", // [v<AA>A^A, <vAA>A^A]
//        "v>vA", // [v<A>A<A^>A, v<A>A<A>^A, <vA>A<A^>A, <vA>A<A>^A]
        ">vvA", // [vA<AA^>A, vA<AA>^A]
    ),
    ('7' to '3') to listOf(
        "vv>>A", // [v<AA>AA^A, <vAA>AA^A]
//        "v>v>A", // [v<A>A<A>A^A, <vA>A<A>A^A]
//        "v>>vA", // [v<A>AA<A^>A, v<A>AA<A>^A, <vA>AA<A^>A, <vA>AA<A>^A]
        ">vv>A", // [vA<AA>A^A]
//        ">v>vA", // [vA<A>A<A^>A, vA<A>A<A>^A]
        ">>vvA", // [vAA<AA^>A, vAA<AA>^A]
    ),
    ('7' to '4') to listOf(
        "vA", // [v<A^>A, v<A>^A, <vA^>A, <vA>^A]
    ),
    ('7' to '5') to listOf(
        "v>A", // [v<A>A^A, <vA>A^A]
        ">vA", // [vA<A^>A, vA<A>^A]
    ),
    ('7' to '6') to listOf(
        "v>>A", // [v<A>AA^A, <vA>AA^A]
        ">v>A", // [vA<A>A^A]
        ">>vA", // [vAA<A^>A, vAA<A>^A]
    ),
    ('7' to '7') to listOf(
        "A", // [A]
    ),
    ('7' to '8') to listOf(
        ">A", // [vA^A]
    ),
    ('7' to '9') to listOf(
        ">>A", // [vAA^A]
    ),
    ('8' to 'A') to listOf(
        "vvv>A", // [v<AAA>A^A, <vAAA>A^A]
//        "vv>vA", // [v<AA>A<A^>A, v<AA>A<A>^A, <vAA>A<A^>A, <vAA>A<A>^A]
//        "v>vvA", // [v<A>A<AA^>A, v<A>A<AA>^A, <vA>A<AA^>A, <vA>A<AA>^A]
        ">vvvA", // [vA<AAA^>A, vA<AAA>^A]
    ),
    ('8' to '0') to listOf(
        "vvvA", // [v<AAA^>A, v<AAA>^A, <vAAA^>A, <vAAA>^A]
    ),
    ('8' to '1') to listOf(
        "vv<A", // [v<AA<A>^>A, v<AA<A>>^A, <vAA<A>^>A, <vAA<A>>^A]
        "v<vA", // [v<A<A>A^>A, v<A<A>A>^A, <vA<A>A^>A, <vA<A>A>^A]
        "<vvA", // [v<<A>AA^>A, v<<A>AA>^A, <v<A>AA^>A, <v<A>AA>^A]
    ),
    ('8' to '2') to listOf(
        "vvA", // [v<AA^>A, v<AA>^A, <vAA^>A, <vAA>^A]
    ),
    ('8' to '3') to listOf(
        "vv>A", // [v<AA>A^A, <vAA>A^A]
//        "v>vA", // [v<A>A<A^>A, v<A>A<A>^A, <vA>A<A^>A, <vA>A<A>^A]
        ">vvA", // [vA<AA^>A, vA<AA>^A]
    ),
    ('8' to '4') to listOf(
        "v<A", // [v<A<A>^>A, v<A<A>>^A, <vA<A>^>A, <vA<A>>^A]
        "<vA", // [v<<A>A^>A, v<<A>A>^A, <v<A>A^>A, <v<A>A>^A]
    ),
    ('8' to '5') to listOf(
        "vA", // [v<A^>A, v<A>^A, <vA^>A, <vA>^A]
    ),
    ('8' to '6') to listOf(
        "v>A", // [v<A>A^A, <vA>A^A]
        ">vA", // [vA<A^>A, vA<A>^A]
    ),
    ('8' to '7') to listOf(
        "<A", // [v<<A>^>A, v<<A>>^A, <v<A>^>A, <v<A>>^A]
    ),
    ('8' to '8') to listOf(
        "A", // [A]
    ),
    ('8' to '9') to listOf(
        ">A", // [vA^A]
    ),
    ('9' to 'A') to listOf(
        "vvvA", // [v<AAA^>A, v<AAA>^A, <vAAA^>A, <vAAA>^A]
    ),
    ('9' to '0') to listOf(
        "vvv<A", // [v<AAA<A>^>A, v<AAA<A>>^A, <vAAA<A>^>A, <vAAA<A>>^A]
        "vv<vA", // [v<AA<A>A^>A, v<AA<A>A>^A, <vAA<A>A^>A, <vAA<A>A>^A]
        "v<vvA", // [v<A<A>AA^>A, v<A<A>AA>^A, <vA<A>AA^>A, <vA<A>AA>^A]
        "<vvvA", // [v<<A>AAA^>A, v<<A>AAA>^A, <v<A>AAA^>A, <v<A>AAA>^A]
    ),
    ('9' to '1') to listOf(
        "vv<<A", // [v<AA<AA>^>A, v<AA<AA>>^A, <vAA<AA>^>A, <vAA<AA>>^A]
//        "v<v<A", // [v<A<A>A<A>^>A, v<A<A>A<A>>^A, <vA<A>A<A>^>A, <vA<A>A<A>>^A]
        "v<<vA", // [v<A<AA>A^>A, v<A<AA>A>^A, <vA<AA>A^>A, <vA<AA>A>^A]
//        "<vv<A", // [v<<A>AA<A>^>A, v<<A>AA<A>>^A, <v<A>AA<A>^>A, <v<A>AA<A>>^A]
//        "<v<vA", // [v<<A>A<A>A^>A, v<<A>A<A>A>^A, <v<A>A<A>A^>A, <v<A>A<A>A>^A]
        "<<vvA", // [v<<AA>AA^>A, v<<AA>AA>^A, <v<AA>AA^>A, <v<AA>AA>^A]
    ),
    ('9' to '2') to listOf(
        "vv<A", // [v<AA<A>^>A, v<AA<A>>^A, <vAA<A>^>A, <vAA<A>>^A]
        "v<vA", // [v<A<A>A^>A, v<A<A>A>^A, <vA<A>A^>A, <vA<A>A>^A]
        "<vvA", // [v<<A>AA^>A, v<<A>AA>^A, <v<A>AA^>A, <v<A>AA>^A]
    ),
    ('9' to '3') to listOf(
        "vvA", // [v<AA^>A, v<AA>^A, <vAA^>A, <vAA>^A]
    ),
    ('9' to '4') to listOf(
        "v<<A", // [v<A<AA>^>A, v<A<AA>>^A, <vA<AA>^>A, <vA<AA>>^A]
//        "<v<A", // [v<<A>A<A>^>A, v<<A>A<A>>^A, <v<A>A<A>^>A, <v<A>A<A>>^A]
        "<<vA", // [v<<AA>A^>A, v<<AA>A>^A, <v<AA>A^>A, <v<AA>A>^A]
    ),
    ('9' to '5') to listOf(
        "v<A", // [v<A<A>^>A, v<A<A>>^A, <vA<A>^>A, <vA<A>>^A]
        "<vA", // [v<<A>A^>A, v<<A>A>^A, <v<A>A^>A, <v<A>A>^A]
    ),
    ('9' to '6') to listOf(
        "vA", // [v<A^>A, v<A>^A, <vA^>A, <vA>^A]
    ),
    ('9' to '7') to listOf(
        "<<A", // [v<<AA>^>A, v<<AA>>^A, <v<AA>^>A, <v<AA>>^A]
    ),
    ('9' to '8') to listOf(
        "<A", // [v<<A>^>A, v<<A>>^A, <v<A>^>A, <v<A>>^A]
    ),
    ('9' to '9') to listOf(
        "A", // [A]
    ),
)

val directional = """
    .^A
    <v>
""".trimIndent().lines().mapIndexed { row, line ->
    line.mapIndexedNotNull { col, c ->
        if (c == '.') null else Pos(row, col) to c
    }
}.flatten().let { KeyPad(it, directionalSteps) }.also { println(it) }

val numeric = """
    789
    456
    123
    .0A
""".trimIndent().lines().mapIndexed { row, line ->
    line.mapIndexedNotNull { col, c ->
        if (c == '.') null else Pos(row, col) to c
    }
}.flatten().let { KeyPad(it, numericSteps) }.also { println(it) }

