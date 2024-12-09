package day9

import go
import measure
import readAllText

typealias Input = List<Sector>

data class Chunk(val id: Int, val start: Int, val size: Int, val done: Boolean = false) {
    //
//}
//
//data class MutableChunk(val id: Int, val start: Int, val size: Int, val done: Boolean = false) {
    val nextFree get() = start + size
    override fun toString() = "$id @ $start..<$nextFree"
    fun moved(dest: Int) = copy(start = dest, done = true)
    fun checksum() = id.toLong() * size * (start + start + size - 1) / 2
}

data class MutableSector(val chunks: MutableList<Chunk>, var free: Int) {
    val nextFree get() = chunks.last().nextFree
    fun add(chunk: Chunk) = chunks.add(chunk.moved(nextFree)).also { free -= chunk.size }
    fun checksum() = chunks.sumOf(Chunk::checksum)
}

data class Sector(val chunks: List<Chunk>, val free: Int) {
    fun toMutableSector() = MutableSector(chunks.toMutableList(), free)
}

fun part1(input: Input): Long {

    val sectors = input.map(Sector::toMutableSector)

    sectors.asReversed().forEach { (currentChunks, _) ->
        currentChunks.asReversed().forEach { chunk ->
            var remaining = chunk
            sectors.asSequence()
                .dropWhile { it.free == 0 }
                .takeWhile { it.nextFree < chunk.start && remaining.size > 0 }
                .forEach { destination ->
                    val split = remaining.size.coerceAtMost(destination.free)
                    destination.add(remaining.copy(size = split))
                    currentChunks.remove(remaining)
                    remaining = remaining.copy(size = remaining.size - split)
                    if (remaining.size > 0) currentChunks.add(remaining)
                }
        }
    }
    return sectors.sumOf(MutableSector::checksum)
}

fun IntArray.checksum(): Long = foldIndexed(0L) { idx, acc, id ->
    acc + if (id > 0) id * idx else 0
}

fun part2(input: Input): Long {
    val sectors = input.map(Sector::toMutableSector)

    sectors.asReversed().forEach { (currentChunks, _) ->
        currentChunks.asReversed().filterNot { it.done }.forEach { chunk ->
            val destination = sectors.asSequence().takeWhile { it.nextFree < chunk.start }
                .firstOrNull { it.free >= chunk.size }
            if (destination != null) {
                currentChunks.remove(chunk)
                destination.add(chunk)
            }
        }
    }
    return sectors.sumOf(MutableSector::checksum)
}

fun parse(text: String): Input = buildList {
    var s = 0
    text.trim()
        .asSequence()
        .chunked(2)
        .map { it[0].digitToInt() to it.getOrElse(1) { '0' }.digitToInt() }
        .forEachIndexed { id, (size, free) ->
            add(Sector(listOf(Chunk(id, s, size)), free))
            s += size + free
        }
}

fun main() {
    val test = parse("2333133121414131402")
    go(1928) { part1(test) }
    val text = readAllText("local/day9_input.txt")
    val input = parse(text)
    go(6378826667552) { part1(input) }
    go(2858) { part2(test) }
    go(6413328569890) { part2(input) }
    measure(text, parse = ::parse, part1 = ::part1, part2 = ::part2)
}
