package day11

import go
import measure
import readAllText

sealed interface Node {
    fun blink(): Node
    fun count(): Long
}

data class Stone(val value: Long) : Node {
    override fun blink(): Node {
        return Split(value.blink().map { Stone(it) to 1L })
    }

    override fun count() = 1L
}

fun Long.blink(): List<Long> {
    val ts = toString()
    val next = when {
        this == 0L -> listOf(1L)
        ts.length % 2 == 0 ->
            listOf(
                ts.take(ts.length / 2).toLong(),
                ts.drop(ts.length / 2).toLong(),
            )

        else -> listOf(this * 2024)
    }
    return next
}

fun List<Pair<Node, Long>>.blink() = flatMap { (s, c) ->
    s.blink().let {
        when (it) {
            is Stone -> listOf(it to c)
            is Split -> it.nodes.map { (s, c1) -> s to c * c1 }
        }
    }
}
    .groupingBy { it.first }
    .fold(0L) { a, (_, e) -> a + e }
    .toList()

data class Split(val nodes: List<Pair<Node, Long>>) : Node {

    override fun blink() = nodes
        .blink()
        .let { Split(it) }

    override fun count() = nodes.sumOf { it.first.count() * it.second }
}

typealias Input = List<Node>

fun part1(input: Input) = blinks(input, 25)

private fun blinks(input: Input, number: Int) = input.sumOf {
    (1..number).fold(it) { acc, step -> acc.blink() }.count()
}

fun part2(input: Input) = blinks(input, 75)


fun parse(text: String) = text.trim().split(" ").map { Stone(
    it.toLong()
) }

fun main() {
    val text = readAllText("local/day11_input.txt")
    val input = parse(text)
    go(239714) { part1(input) }
    go(284973560658514) { part2(input) }
    measure(text, parse = ::parse, part1 = ::part1, part2 = ::part2)
}

