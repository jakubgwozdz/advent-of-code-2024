package day11

import go
import measure
import readAllText

fun Long.blink(): List<Long> {
    val ts = toString()
    return when {
        this == 0L -> listOf(1L)
        ts.length % 2 == 0 -> listOf(ts.take(ts.length / 2).toLong(), ts.drop(ts.length / 2).toLong())
        else -> listOf(this * 2024)
    }
}

fun List<Pair<Long, Long>>.blink() = flatMap { (s, c) ->
    s.blink().map { s1 -> s1 to c }
}
    .groupingBy { it.first }
    .fold(0L) { a, (_, e) -> a + e }
    .toList()

typealias Input = List<Long>

fun part1(input: Input) = blinks(input, 25)

private fun blinks(input: Input, number: Int) = (1..number)
    .fold(input.map { it to 1L }) { acc, step -> acc.blink() }
    .sumOf { it.second }

fun part2(input: Input) = blinks(input, 75)

fun parse(text: String) = text.trim().split(" ").map { it.toLong() }

fun main() {
    val text = readAllText("local/day11_input.txt")
    val input = parse(text)
    go(239714) { part1(input) }
    go(284973560658514) { part2(input) }
    measure(text, parse = ::parse, part1 = ::part1, part2 = ::part2)
}

