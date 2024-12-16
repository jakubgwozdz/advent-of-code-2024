package day16

import go
import linesWithoutLastBlanks
import measure
import readAllText

data class Input(val start: Pos, val end: Pos, val places: Set<Pos>)

typealias Pos = Pair<Int, Int>

enum class Dir { U, R, D, L }

fun Dir.turnRight() = Dir.entries[(ordinal + 1) % Dir.entries.size]
fun Dir.turnLeft() = Dir.entries[(ordinal - 1 + Dir.entries.size) % Dir.entries.size]
operator fun Pos.plus(d: Dir) = when (d) {
    Dir.U -> first - 1 to second
    Dir.R -> first to second + 1
    Dir.D -> first + 1 to second
    Dir.L -> first to second - 1
}

typealias Grid = List<String>

fun Grid.findAll(ch: Char): Sequence<Pos> = asSequence().flatMapIndexed { r, line ->
    line.indices.filter { line[it] == ch }.map { c -> Pos(r, c) }
}

data class State(val pos: Pos, val dir: Dir)

fun State.forward() = State(pos + dir, dir)
fun State.left() = State(pos + dir.turnLeft(), dir.turnLeft())
fun State.right() = State(pos + dir.turnRight(), dir.turnRight())

class Cache(val places: Set<Pos>) {
    private val validMoves: MutableMap<State, List<Pair<State, Int>>> = mutableMapOf()
    fun movesFor(state: State) = validMoves.getOrPut(state) {
        listOf(state.forward() to 1, state.left() to 1001, state.right() to 1001)
            .filter { it.first.pos in places }
    }
}

fun part1(input: Input): Int {
    val cache = Cache(input.places)
    val s0 = State(input.start, Dir.R) to 0
    val visited = mutableMapOf(s0)
    val queue = PriorityQueue(compareBy { it.second }, s0)
    while (queue.isNotEmpty()) {
        val (state, cost) = queue.poll()
        if (state.pos == input.end) return cost
        (cache.movesFor(state)).forEach { (next, dc) ->
            val nextCost = cost + dc
            if (next !in visited || visited[next]!! > nextCost) {
                visited[next] = nextCost
                queue.offer(next to nextCost)
            }
        }
    }
    error("No path found")
}

fun part2(input: Input): Int {
    val placesVisited = mutableSetOf(input.start, input.end)
    val totalCost = part1(input)
    val cache = Cache(input.places)
    val s0 = State(input.start, Dir.R) to (0 to setOf(input.start))
    val visited = mutableMapOf(s0.first to s0.second.first)
    val queue = PriorityQueue(compareBy { it.second.first },s0)
    while (queue.isNotEmpty()) {
        val (state, path) = queue.poll()
        val (cost, set) = path
        if (state.pos == input.end) placesVisited += set

        (cache.movesFor(state)).forEach { (next, dc) ->
            val nextCost = cost + dc
            if (nextCost <= totalCost && (next !in visited || visited[next]!! >= nextCost)) {
                visited[next] = nextCost
                queue.offer(next to (nextCost to set + next.pos))
            }
        }
    }
    return placesVisited.size
}

fun parse(text: String): Input = text.linesWithoutLastBlanks().let { grid ->
    val start = grid.findAll('S').single()
    val end = grid.findAll('E').single()
    val places = grid.findAll('.').toSet() + end + start
    Input(start, end, places)
}

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

open class Queue<E : Any>(vararg initial: E) {

    protected var queue: ArrayList<E> = ArrayList<E>(11).apply { addAll(initial) }

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


class PriorityQueue<E : Any>(val comparator: Comparator<E>, vararg initial: E) : Queue<E>(*initial) {

    override fun offer(e: E) {
        val index = queue.binarySearch(e, comparator).let {
            if (it < 0) -it - 1 else it
        }
        queue.add(index, e)
    }

    override fun toString(): String = queue.toString()
}
