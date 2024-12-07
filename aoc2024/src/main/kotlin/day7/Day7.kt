package day7

import go
import measure
import readAllText

fun main() {
    val text = readAllText("local/day7_input.txt")
    val input = parse(text)
    go(5702958180383) { part1(input) }
    go(92612386119138) { part2(input) }
    measure(text, parse = ::parse, part1 = ::part1, part2 = ::part2)
}

val part1ops = { operand: Long, result: Long ->
    buildList {
        if (result > operand) add(result - operand)
        if (result % operand == 0L) add(result / operand)
    }
}

typealias Input = List<Line>

fun part1(input: Input) = input.sumOf { if (testAllCases(it, part1ops)) it.result else 0 }

val part2ops = { operand: Long, result: Long ->
    buildList {
        if (result > operand) add(result - operand)
        if (result % operand == 0L) add(result / operand)
        val r = result.toString()
        val o = operand.toString()
        if (r.endsWith(o) && r.length > o.length) add(r.dropLast(o.length).toLong())
    }
}

fun part2(input: Input) = input.sumOf { if (testAllCases(it, part2ops)) it.result else 0 }

fun testAllCases(line: Line, ops: (Long, Long) -> List<Long>): Boolean {
    val todo = mutableListOf(line.params.lastIndex to line.result)
    while (todo.isNotEmpty()) {
        val (pos, result) = todo.removeLast()
        if (pos == 0 && result == line.params[pos]) return true
        if (pos > 0) ops(line.params[pos], result).forEach { todo.add(pos - 1 to it) }
    }
    return false
}

data class Line(val result: Long, val params: List<Long>)

fun parse(input: String) = input.lineSequence().filterNot(String::isBlank)
    .map { line ->
        val (r, p) = line.split(":")
        val result = r.toLong()
        val params = p.split(" ").filter { it.isNotBlank() }.map { it.toLong() }
        Line(result, params)
    }
    .toList()
