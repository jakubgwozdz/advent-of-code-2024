package day11

import go
import linesWithoutLastBlanks
import measure
import readAllText

typealias Input = List<String>

fun part1(input: String) = input.lineSequence().filterNot(String::isBlank)
fun part1(input: Input) = input
    .count()

fun part2(input: String) = input.lineSequence().filterNot(String::isBlank)
fun part2(input: Input) = input
    .count()

fun parse(text: String) = text.linesWithoutLastBlanks()

fun main() {
    val text = readAllText("local/day11_input.txt")
    val input = parse(text)
    go() { part1(input) }
    go() { part2(input) }
    TODO()
    measure(text, parse = ::parse, part1 = ::part1, part2 = ::part2)
}

