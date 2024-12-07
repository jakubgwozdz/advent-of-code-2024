package day7

import go
import readAllText

val test = """
    190: 10 19
    3267: 81 40 27
    83: 17 5
    156: 15 6
    7290: 6 8 6 15
    161011: 16 10 13
    192: 17 8 14
    21037: 9 7 18 13
    292: 11 6 16 20
""".trimIndent()

fun main() {
    go(3749) { part1(test) }
    go(5702958180383) { part1(readAllText("local/day7_input.txt")) }
    go(11387) { part2(test) }
    go(92612386119138) { part2(readAllText("local/day7_input.txt")) }
}

fun part1(input: String) = input.lineSequence().filterNot(String::isBlank)
    .sumOf {
        calculate(it) { params, result -> testPossible(params, result, 1) }
    }

fun part2(input: String) = input.lineSequence().filterNot(String::isBlank)
    .sumOf {
        calculate(it) { params, result -> testPossible(params, result, 2) }
    }

private fun calculate(
    line: String,
    predicate: (List<Long>, Long) -> Boolean
): Long {
    val (r, p) = line.split(":")
    val result = r.toLong()
    val params = p.split(" ").filter { it.isNotBlank() }.map { it.toLong() }
    return if (predicate(params, result)) result else 0
}

fun testPossible(params: List<Long>, result: Long, part: Int = 1): Boolean {
    var i = 0
    val total = params.indices.fold(1) { acc, _ -> acc * (part + 1) } // params.size^(part+1)
//    val total = params.size
    while (i < total) {
        val str = i.toString(part + 1).padStart(params.size - 1, '0')
        val x = params.reduceIndexed { index, acc, l ->
            when (str[index - 1]) {
                '0' -> acc + l
                '1' -> acc * l
                '2' -> (acc.toString() + l.toString()).toLong()
                else -> TODO("got ${str[index - 1]}. `$str`[$index-1]")
            }
        }
        if (x == result) return true
        i++
    }
    return false
}
