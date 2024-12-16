package day16

import go
import linesWithoutLastBlanks
import measure
import readAllText

data class Input(val grid: Grid, val start: Pos, val end: Pos, val places: Set<Pos>)

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

//data class State(val pos: Pos, val dir: Dir)
typealias State = Pair<Pos, Dir>

val State.pos get() = first
val State.dir get() = second

fun State.forward() = State(pos + dir, dir)
fun State.left() = State(pos + dir.turnLeft(), dir.turnLeft())
fun State.right() = State(pos + dir.turnRight(), dir.turnRight())

fun part1(input: Input): Int {

    val validMoves: MutableMap<State, List<Pair<State, Int>>> = mutableMapOf()
    val movesFor: (State) -> List<Pair<State, Int>> = { state ->
        validMoves.getOrPut(state) {
            listOf(state.forward() to 1, state.left() to 1001, state.right() to 1001)
                .filter { it.first.pos in input.places }
        }
    }

    val s0 = State(input.start, Dir.R) to 0
    val visited = mutableMapOf(s0)
    val queue = PriorityQueue<Pair<State, Int>>(compareBy { it.second })
    queue.offer(s0)
    while (queue.isNotEmpty()) {
        val (state, cost) = queue.poll()
        if (state.pos == input.end) return cost
        (movesFor(state)).forEach { (next, dc) ->
            val nextCost = cost + dc
            if (next !in visited || visited[next]!! > nextCost) {
                visited[next] = nextCost
                queue.offer(next to nextCost)
            }
        }
    }
    error("No path found")
}

data class Path(val pos: Pos, val dir: Dir, val cost: Int, val visited: Set<Pos>)

fun Path.forward() = Path(pos + dir, dir, cost + 1, visited + pos)
fun Path.left() = copy(dir = dir.turnLeft(), cost = cost + 1000).forward()
fun Path.right() = copy(dir = dir.turnRight(), cost = cost + 1000).forward()

fun part2(input: Input): Int {
    val start = input.start
    val end = input.end
    val places = input.places

    val totalCost = part1(input)

    val p0 = Path(start, Dir.R, 0, setOf(start))
    val placesVisited = mutableSetOf(start, end)
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

fun parse(text: String): Input = text.linesWithoutLastBlanks().let { grid ->
    val start = grid.findAll('S').single()
    val end = grid.findAll('E').single()
    val places = grid.findAll('.').toSet() + end + start
    Input(grid, start, end, places)
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
