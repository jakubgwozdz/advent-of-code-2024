package day2

import readAllText
import kotlin.math.abs
import kotlin.math.sign
import kotlin.ranges.contains

fun main() {
    val data = parse(readAllText("local/day2_input.txt"))
    println(part1(data))
    println(part2(data))
}

fun part1(input: Sequence<List<Long>>) = input.count { it.asSequence().isSafe() }

fun part2(input: Sequence<List<Long>>) = input.count { it.maxOneError() }

fun List<Long>.maxOneError() = asSequence().maxOneErrorOneWay() || asReversed().asSequence().maxOneErrorOneWay()

data class Acc(val prev: Long, val sign: Int = 0, val errors: Int = 0)

fun Sequence<Long>.maxOneErrorOneWay() = drop(1).runningFold(Acc(first())) { acc, curr ->
    val delta = curr - acc.prev
    if (delta == 0L || abs(delta) > 3 || delta.sign == -acc.sign) acc.copy(errors = acc.errors + 1)
    else acc.copy(prev = curr, sign = delta.sign)
}.none { it.errors >= 2 }

fun List<Long>.maxOneErrorQuadratic(): Boolean =
    indices.any { indexToSkip -> asSequenceSkipping(indexToSkip).isSafe() }

fun <T> List<T>.asSequenceSkipping(indexToSkip: Int) = sequence {
    forEachIndexed { index, data -> if (index != indexToSkip) yield(data) }
}

fun Sequence<Long>.isSafe() = zipWithNext { a, b -> b - a }
    .run { all { it in (1..3) } || all { -it in (1..3) } }

fun parse(input: String) = input.lineSequence().filterNot(String::isEmpty)
    .map { it.split(" ").map { it.toLong() } }
