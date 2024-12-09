package day9

import go
import measure
import readAllText

data class Input(
    val files: List<Int>,
    val free: List<Int>,
)

data class Chunk(val id: Int, val start: Int, val size: Int) {
    val nextFree get() = start + size
    override fun toString() = "$id @ $start..<$nextFree"
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
    var s = 0
    val chunks = input.files.mapIndexed { idx, size ->
        Chunk(idx, s, size)
            .also { s += size + input.free.getOrElse(idx) { 0 } }
    }
    val compacted = SortedList(chunks, compareBy { it.start })
    chunks.asReversed().forEach { original ->
        val found: Int? = compacted.asSequence().zipWithNext()
            .takeWhile { (f1, f2) -> f1.nextFree < original.start }
            .firstOrNull { (f1, f2) -> f2.start - f1.nextFree >= original.size }
            ?.first?.nextFree
        if (found != null) {
            compacted.remove(original)
            compacted.add(original.copy(start = found))
        }
    }
    return compacted.backing.sumOf { (id, start, size) -> (0..<size).sumOf { id.toLong() * (start + it) } }
}

class SortedList<E>(initial: List<E>, val comparator: Comparator<E>) {
    val backing: ArrayList<E> = ArrayList(initial)
    fun asSequence(): Sequence<E> = backing.asSequence()
    fun forEach(f: (E) -> Unit) = backing.forEach(f)
    fun add(element: E) = backing.add(indexFor(element), element)
    fun remove(element: E): Boolean = indexFor(element).let {
        if (it in backing.indices) {
            backing.removeAt(it)
            true
        } else false
    }

    // differs from indexOf
    private fun indexFor(element: E): Int = backing.binarySearch(element, comparator).let {
        if (it < 0) -it - 1 else it
    }
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

