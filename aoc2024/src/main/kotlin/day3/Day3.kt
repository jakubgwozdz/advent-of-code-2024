package day3

import readAllText

fun main() {
    val input = readAllText("local/day3_input.txt")
    println(part1(input))
    println(part2(input))
}

val regex1 = Regex("""mul\((\d{1,3}),(\d{1,3})\)""")

fun part1(input: String) = input.lineSequence().flatMap { regex1.findAll(it) }
    .map { it.destructured }
    .sumOf { (a, b) -> a.toLong() * b.toLong() }

data class Acc(val enabled: Boolean = true, val sum: Long = 0)
val regex2 = Regex("""(mul\((\d{1,3}),(\d{1,3})\))|(do\(\))|(don't\(\))""")

fun part2(input: String) = input.lineSequence().flatMap { regex2.findAll(it) }.fold(Acc()) { acc, matchResult ->
    when {
        matchResult.value == "do()" -> acc.copy(enabled = true)
        matchResult.value == "don't()" -> acc.copy(enabled = false)
        !acc.enabled -> acc
        else -> matchResult.destructured.let { (_, a, b) -> acc.copy(sum = acc.sum + a.toLong() * b.toLong()) }
    }
}.sum
