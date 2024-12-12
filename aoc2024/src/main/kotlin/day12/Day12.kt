package day12

import go
import linesWithoutLastBlanks
import measure
import readAllText

enum class Dir { U, R, D, L }
typealias Pos = Pair<Int, Int>
typealias Region = List<Pos>
typealias Fence = Pair<Pos, Dir>
typealias Perimeter = Set<Fence>
typealias Input = List<String>

fun Input.asRegions(): Collection<Region> =
    flatMapIndexed { row, line -> line.indices.map { col -> row to col } }
        .groupBy { (r, c) -> this[r][c] }.values
        .flatMap { it.continuous() }

fun Region.continuous() = buildList<Region> {
    val toGo = this@continuous.toMutableSet()
    while (toGo.isNotEmpty()) add(buildList {
        val queue = mutableListOf(toGo.first().also { toGo.remove(it) })
        while (queue.isNotEmpty()) {
            val element = queue.removeFirst()
            add(element)
            element.neighbours().filter { (pos, _) -> pos in toGo }.forEach { (pos, _) ->
                toGo.remove(pos)
                queue.add(pos)
            }
        }
    })
}

fun Region.perimeter(): Perimeter = flatMap { it.neighbours().filterNot { (pos, _) -> pos in this } }.toSet()

fun Perimeter.discounted(): Perimeter = filterNot { (pos, dir) -> (pos + dir.turnRight() to dir) in this }.toSet()

fun part1(input: Input) = input.asRegions()
    .sumOf { it.size * it.perimeter().size }

fun part2(input: Input) = input.asRegions()
    .sumOf { it.size * it.perimeter().discounted().size }

fun parse(text: String): Input = text.linesWithoutLastBlanks()

fun main() {
    val test1 = """
        AAAA
        BBCD
        BBCC
        EEEC
    """.trimIndent()
    val test2 = """
        OOOOO
        OXOXO
        OOOOO
        OXOXO
        OOOOO
    """.trimIndent()
    val test3 = """
        RRRRIICCFF
        RRRRIICCCF
        VVRRRCCFFF
        VVRCCCJFFF
        VVVVCJJCFE
        VVIVCCJJEE
        VVIIICJJEE
        MIIIIIJJEE
        MIIISIJEEE
        MMMISSJEEE
    """.trimIndent()

    val text = readAllText("local/day12_input.txt")
    val input = parse(text)

    go(140) { part1(parse(test1)) }
    go(772) { part1(parse(test2)) }
    go(1930) { part1(parse(test3)) }

    go(1415378) { part1(input) }

    go(80) { part2(parse(test1)) }
    go(436) { part2(parse(test2)) }
    go(1206) { part2(parse(test3)) }
    go(862714) { part2(input) }
    measure(text, parse = ::parse, part1 = ::part1, part2 = ::part2)
}

operator fun Pos.plus(d: Dir): Pos = when (d) {
    Dir.U -> first - 1 to second
    Dir.R -> first to second + 1
    Dir.D -> first + 1 to second
    Dir.L -> first to second - 1
}

fun Pos.neighbours(): List<Fence> = Dir.entries.map { this + it to it }

fun Dir.turnRight() = Dir.entries[(ordinal + 1) % Dir.entries.size]

