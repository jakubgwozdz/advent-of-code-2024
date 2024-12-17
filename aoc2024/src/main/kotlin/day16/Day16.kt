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
    val start = State(input.start, Dir.R)
    val endOp = { state: State -> state.pos == input.end }
    val movesOp = { state: State -> cache.movesFor(state) }

    val s0 = start to 0
    val visited = mutableMapOf(s0)
    val queue = PriorityQueue(compareBy { it.second }, s0)
    while (queue.isNotEmpty()) {
        val (state, cost) = queue.poll()
        if (endOp(state)) return cost
        (movesOp(state)).forEach { (next, dc) ->
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
    var totalCost = Int.MAX_VALUE
    val placesVisited = mutableListOf(input.start, input.end)

    val cache = Cache(input.places)
    val start = State(input.start, Dir.R)

    val s0 = start to (0 to listOf(start.pos))
    val visited = mutableMapOf(start to s0.second.first)
    val queue = PriorityQueue(compareBy { it.second.first }, s0)
    while (queue.isNotEmpty()) {
        val (state, soFar) = queue.poll()
        val (cost, path) = soFar
        if (state.pos == input.end) {
            if (cost < totalCost) {
                totalCost = cost
                placesVisited.clear()
            }
            placesVisited += path
        }
        (cache.movesFor(state)).forEach { (next, dc) ->
            val nextCost = cost + dc
            if (nextCost <= totalCost && (next !in visited || visited[next]!! >= nextCost)) {
                visited[next] = nextCost
                queue.offer(next to (nextCost to path + next.pos))
            }
        }
    }
    return placesVisited.distinct().size
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
    val text = readAllText("local/day16_input_expensive.txt")
    val input = parse(text)
    go(11048) { part1(parse(test2)) }
//    go(109516) { part1(input) }
    go(64) { part2(parse(test2)) }
//    go(568) { part2(input) }
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
