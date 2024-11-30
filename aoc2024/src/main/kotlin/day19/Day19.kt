package day19

import readAllText

fun main() {
    println(part1(readAllText("local/day19_input.txt")))
    println(part2(readAllText("local/day19_input.txt")))
}

fun part1(input: String) = input.lineSequence().filterNot(String::isBlank)
    .count()

fun part2(input: String) = input.lineSequence().filterNot(String::isBlank)
    .count()
