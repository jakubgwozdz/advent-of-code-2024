package day25

import go
import measure
import readAllText
import java.time.Duration
import java.util.BitSet

typealias Input = List<List<String>>

// O(n^2)
fun part1(input: Input): Int {
    var count = 0
    val legit = 0b1110111011101110111
    val keys = mutableListOf<Int>()
    val locks = mutableListOf<Int>()
    input.forEach { chunk ->
        val isLock = chunk[0] == "#####"
        if (isLock) {
            val keyCode = keyCode(chunk, '.', 16)
            count += keys.count { (keyCode - it) or legit == legit }
            locks.add(keyCode)
        } else {
            val keyCode = keyCode(chunk, '#', 16)
//            for (lock in locks) if ((lock - keyCode) or legit == legit) count++
            count += locks.count { (it - keyCode) or legit == legit }
            keys.add(keyCode)
        }
    }
    return count
}

// part1 but O(n*6^5)
fun part2(input: Input): Int {
    val locks = BitSet(6 * 6 * 6 * 6 * 6)
    input.forEach { chunk ->
        if (chunk[0] == "#####") locks.set(keyCode(chunk, '.', 6))
    }
    var count = 0
    input.forEach { chunk ->
        if (chunk[0] != "#####") {
            val (k0, k1, k2, k3, k4) = heights(chunk, '#')
            for (l0 in k0..6)
                for (l1 in k1..6)
                    for (l2 in k2..6)
                        for (l3 in k3..6)
                            for (l4 in k4..6)
                                if (locks.get(((((l0 - 1) * 6 + l1 - 1) * 6 + l2 - 1) * 6 + l3 - 1) * 6 + l4 - 1))
                                    count++
        }
    }
    return count
}

private fun heights(chunk: List<String>, c: Char) = (0..4).map { col -> chunk.count { row -> row[col] == c } }
private fun keyCode(heights: List<Int>, base: Int) = heights.fold(0) { acc, i -> acc * base + i - 1 }
private fun keyCode(chunk: List<String>, c: Char, base: Int) = keyCode(heights(chunk, c), base)

fun parse(text: String): Input = text.lines().chunked(8)
    .map { it.subList(0, 7) }

fun main() {
    val text = readAllText("local/day25_input.txt")
    val input = parse(text)
    go(3338) { part1(input) }
    go(3338) { part2(input) }
    measure(text, duration = Duration.ofSeconds(15), parse = ::parse, part1 = ::part1, part2 = ::part2)
}
