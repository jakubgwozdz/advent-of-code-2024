package day24

import go
import measure
import readAllText
import java.nio.file.Files
import java.nio.file.Paths

data class Instruction(val input1: String, val op: String, val input2: String) {
    override fun toString() = "$input1 $op $input2"
}

sealed interface Operand : Comparable<Operand> {
    fun unwind(instructions: Map<String, Instruction>): Operand
}

data class Signal(val name: String) : Operand {
    override fun unwind(instructions: Map<String, Instruction>) = instructions[name]
        ?.let { (input1, op, input2) ->
            val n1 = Signal(input1)
            val n2 = Signal(input2)
            if (n1 <= n2) Gate(name, op, n1, n2) else Gate(name, op, n2, n1)
        } ?: this

    override fun compareTo(other: Operand): Int = when (other) {
        is Signal -> nameComparator.compare(this, other)
        is Gate -> -1
    }

    override fun toString() = name
}

val nameComparator =
    compareBy<Signal> { if (it.name.startsWith('x') || it.name.startsWith('y')) it.name else "z${it.name}" }

data class Gate(val name: String, val op: String, val input1: Operand, val input2: Operand) : Operand {
    override fun compareTo(other: Operand) = when {
        other is Gate && this.op == other.op -> input1.compareTo(other.input1)
        other is Gate -> op.compareTo(other.op)
        else -> 1
    }

    override fun unwind(instructions: Map<String, Instruction>): Gate {
        val n1 = input1.unwind(instructions)
        val n2 = input2.unwind(instructions)
        return if (n1 <= n2) Gate(name, op, n1, n2) else Gate(name, op, n2, n1)
    }

    override fun toString() = "$name($input1 ${op.padStart(3)} $input2)"
}


data class Input(val initial: Map<String, Boolean>, val instructions: Map<String, Instruction>)

fun solve(name: String, results: MutableMap<String, Boolean>, instructions: Map<String, Instruction>): Boolean =
    results.getOrPut(name) {
        val instr = instructions[name]!!
        when (instr.op) {
            "AND" -> solve(instr.input1, results, instructions) and solve(instr.input2, results, instructions)
            "OR" -> solve(instr.input1, results, instructions) or solve(instr.input2, results, instructions)
            "XOR" -> solve(instr.input1, results, instructions) xor solve(instr.input2, results, instructions)
            else -> error("Unknown op: ${instr.op}")
        }
    }

//262651624 wrong
fun part1(input: Input): Long {
    val results = input.initial.toMutableMap()

    return count('z', results, input.instructions)
}

private fun count(char: Char, results: MutableMap<String, Boolean>, map: Map<String, Instruction>) =
    (map.keys + results.keys).filter { it.startsWith(char) }
        .sortedDescending()
        .onEach { solve(it, results, map) }
        .fold(0L) { acc, name -> acc * 2 + if (results[name]!!) 1 else 0 }

// cnk,ndq,qwf,z14,z27,z28,z39,z40 wrong???

fun part2(input: Input): Any {
    println()
    // z09(cnk(x09 AND y09) XOR hhp(gbf  OR gnt))
    // z14(x14 AND y14)
    // z27(jgq(x27 AND y27)  OR snv(kqj AND kqw))
    // z28(bhb(x28 XOR y28) XOR mps(kqj XOR kqw))
    // z39(gpm(dsj  OR pqr) AND trn(x39 XOR y39))
    // z45(vss(x44 AND y44)  OR psd(ndn AND njd))
    val swaps: List<Pair<String, String>> = listOf(
        "cnk" to "qwf",
        "z14" to "vhm",
        "z27" to "mps",
        "z39" to "msq",
    )

    val instructions = input.instructions.toMutableMap().apply {
        swaps.forEach { (a, b) -> swap(a, b) }
    }

    val adders: MutableList<Operand> = (0..45).map { i ->
        val id = i.toString().padStart(2, '0')
        val z = "z$id"
        Signal(z)
    }.toMutableList()
    adders.forEachIndexed { k, v -> adders[k] = v.unwind(instructions) }
    adders.forEachIndexed { k, v -> adders[k] = v.unwind(instructions) }
    adders.forEachIndexed { k, v -> adders[k] = v.unwind(instructions) }
    adders.forEachIndexed { k, v -> println(v) }


    val translationsFrom = mutableMapOf<String, String>()
    val translationsTo = mutableMapOf<String, String>()

    if (false)
        repeat(46) { i ->
            val id = i.toString().padStart(2, '0')
            val idNext = (i + 1).toString().padStart(2, '0')
            val idPrev = (i - 1).toString().padStart(2, '0')
            val x = "x$id"
            val y = "y$id"
            val carry = "cout$id"
            val carryPrev = "cout$idPrev"
            val carryIn = "cin$id"
            val carryAny = "cany$id"
            val carryAnyPrev = "cany$idPrev"
            val sum = "s$id"

            // half adder
            instructions
                .filterValues { it.valid(x, y) }
                .forEach { (oldId, v) ->
                    val newId = oldId.takeIf { it.last().isDigit() } ?: when (v.op) {
                        "AND" -> carry
                        "XOR" -> sum
//                else -> oldId
                        else -> error("Unknown op: ${v.op}")
                    }
                    replace(newId, oldId, instructions, translationsTo, translationsFrom)
                }
            // full adder
            instructions
                .filterValues { it.valid(sum, carryPrev) }
                .forEach { (oldId, v) ->
                    val newId = oldId.takeIf { it.last().isDigit() } ?: when (v.op) {
                        "AND" -> carryIn
//                else -> oldId
                        else -> error("Unknown op: ${v.op}")
                    }
                    replace(newId, oldId, instructions, translationsTo, translationsFrom)
                }
            instructions
                .filterValues { it.valid(sum, carryAnyPrev) }
                .forEach { (oldId, v) ->
                    val newId = oldId.takeIf { it.last().isDigit() } ?: when (v.op) {
                        "AND" -> carryIn
//                else -> oldId
                        else -> error("Unknown op: ${v.op}")
                    }
                    replace(newId, oldId, instructions, translationsTo, translationsFrom)
                }
            instructions
                .filterValues { it.valid(carry, carryIn) }
                .forEach { (oldId, v) ->
                    val newId = oldId.takeIf { it.last().isDigit() } ?: when (v.op) {
                        "OR" -> carryAny
//                else -> oldId
                        else -> error("Unknown op: ${v.op}")
                    }
                    replace(newId, oldId, instructions, translationsTo, translationsFrom)
                }

        }

    println("Translations:")
    println(translationsFrom)
    println(translationsTo)


    val results = input.initial.toMutableMap()

    val diff = diffs(results, instructions)
    if (diff == 0L) {
        return swaps.flatMap { it.toList() }.sorted().joinToString(",")
    }


//    val bad = (0 until 46).filter { diff and (1L shl it) != 0L }.map { "z${it.toString().padStart(2, '0')}" }.toSet()

//    graphviz(results, instructions)
    val bad = emptySet<String>()
    graphviz(results, instructions, bad).let {
        Files.writeString(Paths.get("local/day24.dot"), it)
        ProcessBuilder().command("dot", "-Tpng", "-o", "local/day24.png", "local/day24.dot").start().waitFor()
    }

    error("Wrong")
}

private fun diffs(
    results: MutableMap<String, Boolean>,
    instructions: Map<String, Instruction>
): Long {
    val x = count('x', results, instructions)
    val y = count('y', results, instructions)
    val z = count('z', results, instructions)
    val diff = (x + y) xor z

    println("id:       " + (45 downTo 0).joinToString("") { "${it / 10}" })
    println("          " + (45 downTo 0).joinToString("") { "${it % 10}" })
    println("x:         ${x.toString(2)}")
    println("y:         ${y.toString(2)}")
    println("(x+y):    ${(x + y).toString(2)}")
    println("z:        ${z.toString(2).padStart(46, '0')}")

    println("diff:     ${diff.toString(2).padStart(46, '0').replace("0", " ").replace("1", "^")}")
    return diff
}

private fun Instruction.valid(i1: String, i2: String) =
    input1 == i1 && input2 == i2 || input1 == i2 && input2 == i1

private fun replace(
    newId: String,
    oldId: String,
    instructions: MutableMap<String, Instruction>,
    translationsTo: MutableMap<String, String>,
    translationsFrom: MutableMap<String, String>
) {
    if (newId in translationsTo) error("Already exists: $newId")
    translationsFrom[oldId] = newId
    translationsTo[newId] = oldId
    instructions[newId] = instructions.remove(oldId)!!
    instructions.forEach { (k, v) ->
        if (v.input1 == oldId) {
            val (input1, input2) = listOf(newId, v.input2).sorted()
            instructions[k] = v.copy(input1 = input1, input2 = input2)
        }
        if (v.input2 == oldId) {
            val (input1, input2) = listOf(v.input1, newId).sorted()
            instructions[k] = v.copy(input1 = input1, input2 = input2)
        }
    }
}

private fun graphviz(
    results: MutableMap<String, Boolean>,
    instructions: Map<String, Instruction>,
    bad: Set<String> = emptySet()
) = buildString {
    appendLine()
    appendLine("digraph G {")
    instructions.forEach { solve(it.key, results, instructions) }
    results.forEach { (k, v) ->
        val bit = if (v) "1" else "0"
        val label = "$k = $bit"
        val shape = when {
            k.startsWith('x') -> "invtriangle"
            k.startsWith('y') -> "invtrapezium"
            k.startsWith('z') -> "doubleoctagon"
            else -> "ellipse"
        }
        val maybeColor = if (k in bad) "color=\"red\"" else ""
        appendLine("  $k[label=\"$label\" shape=\"$shape\" $maybeColor];")
    }
    instructions.toSortedMap().forEach { (k, v) ->
        appendLine("  op$k[label=\"${v.op}\" shape=\"none\"];")
        appendLine("  ${v.input1} -> op$k;")
        appendLine("  ${v.input2} -> op$k;")
        appendLine("  op$k -> $k;")
    }
    appendLine("}")
}

private fun <K, V> MutableMap<K, V>.swap(
    s1: K,
    s2: K
): MutableMap<K, V> {
    val swap = this[s1]!!
    this[s1] = this[s2]!!
    this[s2] = swap
    return this
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
    go(45213383376616, desc = "Part 1: ") { part1(input) }
    go("", desc = "Part 2: ") { part2(input) }
    TODO()
    measure(text, parse = ::parse, part1 = ::part1, part2 = ::part2)
}

val example1 = """
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
