package day11

import go
import measure
import readAllText

private fun Long.blink(times: Long) = when (this) {
    0L -> listOf(1L to times)
    in 10L..99 -> listOf(this / 10 to times, this % 10 to times)
    in 1000L..9999 -> listOf(this / 100 to times, this % 100 to times)
    in 100000L..999999 -> listOf(this / 1000 to times, this % 1000 to times)
    in 10000000L..99999999 -> listOf(this / 10000 to times, this % 10000 to times)
    in 1000000000L..9999999999 -> listOf(this / 100000 to times, this % 100000 to times)
    in 100000000000L..999999999999 -> listOf(this / 1000000 to times, this % 1000000 to times)
    else -> listOf(this * 2024 to times)
}

fun Input.blink() = flatMap { (s, times) -> s.blink(times) }
    .groupingBy { it.first }.fold(0L) { acc, (_, times) -> acc + times }

typealias Input = Map<Long, Long>

private fun blinks(input: Input, number: Int): Long {
    var x = input
    repeat(number) { x = x.blink() }
    return x.values.sum()
}

fun part1(input: Input) = blinks(input, 25)

fun part2(input: Input) = blinks(input, 75)

fun parse(text: String): Input = text.trim().split(" ").associate { it.toLong() to 1L}

fun main() {
    val text = readAllText("local/day11_input.txt")
    val input = parse(text)
    go(239714) { part1(input) }
    go(284973560658514) { part2(input) }
    measure(text, parse = ::parse, part1 = ::part1, part2 = ::part2)
}

