package day11

import go
import measure
import readAllText

fun List<Pair<Long, Long>>.blink() = flatMap { (s, c) ->
    val ts = s.toString()
    when {
        s == 0L -> listOf(1L to c)
        ts.length % 2 == 0 -> listOf(ts.take(ts.length / 2).toLong() to c, ts.drop(ts.length / 2).toLong() to c)
        else -> listOf(s * 2024 to c)
    }
}
    .groupingBy { it.first }
    .fold(0L) { a, (_, e) -> a + e }
    .toList()

typealias Input = List<Long>

fun part1(input: Input) = blinks(input, 25)

private fun blinks(input: Input, number: Int): Long {
    var x = input.map { it to 1L }
    repeat(number) { x = x.blink() }
    return x.sumOf { it.second }
}

fun part2(input: Input) = blinks(input, 75)

fun parse(text: String) = text.trim().split(" ").map { it.toLong() }

fun main() {
    val text = readAllText("local/day11_input.txt")
    val input = parse(text)
    go(239714) { part1(input) }
    go(284973560658514) { part2(input) }
    measure(text, parse = ::parse, part1 = ::part1, part2 = ::part2)
}

