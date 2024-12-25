package day25

import go
import linesWithoutLastBlanks
import measure
import readAllText

typealias Input = Pair<List<List<Int>>, List<List<Int>>>

fun part1(input: Input): Int = input.let { (keys, locks) ->
    locks.sumOf { lock ->
        keys.count { key -> key.zip(lock).all { (k, l) -> k >= l } }
    }
}

fun parse(text: String): Input = text.lines().chunked(8).let { chunks ->
    val keys = mutableListOf<List<Int>>()
    val locks = mutableListOf<List<Int>>()
    chunks.forEach { chunk ->
        val isKey = chunk[0] == "....."
        val isLock = chunk[0] == "#####"
        check(isKey xor isLock) { "Invalid chunk: $chunk" }
        val counts = (0..4).map { col ->
            chunk.filterNot { it.isBlank() }
                .count { row -> row[col] == if (isLock) '#' else '.' }
        }
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

