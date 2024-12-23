package day23

import go
import linesWithoutLastBlanks
import measure
import readAllText

typealias Input = Map<String, Set<String>>

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

fun part2solve(graph: Input): String {
    val result = mutableListOf<List<String>>()
    graph.forEach { result += listOf(it.key) }
    while (true) {
        val newResult = mutableListOf<List<String>>()
        result.forEach { previous ->
            val last = previous.last()
            graph.forEach { (v, es) -> if (v < last && previous.all { it in es }) newResult += previous + v }
        }
        if (newResult.isEmpty()) return result.first().asReversed().joinToString(",")
        result.clear()
        result += newResult.distinct()
    }
}

fun part2jakub(graph: Input): String = graph.toList()
    .map { (v, es) -> part2solve(graph.filterKeys { it == v || it in es }) }
    .maxBy { it.length }

class Box<T>(var value: T)

fun <V> bronKerbosch(r: List<V>, p: List<V>, x: List<V>, graph: Map<V, Set<V>>, result: Box<List<V>>) {
    val pivot = p.firstOrNull() ?: x.firstOrNull()
    if (pivot != null) {
        val p1 = p.toMutableList()
        val x1 = x.toMutableList()
        (p - graph[pivot]!!).forEach { v ->
            val es = graph[v]!!
            bronKerbosch(r + v, p1.filter { it in es }, x1.filter { it in es }, graph, result)
            p1 -= v
            x1 += v
        }
    }
    if (result.value.size < r.size) result.value = r
}

fun part2(graph: Map<String, Set<String>>): Any {
//    return part2jakub(graph)
    return part2bronKerbosch(graph)
}

private fun part2bronKerbosch(graph: Map<String, Set<String>>): String {
    val result = Box(emptyList<String>())
    bronKerbosch(emptyList(), graph.keys.toList(), emptyList(), graph, result)
    return result.value.joinToString(",")
}

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

