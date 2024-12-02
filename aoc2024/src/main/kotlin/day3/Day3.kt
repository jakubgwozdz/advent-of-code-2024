package day3

import readAllText

fun main() {
    val input = readAllText("local/day3_input.txt")
    println(part1(input))
    println(part2(input))
}

fun part1(input: String) = input.lineSequence().filterNot(String::isBlank)
    .count()

fun part2(input: String) = input.lineSequence().filterNot(String::isBlank)
    .count()
