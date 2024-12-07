package day8

import go
import linesWithoutLastBlanks
import measure
import readAllText

typealias Input = List<String>

fun part1(input: Input) = input
    .count()

fun part2(input: Input) = input
    .count()

fun parse(text: String) = text.linesWithoutLastBlanks()

fun main() {
    val text = readAllText("local/day8_input.txt")
    val input = parse(text)
    go() { part1(input) }
    go() { part2(input) }
    measure(text, parse = ::parse, part1 = ::part1, part2 = ::part2)
}
