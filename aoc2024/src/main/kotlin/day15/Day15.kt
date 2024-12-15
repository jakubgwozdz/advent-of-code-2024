package day15

import go
import linesWithoutLastBlanks
import measure
import readAllText

typealias Dir = Pair<Int, Int>
typealias Pos = Pair<Int, Int>
typealias Grid = List<String>

fun Grid.findAll(ch: Char): Sequence<Pos> = asSequence().flatMapIndexed { r, line ->
    line.indices.filter { line[it] == ch }.map { c -> Pos(r, c) }
}

operator fun Pos.plus(d: Dir) = Pos(first + d.first, second + d.second)

typealias Input = Pair<Grid, List<Dir>>

data class State(
    val robot: Pos,
    val boxes: Set<Pos>,
    val walls: Set<Pos>,
    val moves: List<Dir>,
)

fun part1(input: Input): Int {
    val initial = State(
        robot = input.first.findAll('@').single(),
        boxes = input.first.findAll('O').toSet(),
        walls = input.first.findAll('#').toSet(),
        moves = input.second,
    )
    val last = generateSequence(initial) { state ->
        if (state.moves.isEmpty()) null
        else {
            val move = state.moves.first()
            val nextMoves = state.moves.drop(1)
            val nextRobot = state.robot + move
            if (nextRobot in state.walls) state.copy(moves = nextMoves)
            else if (nextRobot !in state.boxes) state.copy(robot = nextRobot, moves = nextMoves)
            else {
                val nextBox = generateSequence(nextRobot) { pos ->
                    if (pos in state.boxes) pos + move else null
                }.last()
                if (nextBox in state.walls) state.copy(moves = nextMoves)
                else
                    State(nextRobot, state.boxes - nextRobot + nextBox, state.walls, nextMoves)
            }
        }
    }
//        .onEach(State::print)
        .last()
    return last.boxes.sumOf { (r, c) -> r * 100 + c }
}

private fun State.print() {
    val width = walls.maxOf { it.second } + 1
    val height = walls.maxOf { it.first } + 1
    repeat(height) { r ->
        repeat(width) { c ->
            val pos = r to c
            when (pos) {
                robot -> print('@')
                in boxes -> print('O')
                in walls -> print('#')
                else -> print('.')
            }
        }
        println()
    }
    println()
}

fun part2(input: Input) = part1(input)

fun parse(text: String): Input = text.linesWithoutLastBlanks().let { lines ->
    val grid = lines.takeWhile { it.isNotEmpty() }
    val moves = lines.dropWhile { it.isNotEmpty() }.drop(1).joinToString("").map {
        when (it) {
            '^' -> -1 to 0
            'v' -> 1 to 0
            '<' -> 0 to -1
            '>' -> 0 to 1
            else -> error("unexpected char: $it")
        }
    }
    grid to moves
}

val largerTest = """
    ##########
    #..O..O.O#
    #......O.#
    #.OO..O.O#
    #..O@..O.#
    #O#..O...#
    #O..O..O.#
    #.OO.O.OO#
    #....O...#
    ##########

    <vv>^<v^>v>^vv^v>v<>v^v<v<^vv<<<^><<><>>v<vvv<>^v^>^<<<><<v<<<v^vv^v>^
    vvv<<^>^v^^><<>>><>^<<><^vv^^<>vvv<>><^^v>^>vv<>v<<<<v<^v>^<^^>>>^<v<v
    ><>vv>v^v^<>><>>>><^^>vv>v<^^^>>v^v^<^^>v^^>v^<^v>v<>>v^v^<v>v^^<^^vv<
    <<v<^>>^^^^>>>v^<>vvv^><v<<<>^^^vv^<vvv>^>v<^^^^v<>^>vvvv><>>v^<<^^^^^
    ^><^><>>><>^^<<^^v>>><^<v>^<vv>>v>>>^v><>^v><<<<v>>v<v<v>vvv>^<><<>^><
    ^>><>^v<><^vvv<^^<><v<<<<<><^v<<<><<<^^<v<^^^><^>>^<v^><<<^>>^v<v^v<v^
    >^>>^v>vv>^<<^v<>><<><<v<<v><>v<^vv<<<>^^v^>^^>>><<^v>>v^v><^^>>^<>vv^
    <><^^>^^^<><vvvvv^v<v<<>^v<v>v<<^><<><<><<<^^<<<^<<>><<><^^^>^^<>^>v<>
    ^^>vv<^v^v<vv>^<><v<^v>^^^>>>^^vvv^>vvv<>>>^<^>>>>>^<<^v>^vvv<>^<><<v>
    v^^>>><<^^<>>^v^<v^vv<>v^<<>^<^v^v><^<<<><<^<v><v<>vv>>v><v^<vv<>v^<<^
""".trimIndent()

val smallerTest = """
    ########
    #..O.O.#
    ##@.O..#
    #...O..#
    #.#.O..#
    #...O..#
    #......#
    ########

    <^^>>>vv<v>>v<<
""".trimIndent()

fun main() {
    val text = readAllText("local/day15_input.txt")
    val input = parse(text)
    go(2028) { part1(parse(smallerTest)) }
    go(10092) { part1(parse(largerTest)) }
    go(9021) { part2(parse(largerTest)) }
    go(1568399) { part1(input) }
    go() { part2(input) }
    TODO()
    measure(text, parse = ::parse, part1 = ::part1, part2 = ::part2)
}

