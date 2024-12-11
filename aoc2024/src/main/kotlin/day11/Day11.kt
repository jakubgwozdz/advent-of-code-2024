package day11

import go
import measure
import readAllText

fun Input.blink() = flatMap { (s, c) ->
    when (s) {
        0L -> listOf(1L to c)
        in (10..99) -> listOf(s / 10 to c, s % 10 to c)
        in (1000..9999) -> listOf(s / 100 to c, s % 100 to c)
        in (100000..999999) -> listOf(s / 1000 to c, s % 1000 to c)
        in (10000000..99999999) -> listOf(s / 10000 to c, s % 10000 to c)
        in (1000000000..9999999999) -> listOf(s / 100000 to c, s % 100000 to c)
        in (100000000000..999999999999) -> listOf(s / 1000000 to c, s % 1000000 to c)
        else -> listOf(s * 2024 to c)
    }
}
    .groupingBy { it.first }
    .fold(0L) { a, (_, e) -> a + e }
    .toList()

typealias Input = List<Pair<Long, Long>>

fun part1(input: Input) = blinks(input, 25)

private fun blinks(input: Input, number: Int): Long {
    var x = input
    repeat(number) { x = x.blink() }
    return x.sumOf { it.second }
}

fun part2(input: Input) = blinks(input, 75)

fun parse(text: String): Input = text.trim().split(" ").map { it.toLong() to 1L }

fun main() {
    val text = readAllText("local/day11_input.txt")
    val input = parse(text)
    go(239714) { part1(input) }
    go(284973560658514) { part2(input) }
    measure(text, parse = ::parse, part1 = ::part1, part2 = ::part2)
}

