package day17

import go
import measure
import readAllText

data class Input(
    val a: Long,
    val b: Long,
    val c: Long,
    val program: List<Long>,
)

fun part1(input: Input): String = runProgram(input).joinToString(",")

fun runProgram(input: Input): List<Long> {
    val output = mutableListOf<Long>()
    var addr = 0
    var a = input.a
    var b = input.b
    var c = input.c
    fun Long.combo(): Long {
        return when (this) {
            in 0..3 -> this
            4L -> a
            5L -> b
            6L -> c
            else -> throw IllegalArgumentException("$this: Not a combo")
        }
    }

    while (addr < input.program.size) {
        val opcode = input.program[addr++]
        val param = input.program[addr++]
        when (opcode) {
            0L -> a = a ushr param.combo().toInt()
            1L -> b = b xor param
            2L -> b = param.combo() % 8
            3L -> if (a != 0L) addr = param.toInt()
            4L -> b = b xor c
            5L -> output += param.combo() % 8
            6L -> b = a ushr param.combo().toInt()
            7L -> c = a ushr param.combo().toInt()
            else -> throw IllegalArgumentException("Invalid value")
        }
    }
    return output.toList()
}

fun part2(input: Input): Long {
    val q = mutableListOf(0L to 0)
    while (q.isNotEmpty()) {
        val (base, i) = q.removeFirst()
        val offset = (input.program.size - i - 1) * 3
        repeat(8) { j ->
            val a = base and (7L shl offset).inv() or (j.toLong() shl offset)
            val output = runProgram(input.copy(a = a))
            if (output == input.program) return a
            else if (output.takeLast(i + 1) == input.program.takeLast(i + 1)) q.add(a to i + 1)
        }
    }
    error("No solution found")
}

val regex = Regex("""Register A: (\d+)\nRegister B: (\d+)\nRegister C: (\d+)\n\nProgram: ([,0-9]+)""")

fun parse(text: String): Input = regex.find(text)!!.destructured.let { (a, b, c, program) ->
    Input(a.toLong(), b.toLong(), c.toLong(), program.split(",").map { it.toLong() })
}

val test1 = """
    Register A: 729
    Register B: 0
    Register C: 0

    Program: 0,1,5,4,3,0
""".trimIndent()

val test2 = """
    Register A: 2024
    Register B: 0
    Register C: 0

    Program: 0,3,5,4,3,0
""".trimIndent()

fun main() {
//    go("4,6,3,5,6,3,5,2,1,0", "part1(parse(test1)): ") { part1(parse(test1)) }
//    go(117440L, "part2(parse(test2)): ") { part2(parse(test2)) }
    val text = readAllText("local/day17_input.txt")
    val input = parse(text)
    println(runProgram(input.copy(a=265061364597659)))
    println(runProgram(input.copy(a=267277567730587)))
//TODO()
    go("1,4,6,1,6,4,3,0,3", "part1(input): ") { part1(input) }
    go(265061364597659, "part2(input): ") { part2(input) }
    measure(text, parse = ::parse, part1 = ::part1, part2 = ::part2)
}

