package day14

import go
import linesWithoutLastBlanks
import measure
import readAllText
import kotlin.time.Duration

typealias Pos = Pair<Int, Int> // x, y

operator fun Pos.plus(other: Pos) = Pos(first + other.first, second + other.second)
operator fun Pos.rem(other: Pos) = Pos(first.mod(other.first), second.mod(other.second))
operator fun Pos.times(other: Int) = Pos(first * other, second * other)

typealias Input = List<Pair<Pos, Pos>>

fun Pos.quadrant(bounds: Pos) = when {
    first < bounds.first / 2 && second < bounds.second / 2 -> 0
    first > bounds.first / 2 && second < bounds.second / 2 -> 1
    first < bounds.first / 2 && second > bounds.second / 2 -> 2
    first > bounds.first / 2 && second > bounds.second / 2 -> 3
    else -> null
}

fun part1(input: Input, boundary: Pos = 101 to 103) = input
    .step(boundary, 100)
    .groupingBy { (p, v) -> p.quadrant(boundary) }.eachCount()
    .filterKeys { it != null }
    .values.reduce(Int::times)

fun Input.step(boundary: Pos, count: Int = 1) =
    map { (p, v) -> (p + v * count) % boundary to v }

fun part2(input: Input, boundary: Pos = 101 to 103) =
    generateSequence(input) { robots -> robots.step(boundary) }
        .withIndex()
        .takeWhile { (step, robots) -> step == 0 || robots != input }
        .maxBy { (_, robots) ->
            val asSet = robots.map { it.first }.toSet()
            var result = 0
            asSet.forEach {
                if (it + (1 to 0) in asSet) result++
                if (it + (0 to 1) in asSet) result++
                if (it + (-1 to 0) in asSet) result++
                if (it + (0 to -1) in asSet) result++
            }
            result
        }
        .index

//        Also works:
//        .indexOfFirst { robots ->
//            val seen = mutableSetOf<Pos>()
//            robots.forEach { (p, _) -> if (p in seen) return@indexOfFirst false else seen.add(p) }
//            return@indexOfFirst true
//        }

val regex = Regex("""p=(.+),(.+) v=(.+),(.+)""")

fun parse(text: String) = text.linesWithoutLastBlanks().map { line ->
    val (px, py, vx, vy) = regex.find(line)!!.destructured
    Pos(px.toInt(), py.toInt()) to Pos(vx.toInt(), vy.toInt())
}

val test = """
    p=0,4 v=3,-3
    p=6,3 v=-1,-3
    p=10,3 v=-1,2
    p=2,0 v=2,-1
    p=0,0 v=1,3
    p=3,0 v=-2,-2
    p=7,6 v=-1,-3
    p=3,0 v=-1,-2
    p=9,3 v=2,3
    p=7,3 v=-1,2
    p=2,4 v=2,-3
    p=9,5 v=-3,-3
""".trimIndent()

fun main() {
    go(12) { part1(parse(test), 11 to 7) }
    val text = readAllText("local/day14_input.txt")
    val input = parse(text)
    go(217132650) { part1(input) }
    go(6516) { part2(input) }
    measure(text, parse = ::parse, part1 = ::part1, part2 = ::part2)
}

