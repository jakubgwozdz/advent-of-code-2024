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

data class Input(
    val robot: Pos,
    val boxes: Set<Pos>,
    val walls: Set<Pos>,
    val moves: List<Dir>,
)

fun part1(input: Input): Int = generateSequence(input) { state ->
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
                Input(nextRobot, state.boxes - nextRobot + nextBox, state.walls, nextMoves)
        }
    }
}
//        .onEach(State::print)
    .last().boxes.sumOf { (r, c) -> r * 100 + c }

val Pos.scaled: Pos get() = first to second * 2
val Pos.right: Pos get() = first to second + 1
val Pos.left: Pos get() = first to second - 1

fun Input.scaled() = Input(
    robot.scaled,
    boxes.map(Pos::scaled).toSet(),
    walls.map(Pos::scaled).toSet(),
    moves
)

fun Set<Pos>.collidingWide(other: Collection<Pos>): Set<Pos> =
    intersect(other.toSet()) + intersect(other.map(Pos::right).toSet()) + intersect(other.map(Pos::left).toSet())

fun part2(input: Input): Int = generateSequence(input.scaled()) { state ->
    if (state.moves.isEmpty()) null
    else {
        val move = state.moves.first()
        val nextMoves = state.moves.drop(1)
        val nextRobot = state.robot + move
        val boxesCollidingWithNextRobot = listOf(nextRobot, nextRobot.left).filter(state.boxes::contains).toSet()
        if (state.walls.contains(nextRobot) || state.walls.contains(nextRobot.left)) state.copy(moves = nextMoves)
        else if (boxesCollidingWithNextRobot.isNotEmpty()) {
            var isBlocked = false
            val boxesToMove = buildSet {
                generateSequence(boxesCollidingWithNextRobot) { colliding ->
                    addAll(colliding)
                    val moved = colliding.map { it + move }
                    val nextColliding = state.boxes.collidingWide(moved) - colliding
                    when {
                        state.walls.collidingWide(moved).isNotEmpty() -> null.also { isBlocked = true }
                        nextColliding.isEmpty() -> null
                        else -> nextColliding
                    }
                }.last()
            }
            if (isBlocked) state.copy(moves = nextMoves)
            else {
                val nextBoxes = state.boxes - boxesToMove + boxesToMove.map { it + move }
                Input(nextRobot, nextBoxes, state.walls, nextMoves)
            }
        } else state.copy(robot = nextRobot, moves = nextMoves)
    }
}
    .zipWithNext()
//    .onEach(Pair<Input, Input>::print)
    .last().second.boxes.sumOf { (r, c) -> r * 100 + c }

private fun Pair<Input, Input>.print() {
    val width = first.walls.maxOf { it.second } + 2
    val height = first.walls.maxOf { it.first } + 1
    repeat(height) { r ->
        repeat(width) { c ->
            print(first.wideChar(r to c))
        }
        val move = when (first.moves.first()) {
            -1 to 0 -> "  ↑  "
            1 to 0 -> "  ↓  "
            0 to -1 -> "  ←  "
            0 to 1 -> "  →  "
            else -> error("unexpected move: ${first.moves.first()}")
        }
        print((if (r == 1) move else "     "))
        repeat(width) { c ->
            print(second.wideChar(r to c))
        }
        println()
    }
    println()
}

private fun Input.wideChar(pos: Pair<Int, Int>) = when {
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

