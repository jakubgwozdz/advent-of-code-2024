package day22

import go
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import linesWithoutLastBlanks
import measure
import readAllText
import java.time.Instant

typealias Input = List<Long>

fun Long.prune() = this and 0xFFFFFF
fun Long.mix(secret: Long) = this xor secret
private fun Long.nextSecret(): Long {
    val s1 = shl(6).mix(this).prune()
    val s2 = s1.shr(5).mix(s1).prune()
    val s3 = s2.shl(11).mix(s2).prune()
    return s3
}

fun part1(input: Input) = input.sumOf { it ->
    var secret = it
    repeat(2000) { secret = secret.nextSecret() }
    secret
}

fun part2(input: Input): Long {
    var instant = Instant.now()
    val cached = mutableMapOf<String,Long>()
    return (0..99999L).maxOf { l ->
        val d1 = l % 10
        val d2 = l / 10 % 10
        val d3 = l / 100 % 10
        val d4 = l / 1000 % 10
        val d5 = l / 10000 % 10
        val diffs = listOf(d5 - d4, d4 - d3, d3 - d2, d2 - d1)
        val code = diffs.joinToString()
        if (instant.plusMillis(1000).isBefore(Instant.now())) {
            println("testing $l $diffs")
            instant = Instant.now()
        }
        cached.getOrPut(code) {
            input.mapParallel { seed ->
                generateSequence(seed) { it.nextSecret() }
                    .map { it % 10 }
                    .take(2001)
                    .windowed(5)
                    .map { it[4] to it.zipWithNext { a, b -> b - a } }
                    .firstOrNull { (_, d) -> d == diffs }?.first ?: 0L
            }.sum()
        }
    }
}

fun <T, R> List<T>.mapParallel(op: (T) -> R) = runBlocking { map { async(Dispatchers.Default) { op(it) } }.awaitAll() }

fun parse(text: String) = text.linesWithoutLastBlanks().map { it.toLong() }

fun main() {
    go(37327623) { part1(listOf(1, 10, 100, 2024)) }
    go(23) { part2(listOf(1, 2, 3, 2024)) }
    val text = readAllText("local/day22_input.txt")
    val input = parse(text)
    go() { part1(input) }
    go() { part2(input) }
    TODO()
    measure(text, parse = ::parse, part1 = ::part1, part2 = ::part2)
}

