package day17

import go
import measure
import readAllText
import java.time.Instant

data class Input(
    val a: Long,
    val b: Long,
    val c: Long,
    val program: List<Long>,
)

data class State(
    val a: Long,
    val b: Long,
    val c: Long,
    val addr: Int,
    val output: List<Long>,
)

fun part1(input: Input): String = runProgram(input).joinToString(",")

private fun runProgram(input: Input): List<Long> {
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
    fun <T> T.log(op:(T)->String) = this//also { println(op(this)) }
    while (addr < input.program.size) {
        val opcode = input.program[addr++]
        val param = input.program[addr++]
//        println("$opcode $param; a=$a b=$b c=$c addr=$addr output=$output")
        when (opcode) {
            0L -> a = a ushr param.combo().toInt().log { "a = a($a) >> $it" }
            1L -> b = b xor param.log { "b = b($b) xor $it" }
            2L -> b = param.combo().log { "b = $it % 8" } % 8
            3L -> if (a != 0L) addr = param.toInt().log { "addr = $it" }
            4L -> b = b xor c.log { "b = b($b) xor c($c)" }
            5L -> output.add(param.combo().log { "out $it % 8" } % 8)
            6L -> b = a ushr param.combo().toInt().log { "b = a($a) >> $it" }
            7L -> c = a ushr param.combo().toInt().log { "c = a($a) >> $it" }
            else -> throw IllegalArgumentException("Invalid value")
        }
    }
    return output.toList()
}
// 130505386750736 too low
fun part2(input: Input): Long {
    TODO()
    val powers = IntArray(input.program.size) { 0 }

    repeat(input.program.size) { i ->
        println("index: $i")
        (0..7).last {
            powers[i] = it
            val a = powers.fold(0L) { acc, j -> acc * 8 + j }
            val output = runProgram(input.copy(a = a))
            println("${powers.joinToString("")}: $output")
//            println("${powers.joinToString("")} ${a.toString(8).padStart(16)}: $output")
            output.size == input.program.size && output.takeLast(i+1) == input.program.takeLast(i+1)
        }
    }

    return powers.fold(0L) { acc, j -> acc * 8 + j }
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
    go("4,6,3,5,6,3,5,2,1,0","part1(parse(test1)): ") { part1(parse(test1)) }
//    go(117440L, "part2(parse(test2)): ") { part2(parse(test2)) }
    val text = readAllText("local/day17_input.txt")
    val input = parse(text)
    go("1,4,6,1,6,4,3,0,3", "part1(input): ") { part1(input) }
    go(null, "part2(input): ") { part2(input) }
    measure(text, parse = ::parse, part1 = ::part1, part2 = ::part2)
}

