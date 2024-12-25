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
    val name: String
    fun unwind(instructions: Map<String, Instruction>): Operand
}

data class Signal(override val name: String) : Operand {
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

data class Gate(override val name: String, val op: String, val input1: Operand, val input2: Operand) : Operand {
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

data class Adder(
    val a: String, // A
    val b: String, // B
    var cin: String? = null, // Carry in
    val sum: String, // Sum
    var cout: String? = null, // Carry out

    var xor1: String? = null, // A xor B
    var and1: String? = null, // A and B
    var xor2: String? = null, // (A xor B) xor Cin = Sum
    var and2: String? = null, // (A xor B) and Cin
    var or1: String? = null, // (A and B) or ((A xor B) and Cin) = Carry out
)

fun part2(input: Input): Any {
    val instructions = input.instructions.toMutableMap()
    val reverse = instructions.map { (k, v) -> v to k }.toMap()
    val gates = instructions.keys.toMutableSet()

    val swaps: List<Pair<String, String>> = listOf(
        "cnk" to "qwf",
        "z14" to "vhm",
        "z27" to "mps",
        "z39" to "msq",
    )

    val propagation = buildList {
        repeat(45) { i ->
            val id = i.toString().padStart(2, '0')
            add("x$id")
            add("y$id")
            add("z$id")
        }
        add("z45")
        val toGo = instructions.filterKeys { !it.startsWith('z') }.toList().toMutableList()
//        while (toGo.isNotEmpty()) {
//
//        }
    }
    propagation.forEach { println(it) }

    val adders = (0..44).map { i ->
        val id = i.toString().padStart(2, '0')
        Adder("x$id", "y$id", sum = "z$id")
    }
    adders.last().cout = "z45"
    adders.forEachIndexed { i, adder ->
        reverse[Instruction(adder.a, "XOR", adder.b)]?.let { xor1 ->
            adder.xor1 = xor1
            gates -= xor1
        }
        reverse[Instruction(adder.a, "AND", adder.b)]?.let { and1 ->
            adder.and1 = and1
            gates -= and1
            if (i == 0) adder.cout = and1
        }
    }

//    check(gates.isEmpty()) { "gates left: $gates" }

    swaps.forEach { (a, b) -> instructions.swap(a, b) }

//    val adders: MutableList<Operand> = (0..45).map { i ->
//        val id = i.toString().padStart(2, '0')
//        val z = "z$id"
//        Signal(z)
//    }.toMutableList()
//    adders.forEachIndexed { k, v -> adders[k] = v.unwind(instructions) }
//    adders.forEachIndexed { k, v -> adders[k] = v.unwind(instructions) }
//    adders.forEachIndexed { k, v -> adders[k] = v.unwind(instructions) }
//    adders.forEachIndexed { k, v -> adders[k] = v.unwind(instructions) }
//    adders.forEachIndexed { k, v -> println(v) }


    val results = input.initial.toMutableMap()


    val bad = swaps.flatMap { it.toList() }.toSet()
    graphviz(results, instructions, bad).let {
        Files.writeString(Paths.get("local/day24.dot"), it)
        ProcessBuilder().command("dot", "-Tpng", "-o", "local/day24.png", "local/day24.dot").start().waitFor()
    }

    val diff = diffs(results, instructions)
    if (diff == 0L) {
        return swaps.flatMap { it.toList() }.sorted().joinToString(",")
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
    appendLine("digraph G {")
    instructions.forEach { solve(it.key, results, instructions) }
    val r = results.keys.toMutableSet()
    fun appendNode(k: String) {
        if (k !in r) return
        //        val bit = if (v) "1" else "0"
//        val label = "$k = $bit"
        val shape = when {
            k.startsWith('x') -> "invtriangle"
            k.startsWith('y') -> "invtrapezium"
            k.startsWith('z') -> "doubleoctagon"
            else -> "ellipse"
        }
        val maybeColor = if (k in bad) "color=\"red\"" else ""
        appendLine("  $k[shape=\"$shape\" $maybeColor];")
    }

    fun appendGate(gate: Gate) {
        appendNode(gate.input1.name)
        r.remove(gate.input1.name)
        appendNode(gate.input2.name)
        r.remove(gate.input2.name)
    }
    repeat(45) {
        val n = "x${it.toString().padStart(2, '0')}"
        appendNode(n)
        r.remove(n)
    }
    repeat(45) {
        val n = "y${it.toString().padStart(2, '0')}"
        appendNode(n)
        r.remove(n)
    }
    repeat(46) {
        val n = "z${it.toString().padStart(2, '0')}"
        appendNode(n)
        r.remove(n)
    }
    repeat(46) { i ->
        val id = i.toString().padStart(2, '0')
        val z = "z$id"
        val op = Signal(z).unwind(instructions).unwind(instructions).unwind(instructions)
        appendNode(z)
        r.remove(z)
        if (op is Gate) {
            appendGate(op)
            if (op.input1 is Gate) appendGate(op.input1)
            if (op.input2 is Gate) appendGate(op.input2)
        }
    }
    r.forEach { k -> appendNode(k) }
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
