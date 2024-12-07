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

fun part1(inputs: Sequence<Input>) = inputs.sumOf { if (testAllCases(it, part1ops)) it.result else 0 }

val part2ops = listOf<(Long, Long) -> Long>(
    Long::plus,
    Long::times,
    Long::concat,
)

fun part2(inputs: Sequence<Input>) = inputs.sumOf { if (testAllCases(it, part2ops)) it.result else 0 }

fun testAllCases(input: Input, ops: List<(Long, Long) -> Long>): Boolean {
    val total = input.params.indices.fold(1) { acc, _ -> acc * ops.size } // params.size^(ops.size)
    repeat(total) {
        val x = testCase(it, input, ops)
        if (x) return true
    }
    return false
}

private fun testCase(case: Int, input: Input, ops: List<(Long, Long) -> Long>): Boolean {
    var str = case
    val x = input.params.reduce { acc, l ->
        ops[str % ops.size](acc, l).also { str /= ops.size }
            .also { if (it > input.result) return false } // todo: make it cutting of the whole branch
    }
    return x == input.result
}

private fun Long.concat(other: Long): Long {
    var t = this
    var l = other
    while (l > 0) {
        t *= 10
        l /= 10
    }
    return t + other
}

data class Input(val result: Long, val params: List<Long>)

fun parse(input: String) = input.lineSequence().filterNot(String::isBlank)
    .map { line ->
        val (r, p) = line.split(":")
        val result = r.toLong()
        val params = p.split(" ").filter { it.isNotBlank() }.map { it.toLong() }
        Input(result, params)
    }
