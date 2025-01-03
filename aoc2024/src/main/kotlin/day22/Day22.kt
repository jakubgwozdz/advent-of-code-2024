package day22

import go
import linesWithoutLastBlanks
import measure
import readAllText

typealias Input = List<Long>

private fun Long.nextSecret(): Long = step1().step2().step3()

private fun Long.step1() = shl(6) xor this and 0xFFFFFF
private fun Long.step2() = shr(5) xor this and 0xFFFFFF
private fun Long.step3() = shl(11) xor this and 0xFFFFFF

fun part1(input: Input) = input.sumOf {
    generateSequence(it, Long::nextSecret).drop(2000).first()
}

fun part2(input: Input): Any {
    val array = IntArray(19 * 19 * 19 * 19 * 19)
    val done = BooleanArray(19 * 19 * 19 * 19 * 19)
    input.forEach { seed ->
        done.fill(false)
        generateSequence(seed, Long::nextSecret)
            .map { it % 10 }
            .take(2001)
            .windowed(5)
            .forEach { last5 ->
                val code = last5.zipWithNext { a, b -> b - a }.fold(0L) { acc, l -> acc * 19 + l + 9 }.toInt()
                if (!done[code]) {
                    done[code] = true
                    array[code] += last5.last().toInt()
                }
            }
    }
    return array.max()
}

fun parse(text: String) = text.linesWithoutLastBlanks().map { it.toLong() }

fun main() {
    go(37327623) { part1(listOf(1, 10, 100, 2024)) }
    go(23) { part2(listOf(1, 2, 3, 2024)) }
    val text = readAllText("local/day22_input.txt")
    val input = parse(text)
    go(13022553808) { part1(input) }
    go(1555) { part2(input) }
//    TODO()
    measure(text, parse = ::parse, part1 = ::part1, part2 = ::part2)
}

