package day23

import go
import linesWithoutLastBlanks
import measure
import readAllText

fun part1(input: Input): Int {
    val result = mutableSetOf<Set<String>>()
    input.keys
        .filter { it[0] == 't' }
        .forEach { t ->
            input.keys
                .filter { a -> a in input[t]!! || t in input[a]!! }
                .forEach { a ->
                    input.keys
                        .filter { b -> b in input[t]!! || t in input[b]!! }
                        .filter { b -> b in input[a]!! || a in input[b]!! }
                        .forEach { b -> result += setOf(t, a, b) }
                }
        }
    return result.size
}

typealias Input = Map<String, Set<String>>

fun part2solve(graph: Input): String {
    val result = mutableListOf<List<String>>()
    graph.forEach { result += listOf(it.key) }
    while (true) {
        val newResult = mutableListOf<List<String>>()
        result.forEachIndexed { i1, previous ->
            graph.filter { (v, es) -> v !in previous && previous.all { it in es } }
                .forEach { newResult += previous + it.key }
        }
        if (newResult.isEmpty()) return result.first().sorted().joinToString(",")
        result.clear()
        result += newResult.distinct()
    }
}

fun part2(graph: Input): String = graph.toList()
    .map { (v, es) -> part2solve(graph.filterKeys { it == v || it in es }) }
    .maxBy { it.length }

fun parse(text: String): Input = text.linesWithoutLastBlanks()
    .map { it.split("-") }
    .let {
        val v = it.flatMap { (a, b) -> listOf(a, b) }.sorted().distinct()
        val e = it.map { (a, b) -> a to b }.toSet() +
                it.map { (a, b) -> b to a }.toSet()
        v.associateWith { v1 -> v.filter { v2 -> v1 < v2 && (v1 to v2) in e }.toSet() }
    }

fun main() {
    val text = readAllText("local/day23_input.txt")
    val input = parse(text)
    go(1366) { part1(input) }
    go("bs,cf,cn,gb,gk,jf,mp,qk,qo,st,ti,uc,xw") { part2(input) }
    measure(text, parse = ::parse, part1 = ::part1, part2 = ::part2)
}

