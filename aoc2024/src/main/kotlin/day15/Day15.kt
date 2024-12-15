package day15

import go
import linesWithoutLastBlanks
import measure
import readAllText

typealias Dir = Pair<Int, Int>
typealias Pos = Pair<Int, Int>
typealias Grid = List<String>

data class Input(
    val robot: Pos,
    val boxes: Set<Pos>,
    val walls: Set<Pos>,
    val moves: List<Dir>,
)

data class State(
    val robot: Pos,
    val boxes: Set<Pos>,
)

val Pos.scaled: Pos get() = first to second * 2
val Pos.right: Pos get() = first to second + 1
val Pos.left: Pos get() = first to second - 1

fun Input.wide() = Input(
    robot.scaled,
    boxes.map(Pos::scaled).toSet(),
    walls.map(Pos::scaled).toSet(),
    moves
)

fun part1(input: Input): Int = solve(
    input = input,
    robotCollisions = { pos -> listOf(pos) },
    boxesCollisions = { pos -> listOf(pos) }
)

fun part2(input: Input): Int = solve(
    input = input.wide(),
    robotCollisions = { pos -> listOf(pos, pos.left) },
    boxesCollisions = { pos -> listOf(pos, pos.left, pos.right) }
)

fun solve(input: Input, robotCollisions: (Pos) -> List<Pos>, boxesCollisions: (Pos) -> List<Pos>) = input.moves
    .fold(State(input.robot, input.boxes)) { state, move ->
        makeMove(state, move, input.walls, robotCollisions, boxesCollisions).second
    }.boxes.sumOf { (r, c) -> r * 100 + c }

private fun makeMove(
    state: State,
    move: Dir,
    walls: Set<Pos>,
    robotCollisions: (Pos) -> List<Pos>,
    boxesCollisions: (Pos) -> List<Pos>
): Pair<List<Pair<Pos, Pos>>, State> {
    val movedBoxes = mutableListOf<Pair<Pos, Pos>>()

    val nextRobot = state.robot + move
    var toCheck = robotCollisions(nextRobot)

    var collidingBoxes = toCheck.filter { it in state.boxes }.toSet()

    var repeat = collidingBoxes.isNotEmpty() && toCheck.none { it in walls }
    while (repeat) {
        movedBoxes.addAll(collidingBoxes.map { it to it + move })

        toCheck = collidingBoxes.flatMap { boxesCollisions(it + move) }
        collidingBoxes = toCheck.filter { it in state.boxes }.toSet() - collidingBoxes

        repeat = collidingBoxes.isNotEmpty() && toCheck.none { it in walls }
    }

    return movedBoxes to when {
        toCheck.any { it in walls } -> state
        movedBoxes.isEmpty() -> state.copy(robot = nextRobot)
        else -> State(nextRobot, state.boxes.afterMove(movedBoxes))
    }
}

private fun Set<Pos>.afterMove(changes: Iterable<Pair<Pos, Pos>>) =
    this - changes.map { (src, _) -> src }.toSet() + changes.map { (_, dst) -> dst }

private fun Input.print(prev: State, next: State, index: Int) {
    val width = walls.maxOf { it.second } + 2
    val height = walls.maxOf { it.first } + 1
    repeat(height) { r ->
        repeat(width) { c -> print(prev.wideChar(r to c, walls)) }
        val move = when (moves[index]) {
            -1 to 0 -> "  ↑  "
            1 to 0 -> "  ↓  "
            0 to -1 -> "  ←  "
            0 to 1 -> "  →  "
            else -> error("unexpected move")
        }
        print((if (r == 1) move else "     "))
        repeat(width) { c -> print(next.wideChar(r to c, walls)) }
        println()
    }
    println()
}

private fun State.wideChar(pos: Pair<Int, Int>, walls: Set<Pos>) = when {
    pos == robot -> '@'
    pos in boxes -> '['
    pos.left in boxes -> ']'
    pos in walls -> '#'
    pos.left in walls -> '#'
    else -> '.'
}

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
    Input(
        robot = grid.findAll('@').single(),
        boxes = grid.findAll('O').toSet(),
        walls = grid.findAll('#').toSet(),
        moves = moves,
    )
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
    go(1575877) { part2(input) }
    measure(text, parse = ::parse, part1 = ::part1, part2 = ::part2)
}

fun Grid.findAll(ch: Char): Sequence<Pos> = asSequence().flatMapIndexed { r, line ->
    line.indices.filter { line[it] == ch }.map { c -> Pos(r, c) }
}

operator fun Pos.plus(d: Dir) = Pos(first + d.first, second + d.second)

