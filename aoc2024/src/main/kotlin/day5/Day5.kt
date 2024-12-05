package day5

import readAllText

val test = """
    47|53
    97|13
    97|61
    97|47
    75|29
    61|13
    75|53
    29|13
    97|29
    53|29
    61|53
    97|53
    61|29
    47|13
    75|47
    97|75
    47|61
    75|61
    47|29
    75|13
    53|13

    75,47,61,53,29
    97,61,53,29,13
    75,29,13
    75,97,47,61,53
    61,13,29
    97,13,75,29,47
""".trimIndent()

fun main() {
    val input = readAllText("local/day5_input.txt")
    println(part1(test))
    println(part1(input))
    println(part2(input))
}

fun part1(input: String) = input.lines().let {
    val s = it.indexOfFirst(String::isBlank)
    it.take(s).filterNot(String::isBlank).map { it.split("|") } to
            it.drop(s).filterNot(String::isBlank).map { it.split(",") }
}.let { (ordering, updates) ->
    val befores = mutableMapOf<String, MutableSet<String>>()
    val afters = mutableMapOf<String, MutableSet<String>>()

    ordering.forEach { (x, y) ->
        befores.getOrPut(y) { mutableSetOf() }.run {
            add(x)
            addAll(befores[x].orEmpty())
        }
        afters.getOrPut(x) { mutableSetOf() }.run {
            add(y)
            addAll(afters[y].orEmpty())
        }
    }

    updates.filter {
        it.indices.all { i ->
            val a = afters[it[i]].orEmpty()
            val b = befores[it[i]].orEmpty()
            it.take(i).none { x -> x in a } && it.drop(i + 1).none { y -> y in b }
        }
    }.sumOf { it[it.size / 2].toLong() }
}

fun part2(input: String) = input.lineSequence().filterNot(String::isBlank)
    .count()
