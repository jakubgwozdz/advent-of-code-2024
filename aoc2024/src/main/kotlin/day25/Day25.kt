package day25

import go
import measure
import readAllText

typealias Input = List<List<String>>

fun part1(input: Input): Int {
    val keys = mutableListOf<Int>()
    val locks = mutableListOf<Int>()
    input.forEach { chunk ->
        val isKey = chunk[0] == "....."
        val isLock = chunk[0] == "#####"
        check(isKey xor isLock) { "Invalid chunk: $chunk" }
        val keyCode = (0..4)
            .map { col -> chunk.count { row -> row[col] == if (isLock) '.' else '#' } }
            .fold(0) { acc, i -> acc * 16 + i }
        if (isLock) locks.add(keyCode) else keys.add(keyCode)
    }

    var count = 0
    val legit = 0b1110111011101110111
    for (key in keys)
        for (lock in locks)
            if ((lock - key) or legit == legit) count++
    return count
}

fun parse(text: String): Input = text.lines().chunked(8)
    .map { it.subList(0, 7) }

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

