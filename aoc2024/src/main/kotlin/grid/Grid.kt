package grid


typealias Move = Pair<Int, Int>

val diagonals: Set<Move> = setOf(-1 to -1, -1 to 1, 1 to -1, 1 to 1)
val vertical: Set<Move> = setOf(-1 to 0, 1 to 0)
val horizontal: Set<Move> = setOf(0 to -1, 0 to 1)

typealias Pos = Pair<Int, Int>

operator fun Pos.plus(d: Move) = Pos(first + d.first, second + d.second)
operator fun Pos.minus(d: Move) = Pos(first - d.first, second - d.second)

typealias Grid = List<String>

operator fun Grid.get(p: Pos) = getOrNull(p.first)?.getOrNull(p.second)
operator fun Grid.contains(p: Pos) = p.first in indices && p.second in get(p.first).indices

fun Grid.findAll(ch: Char): Sequence<Pos> = asSequence().flatMapIndexed { r, line ->
    line.indices.filter { line[it] == ch }.map { c -> Pos(r, c) }
}

enum class Dir(val move: Move) { U(-1 to 0), R(0 to 1), D(1 to 0), L(0 to -1) }

fun Dir.turnRight() = Dir.entries[(ordinal + 1) % Dir.entries.size]
operator fun Pos.plus(d: Dir) = this + d.move