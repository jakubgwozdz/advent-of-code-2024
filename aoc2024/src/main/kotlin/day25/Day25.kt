package day25

import go
import linesWithoutLastBlanks
import measure
import readAllText

typealias Input = Pair<List<Int>, List<Int>>

fun part1(input: Input): Int = input.let { (keys, locks) ->
    var count = 0
    for (key in keys) {
        for (lock in locks) {
            val valid = key <= lock &&
                    key and 7 <= lock and 7 &&
                    key and 63 <= lock and 63 &&
                    key and 511 <= lock and 511 &&
                    key and 4095 <= lock and 4095
            if (valid) count++
        }
    }
    count
}

fun parse(text: String): Input = text.lines().chunked(8).let { chunks ->
    val keys = mutableListOf<Int>()
    val locks = mutableListOf<Int>()
    chunks.forEach { chunk ->
        val isKey = chunk[0] == "....."
        val isLock = chunk[0] == "#####"
        check(isKey xor isLock) { "Invalid chunk: $chunk" }
        val counts = (0..4).map { col ->
            chunk.filterNot { it.isBlank() }
                .count { row -> row[col] == if (isLock) '.' else '#' }
        }.fold(0) { acc, i -> acc * 8 + i }
        if (isLock) locks.add(counts) else keys.add(counts)
    }
    keys to locks
}

val example = """
    #####
    .####
    .####
    .####
    .#.#.
    .#...
    .....

    #####
    ##.##
    .#.##
    ...##
    ...#.
    ...#.
    .....

    .....
    #....
    #....
    #...#
    #.#.#
    #.###
    #####

    .....
    .....
    #.#..
    ###..
    ###.#
    ###.#
    #####

    .....
    .....
    .....
    #....
    #.#..
    #.#.#
    #####
""".trimIndent()

fun main() {
    val text = readAllText("local/day25_input.txt")
    val input = parse(text)
    go(3) { part1(parse(example)) }
    go(3338) { part1(input) }
    measure(text, parse = ::parse, part1 = ::part1, part2 = ::part1)
}

