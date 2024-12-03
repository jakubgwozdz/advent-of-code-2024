package day3

import readAllText

fun main() {
    val input = readAllText("local/day3_input.txt")
    println(part1(input))
    println(part2(input))
}

fun mul(a: String, b: String) = a.toLong() * b.toLong()

val regex1 = """mul\((?<a>\d{1,3}),(?<b>\d{1,3})\)""".toRegex()

fun part1(input: String) = regex1.findAll(input)
    .sumOf { r -> mul(r["a"], r["b"]) }

data class Acc(val enabled: Boolean = true, val sum: Long = 0) {
    fun enabled() = copy(enabled = true)
    fun disabled() = copy(enabled = false)
    fun withMul(a: String, b: String) = if (enabled) Acc(sum = sum + mul(a, b)) else this
}

val regex2 = """(mul\((?<a>\d{1,3}),(?<b>\d{1,3})\))|(do\(\))|(don't\(\))""".toRegex()

fun part2(input: String) = regex2.findAll(input).fold(Acc()) { acc, r ->
    when (r.value) {
        "do()" -> acc.enabled()
        "don't()" -> acc.disabled()
        else -> acc.withMul(r["a"], r["b"])
    }
}.sum

private operator fun MatchResult.get(name: String) = groups[name]?.value ?: ""
