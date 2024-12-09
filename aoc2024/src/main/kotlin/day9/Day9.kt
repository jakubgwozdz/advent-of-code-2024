package day9

import go
import measure
import readAllText

typealias Input = List<Sector>

data class Sector(val chunks: List<Chunk>, val size: Int) {
    fun toMutableSector() = MutableSector(chunks.map { it.toMutableChunk() }.toMutableList(), size)
}

data class Chunk(val id: Int, val start: Int, val size: Int) {
    fun toMutableChunk(): MutableChunk = MutableChunk(id, start, size)
}

fun parse(text: String): Input = buildList {
    var s = 0
    text.trim()
        .asSequence()
        .chunked(2)
        .map { it[0].digitToInt() to it.getOrElse(1) { '0' }.digitToInt() }
        .forEachIndexed { id, (size, free) ->
            add(Sector(listOf(Chunk(id, s, size)), size + free))
            s += size + free
        }
}

data class MutableChunk(val id: Int, val start: Int, var size: Int, val done: Boolean = false) {
    fun extractTo(dest: MutableSector) {
        val copy = copy(start = dest.nextFree, size = this.size.coerceAtMost(dest.free), done = true)
        dest.add(copy)
        this.size -= copy.size
    }

    fun checksum() = id.toLong() * size * (start + start + size - 1) / 2
}

data class MutableSector(val chunks: MutableList<MutableChunk>, val size: Int) {
    val start: Int = chunks.first().start
    var nextFree = chunks.sumOf { it.size } + start
    var free = size - chunks.sumOf { it.size }

    fun checksum() = chunks.sumOf(MutableChunk::checksum)
    fun add(copy: MutableChunk) {
        chunks.add(copy)
        nextFree += copy.size
        free -= copy.size
    }
}

fun part1(input: Input): Long {
    val sectors = input.map(Sector::toMutableSector)
    var nextFreeSector = 0
    sectors.asReversed().forEach { current ->
        current.chunks.asReversed().forEach { chunk ->
            while (chunk.size > 0) {
                while (sectors[nextFreeSector].free == 0) nextFreeSector++
                if (sectors[nextFreeSector].nextFree < chunk.start) chunk.extractTo(sectors[nextFreeSector])
                else break
            }
        }
    }
    return sectors.sumOf(MutableSector::checksum)
}

fun part2(input: Input): Long {
    val sectors = input.map(Sector::toMutableSector)

    sectors.asReversed().forEach { current ->
        current.chunks.asReversed().filterNot { it.done }.forEach { chunk ->
            val destination = sectors.asSequence().takeWhile { it.nextFree < chunk.start }
                .firstOrNull { it.free >= chunk.size }
            if (destination != null) {
                chunk.extractTo(destination)
            }
        }
    }
    return sectors.sumOf(MutableSector::checksum)
}

fun main() {
    val test = parse("2333133121414131402")
    val text = readAllText("local/day9_input.txt")
    val input = parse(text)
    go(1928) { part1(test) }
    go(2858) { part2(test) }
    go(6378826667552) { part1(input) }
    go(6413328569890) { part2(input) }
    measure(text, parse = ::parse, part1 = ::part1, part2 = ::part2)
}
