package day18

import go
import linesWithoutLastBlanks
import measure
import readAllText

typealias Input = List<Pos>

data class Pos(val row: Int, val col: Int) {
    override fun toString()= "$row,$col"
}

fun Pos.neighborsPart1() = sequenceOf(
    Pos(row - 1, col),
    Pos(row, col + 1),
    Pos(row + 1, col),
    Pos(row, col - 1),
)

fun part1(input: Input): Int = input.take(1024).toSet().let { fallen ->

    val start = Pos(0, 0)
    val end = Pos(70, 70)

    val queue = mutableListOf(start to 0)
    val visited = mutableSetOf(start)
    while (queue.isNotEmpty()) {
        val (last, count) = queue.removeFirst()
        if (last == end) return count
        last.neighborsPart1().forEach { next ->
            if (next !in visited &&
                next !in fallen &&
                next.row in 0..end.row && next.col in 0..end.col
            ) {
                visited += next
                queue.add(next to count + 1)
            }
        }
    }
    error("No solution found")
}

fun Pos.neighborsPart2() = sequenceOf(
    Pos(row - 1, col),
    Pos(row - 1, col + 1),
    Pos(row, col + 1),
    Pos(row + 1, col + 1),
    Pos(row + 1, col),
    Pos(row + 1, col - 1),
    Pos(row, col - 1),
    Pos(row - 1, col - 1),
)

fun part2(input: Input): String {
    val dropped = mutableSetOf<Pos>()

    val n = 73 // grid size + borders
    val uf = UnionFind<Pos>(n * n) { (r, c) -> (r + 1) * n + c + 1 }

    fun drop(pos: Pos) {
        dropped += pos
        pos.neighborsPart2().forEach { if (it in dropped) uf.union(pos, it) }
    }

    val leftBorder = Pos(-1, 1)
    (0..70).forEach { i ->
        drop(Pos(-1, i + 1))
        drop(Pos(i - 1, 71))
    }

    val rightBorder = Pos(1, -1)
    (0..70).forEach { i ->
        drop(Pos(i + 1, -1))
        drop(Pos(71, i - 1))
    }

    fun isBlocked() = uf.find(leftBorder) == uf.find(rightBorder)

    input.forEach {
        drop(it)
        if (isBlocked()) return it.toString()
    }

    error("No solution found")
}

class UnionFind<T>(size: Int, val indexOp: (T) -> Int) {
    private val parent = IntArray(size) { it }
    private val rank = IntArray(size) { 0 }

    fun findByIndex(i: Int): Int {
        if (parent[i] != i) parent[i] = findByIndex(parent[i])
        return parent[i]
    }

    fun find(e: T): Int = findByIndex(indexOp(e))

    fun unionByIndex(i1: Int, i2: Int) {
        val rootX = findByIndex(i1)
        val rootY = findByIndex(i2)
        if (rootX != rootY) when {
            rank[rootX] > rank[rootY] -> parent[rootY] = rootX
            rank[rootX] < rank[rootY] -> parent[rootX] = rootY
            else -> parent[rootY] = rootX.also { rank[rootX]++ }
        }
    }

    fun union(e1: T, e2: T) = unionByIndex(indexOp(e1), indexOp(e2))
}


fun parse(text: String) = text.linesWithoutLastBlanks().map { line ->
    val (a, b) = line.split(",").map { it.toInt() }
    Pos(a, b)
}

fun main() {
    val text = readAllText("local/day18_input.txt")
    val input = parse(text)
    go(226, "part1(input): ") { part1(input) }
    go("60,46", "part2(input): ") { part2(input) }
    measure(text, parse = ::parse, part1 = ::part1, part2 = ::part2)
}
