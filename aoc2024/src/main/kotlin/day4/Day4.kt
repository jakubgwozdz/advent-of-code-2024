package day4

import readAllText

fun main() {
    println(part1(readAllText("local/day4_input.txt")))
    println(part2(readAllText("local/day4_input.txt")))
}

typealias Pos = Pair<Int, Int>
private operator fun Pos.plus(d: Pair<Int, Int>) = Pos(first + d.first, second + d.second)
private operator fun Pos.minus(d: Pair<Int, Int>) = Pos(first - d.first, second - d.second)

fun part1(input: String) = input.lines().filterNot(String::isBlank).let { map ->
    val g = map.flatMapIndexed { r: Int, line: String -> line.mapIndexed { c, ch -> Pos(r, c) to ch } }
        .groupBy { (_, ch) -> ch }.mapValues { (k, v) -> v.map { (p, _) -> p }.toSet() }
    val x = g['X'].orEmpty()
    val m = g['M'].orEmpty()
    val a = g['A'].orEmpty()
    val s = g['S'].orEmpty()
    sequence {
        (-1..1).forEach { dr -> (-1..1).forEach { dc -> yield(dr to dc) } }
    }.sumOf { d ->
        x.count { px ->
            val pm = px + d
            val pa = pm + d
            val ps = pa + d
            pm in m && pa in a && ps in s
        }
    }
}

fun part2(input: String) = input.lines().filterNot(String::isBlank).let { map ->
    val g = map.flatMapIndexed { r: Int, line: String -> line.mapIndexed { c, ch -> Pos(r, c) to ch } }
        .groupBy { (_, ch) -> ch }.mapValues { (k, v) -> v.map { (p, _) -> p }.toSet() }
    val m = g['M'].orEmpty()
    val a = g['A'].orEmpty()
    val s = g['S'].orEmpty()
    a.count { pa ->
        listOf(-1 to -1, -1 to 1, 1 to -1, 1 to 1).count { d ->
            (pa + d) in s && (pa - d) in m
        } == 2
    }
}
