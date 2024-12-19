package day19

import go
import measure
import readAllText

data class Input(val patterns: List<String>, val designs: List<String>)

fun part1(input: Input) = input.designs.count { design -> isPossible(design, input.patterns) }

fun isPossible(design: String, patterns: List<String>): Boolean = patterns.any {
    it == design || design.startsWith(it) && isPossible(design.substring(it.length), patterns)
}

fun part2(input: Input): Long = input.designs.sumOf { design -> possible(design, input.patterns) }

fun possible(
    design: String, patterns: List<String>,
    cache: MutableMap<String, Long> = mutableMapOf()
): Long = cache.getOrPut(design) {
    if (design.isEmpty()) 1 else patterns
        .filter { design.startsWith(it) }
        .sumOf { possible(design.substring(it.length), patterns, cache) }
}


fun parse(text: String): Input {
    val towels = text.lines().first().split(", ")
    val patterns = text.lines().drop(1).filter { it.isNotBlank() }
    return Input(towels, patterns)
}

val test = """
    r, wr, b, g, bwu, rb, gb, br

    brwrr
    bggr
    gbbr
    rrbgbr
    ubwu
    bwurrg
    brgr
    bbrgwb
""".trimIndent()

fun main() {
    go(6) { part1(parse(test)) }
    val text = readAllText("local/day19_input.txt")
    val input = parse(text)
    go(340) { part1(input) }

    go(16) { part2(parse(test)) }
    go(717561822679428) { part2(input) }

    measure(text, parse = ::parse, part1 = ::part1, part2 = ::part2)
}

