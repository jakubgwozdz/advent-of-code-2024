package day1

import readAllText
import kotlin.math.absoluteValue

fun main() {
    println(part1(readAllText("local/day1_input.txt")))
    println(part2(readAllText("local/day1_input.txt")))
}

fun part1(input: String) = parse(input)
    .let { (left, right) ->
        left.sorted().zip(right.sorted()) { left, right -> (right - left).absoluteValue }.sum()
    }

fun part2(input: String) = parse(input)
    .let { (left, right) ->
        val rightCounters = right.groupingBy { it }.eachCount()
        left.sumOf { l -> l * (rightCounters[l] ?: 0) }
    }

private fun parse(input: String): Pair<List<Long>, List<Long>> = input.lineSequence().filterNot(String::isBlank)
    .map { it.substringBefore(' ').toLong() to it.substringAfterLast(' ').toLong() }
    .unzip()
