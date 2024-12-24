package day24

import go
import measure
import readAllText

data class Instruction(val input1: String, val op: String, val input2: String)
data class Input(val initial: Map<String, Boolean>, val instructions: Map<String, Instruction>)

fun solve(name: String, results: MutableMap<String, Boolean>, instructions: Map<String, Instruction>): Boolean {
    return results.getOrPut(name) {
        val instr = instructions[name]!!
        when (instr.op) {
            "AND" -> solve(instr.input1, results, instructions) and solve(instr.input2, results, instructions)
            "OR" -> solve(instr.input1, results, instructions) or solve(instr.input2, results, instructions)
            "XOR" -> solve(instr.input1, results, instructions) xor solve(instr.input2, results, instructions)
            else -> error("Unknown op: ${instr.op}")
        }
    }
}

//262651624 wrong
fun part1(input: Input): Long {
    val results = input.initial.toMutableMap()

    return input.instructions.keys.filter { it.startsWith('z') }
        .sortedDescending()
        .onEach { solve(it, results, input.instructions) }
        .fold(0L) { acc, name -> acc * 2 + if (results[name]!!) 1 else 0 }
}

fun part2(input: Input): Any {
    TODO()
}

val instrRegex = Regex("""(.{3}) (OR|XOR|AND) (.{3}) -> (.{3})""")

fun parse(text: String): Input {
    val initial = text.lineSequence().takeWhile { it.isNotEmpty() }
        .map {
            val (name, value) = it.split(": ")
            name to (value == "1")
        }.toMap()
    val instructions = text.lineSequence().dropWhile { it.isNotEmpty() }.drop(1).takeWhile { it.isNotEmpty() }
        .map {
            instrRegex.matchEntire(it)!!.destructured.let { (input1, op, input2, output) ->
                output to Instruction(input1, op, input2)
            }
        }.toMap()
    return Input(initial, instructions)
//        .also {
//            it.initial.forEach { (k, v) -> println("$k = $v") }
//            it.instructions.forEach { (k, v) -> println("$k = $v") }
//        }
}

fun main() {
    val text = readAllText("local/day24_input.txt")
    val input = parse(text)
    go(4) { part1(parse(example1)) }
    go(2024) { part1(parse(example2)) }
    go(desc = "Part 1:") { part1(input) }
    go() { part2(input) }
    TODO()
    measure(text, parse = ::parse, part1 = ::part1, part2 = ::part2)
}

val example1 ="""
    x00: 1
    x01: 1
    x02: 1
    y00: 0
    y01: 1
    y02: 0

    x00 AND y00 -> z00
    x01 XOR y01 -> z01
    x02 OR y02 -> z02
""".trimIndent()

val example2 = """
    x00: 1
    x01: 0
    x02: 1
    x03: 1
    x04: 0
    y00: 1
    y01: 1
    y02: 1
    y03: 1
    y04: 1

    ntg XOR fgs -> mjb
    y02 OR x01 -> tnw
    kwq OR kpj -> z05
    x00 OR x03 -> fst
    tgd XOR rvg -> z01
    vdt OR tnw -> bfw
    bfw AND frj -> z10
    ffh OR nrd -> bqk
    y00 AND y03 -> djm
    y03 OR y00 -> psh
    bqk OR frj -> z08
    tnw OR fst -> frj
    gnj AND tgd -> z11
    bfw XOR mjb -> z00
    x03 OR x00 -> vdt
    gnj AND wpb -> z02
    x04 AND y00 -> kjc
    djm OR pbm -> qhw
    nrd AND vdt -> hwm
    kjc AND fst -> rvg
    y04 OR y02 -> fgs
    y01 AND x02 -> pbm
    ntg OR kjc -> kwq
    psh XOR fgs -> tgd
    qhw XOR tgd -> z09
    pbm OR djm -> kpj
    x03 XOR y03 -> ffh
    x00 XOR y04 -> ntg
    bfw OR bqk -> z06
    nrd XOR fgs -> wpb
    frj XOR qhw -> z04
    bqk OR frj -> z07
    y03 OR x01 -> nrd
    hwm AND bqk -> z03
    tgd XOR rvg -> z12
    tnw OR pbm -> gnj
""".trimIndent()
