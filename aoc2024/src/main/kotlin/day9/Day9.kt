package day9

import go
import measure
import readAllText

data class Input(
    val files: List<Int>,
    val free: List<Int>,
)

data class File(val id: Int, val start: Int, val size: Int)

fun part1(input: Input): Long {
    var s = 0
    val files = input.files.mapIndexed { idx, size ->
        File(idx, s, size)
            .also { s += size + input.free.getOrElse(idx) { 0 } }
    }
    val memory = IntArray(s) { -1 }
    files.forEach { (id, start, size) -> repeat(size) { memory[start + it] = id } }
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

private fun toStr(memory: IntArray) = memory
    .joinToString(" ") { (if (it < 0) "." else "$it").padStart(4) }
    .chunked(100)
    .joinToString("\n")

fun part2(input: Input): Long {
    var i = 0
    val files = input.files.withIndex().map { (id, length) ->
        val range = i..<(i + length)
        i += length
        i += input.free.getOrElse(id) { 0 }
        id to range
    }.toMutableList()
//    println(files)
    val filesAsMap = files.toMap().toMutableMap()
    input.files.indices.reversed().filter { it != 0 }.forEach { id ->
        val original = filesAsMap[id]!!
        val size = original.last - original.first + 1
//        println("at $id, $original, $size")

        // find free
        var i = 1
        var newRange: IntRange? = null
        while (files[i - 1].second.last + 1 < original.first && newRange == null) {
            newRange = (files[i - 1].second.last + 1)..(files[i].second.first - 1)
            if (newRange.last - newRange.first + 1 < size) {
                newRange = null
                i++
            }
        }
        if (newRange != null) {
            newRange = newRange.first..<(newRange.first + size)
            filesAsMap[id] = newRange
            files.remove(id to original)
            files.add(i, id to newRange)
        }
    }

    val length = input.files.sum() + input.free.sum()
    val memory = IntArray(length) { -1 }
    files.forEach { (id, length) ->
        length.forEach { i ->
            if (memory[i] != -1) {
                error("while setting $id memory[$i] already set to ${memory[i]}")
            }
            memory[i] = id
        }
    }
    return memory.checksum()
//    println(files)
//    return files.sumOf { (id, range) -> range.sumOf { it * id }.toLong() }
}

// 5142018250274 wrong

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
    TODO()
    measure(text, parse = ::parse, part1 = ::part1, part2 = ::part2)
}

