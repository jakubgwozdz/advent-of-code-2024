package day16

import go
import linesWithoutLastBlanks
import measure
import readAllText

typealias Input = List<String>

enum class Dir { U, R, D, L }

fun Dir.turnRight() = Dir.entries[(ordinal + 1) % Dir.entries.size]
fun Dir.turnLeft() = Dir.entries[(ordinal - 1 + Dir.entries.size) % Dir.entries.size]
operator fun Pos.plus(d: Dir) = when (d) {
    Dir.U -> first - 1 to second
    Dir.R -> first to second + 1
    Dir.D -> first + 1 to second
    Dir.L -> first to second - 1
}


typealias Pos = Pair<Int, Int>

typealias Grid = List<String>

fun Grid.findAll(ch: Char): Sequence<Pos> = asSequence().flatMapIndexed { r, line ->
    line.indices.filter { line[it] == ch }.map { c -> Pos(r, c) }
}

data class State(val pos: Pos, val dir: Dir, val cost: Int)

fun part1(input: Input): Int {
    val grid: Grid = input
    val start = grid.findAll('S').single()
    val end = grid.findAll('E').single()
    val places = grid.findAll('.').toSet() + end
    val s0 = State(start, Dir.R, 0)
    val visited = mutableMapOf(s0.pos to s0.dir to s0.cost)
    val queue = PriorityQueue<State>(compareBy { it.cost })
    queue.offer(s0)
    while (queue.isNotEmpty()) {
        val (pos, dir, cost) = queue.poll()
        if (pos == end) return cost

        listOf(dir to 1, dir.turnLeft() to 1001, dir.turnRight() to 1001)
            .map { (d, dc) -> (pos + d) to d to (cost + dc) }
            .filter { (k, v) -> k.first in places && (k !in visited || visited[k]!! > v) }
            .forEach { (k, v) ->
                visited[k] = v
                queue.offer(State(k.first, k.second, v))
            }
    }
    error("No path found")
}

data class Path(val pos: Pos, val dir: Dir, val cost: Int, val visited: Set<Pos>)

fun Path.forward() = Path(pos + dir, dir, cost + 1, visited + pos)
fun Path.left() = copy(dir = dir.turnLeft(), cost = cost + 1000).forward()
fun Path.right() = copy(dir = dir.turnRight(), cost = cost + 1000).forward()

fun part2(input: Input): Int {
    val grid: Grid = input
    val start = grid.findAll('S').single()
    val end = grid.findAll('E').single()
    val places = grid.findAll('.').toSet() + end

    val totalCost = part1(input)

    val p0 = Path(start, Dir.R, 0, setOf(start))
    val placesVisited = mutableSetOf(start,end)
    val visited = mutableMapOf(p0.pos to p0.dir to p0.cost)

    val queue = PriorityQueue<Path>(compareBy { it.cost })
//    val queue = Queue<Path>()
    queue.offer(p0)
    while (queue.isNotEmpty()) {
        val path = queue.poll()
        if (path.pos == end) placesVisited += path.visited
        listOf(path.forward(), path.left(), path.right())
            .filter { it.pos in places && it.cost <= totalCost && (it.pos to it.dir !in visited || visited[it.pos to it.dir]!! >= it.cost) }
            .forEach {
                queue.offer(it)
                visited[it.pos to it.dir] = it.cost
            }
    }
    return placesVisited.size
}

fun parse(text: String) = text.linesWithoutLastBlanks()

val test2 = """
    #################
    #...#...#...#..E#
    #.#.#.#.#.#.#.#.#
    #.#.#.#...#...#.#
    #.#.#.#.###.#.#.#
    #...#.#.#.....#.#
    #.#.#.#.#.#####.#
    #.#...#.#.#.....#
    #.#.#####.#.###.#
    #.#.#.......#...#
    #.#.###.#####.###
    #.#.#...#.....#.#
    #.#.#.#####.###.#
    #.#.#.........#.#
    #.#.#.#########.#
    #S#.............#
    #################
""".trimIndent()

fun main() {
    val text = readAllText("local/day16_input.txt")
    val input = parse(text)
    go(11048) { part1(parse(test2)) }
    go(109516) { part1(input) }
    go(64) { part2(parse(test2)) }
    go(568) { part2(input) }
    measure(text, parse = ::parse, part1 = ::part1, part2 = ::part2)
}

open class Queue<E : Any> {

    protected var queue: ArrayList<E> = ArrayList(11)

    val size get() = queue.size

    fun isNotEmpty(): Boolean = size > 0

    fun poll(): E {
        check(size > 0)
        return queue.removeAt(0)
    }

    open fun offer(e: E) {
        queue.add(e)
    }

}


class PriorityQueue<E : Any>(val comparator: Comparator<E>) : Queue<E>() {

    override fun offer(e: E) {
        val index = queue.binarySearch(e, comparator).let {
            if (it < 0) -it - 1 else it
        }
        queue.add(index, e)
    }

    override fun toString(): String = queue.toString()
}
