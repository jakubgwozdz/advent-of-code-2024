package day2

import readAllText

fun main() {
    val data = parse(readAllText("local/day2_input.txt"))
    println(part1(data))
    println(part2(data))
}

fun part1(input: Sequence<List<Long>>) = input.count { it.asSequence().isSafe() }

fun part2(input: Sequence<List<Long>>) = input.count { line ->
    line.indices.asSequence().any { indexToSkip -> line.asSequenceSkipping(indexToSkip).isSafe() }
}

fun <T> List<T>.asSequenceSkipping(indexToSkip: Int) = sequence {
    forEachIndexed { index, data -> if (index != indexToSkip) yield(data) }
}

fun Sequence<Long>.isSafe() = zipWithNext { a, b -> b - a }
    .run { all { it in (1..3) } || all { -it in (1..3) } }

fun parse(input: String) = input.lineSequence().filterNot(String::isEmpty)
    .map { it.split(" ").map { it.toLong() } }
