package day11

import go
import measure
import readAllText

val cache = mutableMapOf<Long, Node>()

sealed interface Node {
    fun blink(): Node
    fun count(): Long
}

var maxDigits = 0

data class Stone(val value: Long) : Node {
    override fun blink(): Node =
        cache.getOrPut(value) {
            val ts = value.toString()
            val next = when {
                value == 0L -> Stone(1)
                ts.length % 2 == 0 -> Split(
                    listOf(
                        Stone(ts.take(ts.length / 2).toLong()) to 1,
                        Stone(ts.drop(ts.length / 2).toLong()) to 1,
                    )
                )

                else -> Stone(value * 2024)
            }
            next
//                .also { println("$value -> $it") }
        }

    override fun count() = 1L
}

data class Split(val nodes: List<Pair<Node, Long>>) : Node {

    override fun blink() = blink1()

    fun blink0(): Node = Split(nodes.map { it.first.blink() to it.second })
        .also {
            val v1 = it.count()
            val v2 = this.blink1().count()
            check(v2 == v1) {
                """
                    $v1 vs $v2
                    normal $it
                    fasten ${this.blink1()}
                """.trimIndent()
            }
        }

    fun blink1(): Node = nodes.toList()
        .flatMap { (s, c) ->
            s.blink().let {
                when (it) {
                    is Stone -> listOf(it to c)
                    is Split -> it.nodes.map { (s, c1) -> s to c * c1 }
                }
            }
        }
        .groupingBy { it.first }
        .fold(0L) { a, (_, e) -> a + e }
        .let { Split(it.toList()) }

    override fun count() = nodes.sumOf { it.first.count() * it.second }
}

typealias Input = List<Node>

fun part1(input: Input) = blinks(input, 25)

private fun blinks(input: Input, number: Int) = input.sumOf {
    cache.clear()
    (1..number).fold(it) { acc, step ->
        acc.blink()
//            .also { println("$step -> ${it.count()}") }
    }.count()
}

fun part2(input: Input) = blinks(input, 75)


fun parse(text: String) = text.trim().split(" ").map { Stone(it.toLong()) }

fun main() {
//    go() { blinks(parse("125"), 75) }
    go(55312L) { part1(parse("125 17")) }
    val text = readAllText("local/day11_input.txt")
    val input = parse(text)
    go() { part1(input) }
    go() { part2(input) }
    return
    measure(text, parse = ::parse, part1 = ::part1, part2 = ::part2)
}

