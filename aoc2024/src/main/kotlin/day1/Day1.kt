package day1

import readAllText
import kotlin.math.abs

fun main() {
    val input = readAllText("local/day1_input.txt")
    val (left, right) = parse(input)
    println(part1(left, right))
    println(part2(left, right))
}

fun parse(input: String): Pair<List<Long>, List<Long>> = input.lineSequence().filterNot(String::isBlank)
    .map { it.substringBefore(' ').toLong() to it.substringAfterLast(' ').toLong() }
    .unzip()
    .let { (left, right) -> left.sorted() to right.sorted() }

fun part1(left: List<Long>, right: List<Long>): Long = left.asSequence()
    .zip(right.asSequence()) { l, r -> abs(r - l) }
    .sum()

data class Acc(val index: Int = 0, val sum: Long = 0)

fun part2(left: List<Long>, right: List<Long>): Long = left.fold(Acc()) { acc, l ->
    var (j, s) = acc
    while (j < right.size && right[j] < l) j++
    while (j < right.size && right[j] == l) j++.also { s += l }
    Acc(j, s)
}.sum
