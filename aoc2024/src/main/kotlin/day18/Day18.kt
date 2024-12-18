package day18

import go
import linesWithoutLastBlanks
import measure
import readAllText

typealias Input = List<Pos>
typealias Pos = Pair<Int, Int>
enum class Dir { U, R, D, L }

operator fun Pos.plus(d: Dir) = when (d) {
    Dir.U -> first - 1 to second
    Dir.R -> first to second + 1
    Dir.D -> first + 1 to second
    Dir.L -> first to second - 1
}
fun Pos.distance(other: Pos): Int {
    val (r1, c1) = this
    val (r2, c2) = other
    return (c1 - c2) * (c1 - c2) + (r1 - r2) * (r1 - r2)
}

fun part1(input: Input, end: Pos = Pos(70, 70)): Int = input.take(1024).toSet()
    .let { fallen -> solve(fallen, end)!!.size - 1 }

fun solve(fallen: Set<Pos>, end: Pos): List<Pos>? {
    val start = Pos(0, 0)

    val comparator = compareBy<List<Pos>> { path -> path.size }
        .thenBy { path -> path.last().distance(end) }
    val queue = PriorityQueue(comparator, listOf(start))
    val visited = mutableSetOf(start)
    while (queue.isNotEmpty()) {
        val p = queue.poll()
        val last = p.last()
        if (last == end) return p//.also { println("Found path: $it") }
        Dir.entries.forEach { dir ->
            val next = last + dir
            if (next !in fallen &&
                next !in visited &&
                next.first in 0..end.first && next.second in 0..end.second) {
                visited += next
                queue.offer(p + next)
            }
        }
    }
    return null
}

fun part2(input: Input, end: Pos = Pos(70, 70)): String {
    val fallen = mutableSetOf<Pos>()
    var lastPath = solve(fallen, end)!!.toSet()
    input.forEach { block ->
        fallen += block
        if (block in lastPath) {
            lastPath = solve(fallen, end)?.toSet() ?: return "${block.first},${block.second}"
        }
    }
    error("No solution found")
}

fun parse(text: String) = text.linesWithoutLastBlanks().map { line ->
    val (a, b) = line.split(",").map { it.toInt() }
    a to b
}

val test1 = """
    5,4
    4,2
    4,5
    3,0
    2,1
    6,3
    2,4
    1,5
    0,6
    3,3
    2,6
    5,1
    1,2
    5,5
    2,5
    6,5
    1,4
    0,4
    6,4
    1,1
    6,1
    1,0
    0,5
    1,6
    2,0
""".trimIndent()

fun main() {
    go(22) { part1(parse(test1).take(12), Pos(6, 6)) }
    go("6,1") { part2(parse(test1), Pos(6, 6)) }
    val text = readAllText("local/day18_input.txt")
    val input = parse(text)
    go(226, "part1(input): ") { part1(input) }
    go("60,46", "part2(input): ") { part2(input) }
    measure(text, parse = ::parse, part1 = ::part1, part2 = ::part2)
}

class PriorityQueue<E : Any>(val comparator: Comparator<E>, vararg initial: E) {

    private var backingList = mutableListOf<E>().apply { addAll(initial) }

    val size get() = backingList.size
    fun isNotEmpty(): Boolean = size > 0

    fun poll(): E {
        check(size > 0)
        return backingList.removeAt(0)
    }

    fun offer(e: E) {
        val index = backingList.binarySearch(e, comparator).let {
            if (it < 0) -it - 1 else it
        }
        backingList.add(index, e)
    }

    fun toList() = backingList.toList()
    override fun toString(): String = backingList.toString()
}
