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
    input = input.wide(), // same count of boxes etc, just x coordinate is doubled
    robotCollisions = { pos -> listOf(pos, pos.left) },
    boxesCollisions = { pos -> listOf(pos, pos.left, pos.right) }
)

fun solve(input: Input, robotCollisions: (Pos) -> List<Pos>, boxesCollisions: (Pos) -> List<Pos>) = input.moves
    .fold(State(input.robot, input.boxes)) { state, dir ->
        makeMove(state, dir, input.walls, robotCollisions, boxesCollisions).first
    }.boxes.sumOf { (r, c) -> r * 100 + c }

fun makeMove(
    state: State,
    dir: Dir,
    walls: Set<Pos>,
    robotCollisions: (Pos) -> List<Pos>,
    boxesCollisions: (Pos) -> List<Pos>
): Pair<State, Report> {
    val movedBoxes = mutableListOf<Pair<Pos, Pos>>()

    val nextRobot = state.robot + dir
    var toCheck = robotCollisions(nextRobot)
    var isLegal = toCheck.none { it in walls }
    var collidingBoxes = toCheck.filter { it in state.boxes }.toSet()

    var repeat = collidingBoxes.isNotEmpty() && isLegal
    while (repeat) {
        movedBoxes.addAll(collidingBoxes.map { it to it + dir })

        toCheck = collidingBoxes.flatMap { boxesCollisions(it + dir) }
        isLegal = toCheck.none { it in walls }
        collidingBoxes = toCheck.filter { it in state.boxes }.toSet() - collidingBoxes

        repeat = collidingBoxes.isNotEmpty() && isLegal
    }

    return when {
        !isLegal -> state
        movedBoxes.isEmpty() -> state.copy(robot = nextRobot)
        else -> State(
            nextRobot,
            state.boxes - movedBoxes.map { (src, _) -> src }.toSet() + movedBoxes.map { (_, dst) -> dst }
        )
    } to Report(dir, isLegal, state.robot to nextRobot, movedBoxes) // report is for animation only
}

data class Report(
    val dir: Dir,
    val success: Boolean,
    val robot: Pair<Pos, Pos>,
    val movedBoxes: List<Pair<Pos, Pos>>
)

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

