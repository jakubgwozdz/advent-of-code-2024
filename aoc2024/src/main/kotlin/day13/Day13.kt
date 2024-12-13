package day13

import go
import linesWithoutLastBlanks
import measure
import readAllText

typealias Pos = Pair<Long, Long> // x, y

operator fun Pos.plus(other: Pos) = Pos(first + other.first, second + other.second)
operator fun Pos.times(other: Int) = Pos(first * other, second * other)
operator fun Pos.times(other: Long) = Pos(first * other, second * other)
fun Pos.isLessThanOrEqual(other: Pos) = first <= other.first && second <= other.second

data class Case(val a: Pos, val b: Pos, val prize: Pos)

typealias Input = List<Case>

fun price(tA: Long, tB: Long) = tA * 3 + tB

fun part1(input: Input) = input.sumOf { case ->
    case.solve()
//        .also { println("$case $it") }
//        .also {
//        val solve2 = case.solve2()
//        if (solve2 != it) {
//            println("$case solution1 $it solution2 $solve2")
//            case.solve2()
//        }
//    }
}

fun Case.solve(): Long {
    val aStart = 0L
    val bStart = 0L
    var ta = aStart
    var tb = bStart
    while ((a * (ta) + b * (tb)).isLessThanOrEqual(prize)) {
        var tb = bStart
        while ((a * (ta) + b * (tb)).isLessThanOrEqual(prize)) {
            val pos = a * (ta) + b * (tb)
            if (pos == prize) return price(ta, tb)
            tb++
        }
        ta++
    }
    return 0
}

fun Case.solve2(): Long {
    val lcm = lcm(a.first, b.first) * lcm(a.second, b.second)
    val maxOps = lcm
    val startA = minOf(prize.first / a.first, prize.second / a.second) - lcm
    val startB = minOf(prize.first / b.first, prize.second / b.second) - lcm

    for (ta in 0..maxOps) {
        for (tb in 0..maxOps) {
            val x = a * (startA + ta) + b * (startB + tb)
            if (x == prize) return price(startA + ta, startB + tb)
        }
    }

    return 0
}

fun part2(input: Input) = input
    .map { it.copy(prize = it.prize.first + 10000000000000 to it.prize.second + 10000000000000) }
    .sumOf(Case::solve2)

val rA = Regex("""Button A: X\+(.+), Y\+(.+)""")
val rB = Regex("""Button B: X\+(.+), Y\+(.+)""")
val rP = Regex("""Prize: X=(.+), Y=(.+)""")

fun parse(text: String) = text.linesWithoutLastBlanks().chunked(4).map { (sA, sB, sP) ->
    val a = rA.find(sA)!!.destructured.let { (x, y) -> Pos(x.toLong(), y.toLong()) }
    val b = rB.find(sB)!!.destructured.let { (x, y) -> Pos(x.toLong(), y.toLong()) }
    val prize = rP.find(sP)!!.destructured.let { (x, y) -> Pos(x.toLong(), y.toLong()) }
//    if (gcd(a.first, b.first) != 1L && gcd(a.second, b.second) != 1L) println(
//        "$a,$b,$prize gcd(X)=${
//            gcd(a.first, b.first)
//        }, gcd(Y)=${gcd(a.second, b.second)}"
//    )
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
    go(480) { part1(parse(test)) }
    val text = readAllText("local/day13_input.txt")
    val input = parse(text)
    go(30973) { part1(input) }
    go() { part2(input) }
    TODO()
    measure(text, parse = ::parse, part1 = ::part1, part2 = ::part2)
}

tailrec fun gcd(a: Long, b: Long): Long = if (b == 0L) a else gcd(b, a % b)
fun lcm(a: Long, b: Long): Long = a / gcd(a, b) * b
