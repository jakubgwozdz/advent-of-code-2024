package day18

import go
import linesWithoutLastBlanks
import measure
import readAllText

typealias Input = List<Pos>
typealias Pos = Pair<Int, Int>

enum class Dir { U, R, D, L }

operator fun Pos.plus(d: Dir): Pos = when (d) {
    Dir.U -> first - 1 to second
    Dir.R -> first to second + 1
    Dir.D -> first + 1 to second
    Dir.L -> first to second - 1
}

fun part1(input: Input): Int = input.take(1024).toSet().let { fallen ->

    val start = Pos(0, 0)
    val end = Pos(70, 70)

    val queue = mutableListOf(start to 0)
    val visited = mutableSetOf(start)
    while (queue.isNotEmpty()) {
        val (last, count) = queue.removeFirst()
        if (last == end) return count
        Dir.entries.forEach { dir ->
            val next = last + dir
            if (next !in visited &&
                next !in fallen &&
                next.first in 0..end.first && next.second in 0..end.second
            ) {
                visited += next
                queue.add(next to count + 1)
            }
        }
    }
    error("No solution found")
}

fun Pos.neighbors() = listOf(
    first - 1 to second,
    first + 1 to second,
    first to second - 1,
    first to second + 1,
    first - 1 to second - 1,
    first - 1 to second + 1,
    first + 1 to second - 1,
    first + 1 to second + 1
)

fun part2(input: Input): String {
    val dropped = mutableSetOf<Pos>()

    val n = 73 // grid size + borders
    val uf = UnionFind<Pos>(n*n) { (r, c) -> (r + 1) * n + c + 1 }

    fun drop(pos: Pos) {
        dropped += pos
        pos.neighbors().forEach { if (it in dropped) uf.union(pos, it) }
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
        if (isBlocked()) return "${it.first},${it.second}"
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
    a to b
}

fun main() {
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
