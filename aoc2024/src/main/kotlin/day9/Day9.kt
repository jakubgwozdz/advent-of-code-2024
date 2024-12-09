package day9

import go
import measure
import readAllText

data class Input(
    val files: List<Int>,
    val free: List<Int>,
)

data class Chunk(val id: Int, val start: Int, val size: Int, val done: Boolean = false) {
    val nextFree get() = start + size
    override fun toString() = "$id @ $start..<$nextFree"
    fun move(dest: Int) = copy(start = dest, done = true)
    fun checksum() = id.toLong() * size * (start + start + size - 1) / 2
}

data class ChunkWithFree(val chunks: MutableList<Chunk>, var free: Int) {
    val nextFree get() = chunks.last().nextFree
    fun add(chunk: Chunk) = chunks.add(chunk.move(nextFree)).also { free -= chunk.size }
    fun checksum() = chunks.sumOf(Chunk::checksum)
}

fun part1(input: Input): Long {
    var s = 0
    val chunks = input.files.mapIndexed { idx, size ->
        Chunk(idx, s, size)
            .also { s += size + input.free.getOrElse(idx) { 0 } }
    }
    val memory = IntArray(s) { -1 }
    chunks.forEach { (id, start, size) -> repeat(size) { memory[start + it] = id } }
    var end = memory.lastIndex
    var start = 0
    while (start < end) {
        while (memory[start] >= 0) start++
        while (memory[end] < 0) end--
        if (start < end) {
            memory[start++] = memory[end]
            memory[end--] = -1
        }
    }
    return memory.checksum()
}

fun IntArray.checksum(): Long = foldIndexed(0L) { idx, acc, id ->
    acc + if (id > 0) id * idx else 0
}

fun part2(input: Input): Long {
    val chunksWithFree: List<ChunkWithFree> = buildList {
        var s = 0
        input.files.forEachIndexed { idx, size ->
            val free = input.free.getOrElse(idx) { 0 }
            add(ChunkWithFree(mutableListOf(Chunk(idx, s, size)), free))
            s += size + free
        }
    }

    chunksWithFree.asReversed().forEach { (chunks, _) ->
        chunks.reversed().filterNot { it.done }.forEach { chunk ->
            chunksWithFree.asSequence().takeWhile { it.nextFree < chunk.start }
                .firstOrNull { it.free >= chunk.size }
                ?.let {
                    chunks.remove(chunk)
                    it.add(chunk)
                }
        }
    }
    return chunksWithFree.sumOf(ChunkWithFree::checksum)
}

fun parse(text: String): Input {
    val line = text.trim()
    val files = mutableListOf<Int>()
    val free = mutableListOf<Int>()
    var nextIsFile = true
    line.forEach {
        if (nextIsFile) files += "$it".toInt()
        else free += "$it".toInt()
        nextIsFile = !nextIsFile
    }
    return Input(files, free)
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
