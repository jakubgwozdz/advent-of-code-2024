package day7

import go
import readAllText

fun main() {
    val input = parse(readAllText("local/day7_input.txt"))
    go(5702958180383) { part1(input) }
    go(92612386119138) { part2(input) }
}

val part1ops = listOf<(Long, Long) -> Long>(
    Long::plus,
    Long::times,
)

fun part1(input: Sequence<Input>) = input.sumOf { calculate(it, part1ops) }

val part2ops = listOf<(Long, Long) -> Long>(
    Long::plus,
    Long::times,
    { acc, l -> (acc.toString() + l.toString()).toLong() },
)

fun part2(input: Sequence<Input>) = input.sumOf { calculate(it, part2ops) }

private fun calculate(
    input: Input,
    ops: List<(Long, Long) -> Long>,
): Long = if (isPossible(input.params, input.result, ops)) input.result else 0


fun isPossible(params: List<Long>, result: Long, ops: List<(Long, Long) -> Long>): Boolean {
    val total = params.indices.fold(1) { acc, _ -> acc * ops.size } // params.size^(ops.size)
    repeat(total) {
        val x = testCase(it, params, result, ops)
        if (x) return true
    }
    return false
}

private fun testCase(case: Int, params: List<Long>, result: Long, ops: List<(Long, Long) -> Long>): Boolean {
    var str = case
    val x = params.reduce { acc, l ->
        ops[str % ops.size](acc, l).also { str /= ops.size }.also { if (it > result) return false }
    }
    return x == result
}

data class Input(val result: Long, val params: List<Long>)

fun parse(input: String) = input.lineSequence().filterNot(String::isBlank)
    .map { line ->
        val (r, p) = line.split(":")
        val result = r.toLong()
        val params = p.split(" ").filter { it.isNotBlank() }.map { it.toLong() }
        Input(result, params)
    }
