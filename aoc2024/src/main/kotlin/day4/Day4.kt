package day4

import readAllText

fun main() {
    println(part1(readAllText("local/day4_input.txt")))
    println(part2(readAllText("local/day4_input.txt")))
}

fun part1(input: String) = input.lineSequence().filterNot(String::isBlank)
    .count()

fun part2(input: String) = input.lineSequence().filterNot(String::isBlank)
    .count()
