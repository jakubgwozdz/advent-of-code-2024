package day7

import go
import readAllText

fun main() {
    val input = parse(readAllText("local/day7_input.txt"))
    go(5702958180383) { part1(input) }
    go(92612386119138) { part2(input) }
}

val part1ops = { operand: Long, result: Long ->
    buildList {
        if (result > operand) add(result - operand)
        if (result % operand == 0L) add(result / operand)
    }
}

fun part1(inputs: Sequence<Input>) = inputs.sumOf { if (testAllCases(it, part1ops)) it.result else 0 }

val part2ops = { operand: Long, result: Long ->
    buildList {
        if (result > operand) add(result - operand)
        if (result % operand == 0L) add(result / operand)
        val r = result.toString()
        val o = operand.toString()
        if (r.endsWith(o) && r.length > o.length) add(r.dropLast(o.length).toLong())
    }
}

fun part2(inputs: Sequence<Input>) = inputs.sumOf { if (testAllCases(it, part2ops)) it.result else 0 }

fun testAllCases(input: Input, ops: (Long, Long) -> List<Long>): Boolean {
    val todo = mutableListOf(input.params.lastIndex to input.result)
    while (todo.isNotEmpty()) {
        val (pos, result) = todo.removeLast()
        if (pos == 0 && result == input.params[pos]) return true
        if (pos > 0) ops(input.params[pos], result).forEach { todo.add(pos - 1 to it) }
    }
    return false
}

data class Input(val result: Long, val params: List<Long>)

fun parse(input: String) = input.lineSequence().filterNot(String::isBlank)
    .map { line ->
        val (r, p) = line.split(":")
        val result = r.toLong()
        val params = p.split(" ").filter { it.isNotBlank() }.map { it.toLong() }
        Input(result, params)
    }
