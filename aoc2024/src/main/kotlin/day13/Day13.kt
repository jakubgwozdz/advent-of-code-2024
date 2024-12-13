package day13

import go
import linesWithoutLastBlanks
import measure
import readAllText

typealias Pos = Pair<Long, Long> // x, y

operator fun Pos.plus(other: Pos) = Pos(first + other.first, second + other.second)
operator fun Pos.times(other: Int) = Pos(first * other, second * other)
fun Pos.isLessThanOrEqual(other: Pos) = first <= other.first && second <= other.second

data class Case(val a: Pos, val b: Pos, val prize: Pos)

typealias Input = List<Case>

fun price(tA: Int, tB: Int) = tA * 3 + tB

fun part1(input: Input) = input.sumOf(Case::solve)

fun Case.solve(): Int {
    var rA = 101
    var rB = 101
    var tA = 0
    while (price(tA, 0) < price(rA, rB) && (a * tA).isLessThanOrEqual(prize)) {
        var tB = 0
        while (price(tA, tB) < price(rA, rB) && (a * tA + b * tB).isLessThanOrEqual(prize)) {
            if (a * tA + b * tB == prize && price(tA, tB) < price(rA, rB)) {
                rA = tA
                rB = tB
            }
            tB++
        }
        tA++
    }
    return if (rA == 101 || rB == 101) 0 else price(rA, rB)
}

fun part2(input: Input) = input
    .map { it.copy(prize = it.prize.first + 10000000000000 to it.prize.second + 10000000000000) }
    .sumOf(Case::solve)

val rA = Regex("""Button A: X\+(.+), Y\+(.+)""")
val rB = Regex("""Button B: X\+(.+), Y\+(.+)""")
val rP = Regex("""Prize: X=(.+), Y=(.+)""")

fun parse(text: String) = text.linesWithoutLastBlanks().chunked(4).map { (sA, sB, sP) ->
    val a = rA.find(sA)!!.destructured.let { (x, y) -> Pos(x.toLong(), y.toLong()) }
    val b = rB.find(sB)!!.destructured.let { (x, y) -> Pos(x.toLong(), y.toLong()) }
    val prize = rP.find(sP)!!.destructured.let { (x, y) -> Pos(x.toLong(), y.toLong()) }
    Case(a, b, prize)
}

val test = """
    Button A: X+94, Y+34
    Button B: X+22, Y+67
    Prize: X=8400, Y=5400

    Button A: X+26, Y+66
    Button B: X+67, Y+21
    Prize: X=12748, Y=12176

    Button A: X+17, Y+86
    Button B: X+84, Y+37
    Prize: X=7870, Y=6450

    Button A: X+69, Y+23
    Button B: X+27, Y+71
    Prize: X=18641, Y=10279
""".trimIndent()

fun main() {
    val text = readAllText("local/day13_input.txt")
    val input = parse(text)
    go(480) { part1(parse(test)) }
    go() { part1(input) }
    go() { part2(input) }
    TODO()
    measure(text, parse = ::parse, part1 = ::part1, part2 = ::part2)
}

