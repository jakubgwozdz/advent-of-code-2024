package day2

import readAllText

fun main() {
    val data = parse(readAllText("local/day2_input.txt"))
    println(part1(data))
    println(part2(data))
}

fun part1(input: List<List<Long>>) = input.count(Iterable<Long>::isSafe)

fun part2(input: List<List<Long>>) = input.count { line ->
    line.indices.asSequence()
        .map(line::skippingOne)
        .any(Iterable<Long>::isSafe)
}

private fun List<Long>.skippingOne(indexToSkip: Int): Iterable<Long> = Iterable<Long> {
    object : Iterator<Long> {
        var i = if (indexToSkip == 0) 1 else 0
        override fun next(): Long = this@skippingOne[i++].also { if (i == indexToSkip) i++ }
        override fun hasNext(): Boolean = i < this@skippingOne.size
    }
}

fun Iterable<Long>.isSafe(): Boolean = zipWithNext().all { (a, b) -> (b - a) in (1..3) } ||
        zipWithNext().all { (a, b) -> (a - b) in (1..3) }

fun parse(input: String): List<List<Long>> = input.lineSequence().filterNot(String::isBlank)
    .map { it.split(" ").map { it.toLong() } }.toList()
