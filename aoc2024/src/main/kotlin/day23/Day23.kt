package day23

import go
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import linesWithoutLastBlanks
import measure
import readAllText

fun part1(input: Input): Int {
    val result = mutableSetOf<Set<String>>()
    val tComps = input.keys.filter { it[0] == 't' }
    tComps.forEach { tComp ->
        input[tComp]!!.forEach { c1 ->
            input[tComp]!!.forEach { c2 -> if (c1 != c2 && c2 in input[c1]!!) result += setOf(tComp, c1, c2) }
        }
    }
    return result.size
}

fun <T, R> List<T>.mapParallel(op: (T) -> R) = runBlocking { map { async(Dispatchers.Default) { op(it) } }.awaitAll() }

typealias Input = Map<String, Set<String>>

fun part2(graph: Input): String {
    val result = mutableSetOf<Set<String>>()
    graph.keys.forEach { result += setOf(it) }
    while (result.size > 1) {
        val newResult = mutableListOf<Set<String>>()
        result.forEach { s1 ->
            graph.filter { (v, es) -> v !in s1 && es.containsAll(s1) }
                .forEach { newResult += s1 + it.key }
        }
        result.clear()
        result += newResult.distinct()
    }
    return result.single().sorted().joinToString(",")
}

fun parse(text: String): Input = text.linesWithoutLastBlanks()
    .map { it.split("-") }
    .let {
        val v = it.flatMap { (a, b) -> listOf(a, b) }.sorted().distinct()
        val e = it.map { (a, b) -> a to b }.toSet() +
                it.map { (a, b) -> b to a }.toSet()
        v.associateWith { v1->v.filter { v2->(v1 to v2) in e }.toSet() }
    }

val example = """
    kh-tc
    qp-kh
    de-cg
    ka-co
    yn-aq
    qp-ub
    cg-tb
    vc-aq
    tb-ka
    wh-tc
    yn-cg
    kh-ub
    ta-co
    de-co
    tc-td
    tb-wq
    wh-td
    ta-ka
    td-qp
    aq-cg
    wq-ub
    ub-vc
    de-ta
    wq-aq
    wq-vc
    wh-yn
    ka-de
    kh-ta
    co-tc
    wh-qp
    tb-vc
    td-yn
    ww-co
    ww-de
    ww-ka
    ww-ta
""".trimIndent()

fun main() {
//    go(7) { part1(parse(example)) }
    val text = readAllText("local/day23_input.txt")
    val input = parse(text)
    go(1366) { part1(input) }
//    go("co,de,ka,ta") { part2(parse(example)) }
    go("co,de,ka,ta,ww") { part2(parse(example)) }
    go("bs,cf,cn,gb,gk,jf,mp,qk,qo,st,ti,uc,xw") { part2(input) }
    measure(text, parse = ::parse, part1 = ::part1, part2 = ::part2)
}

