package day24

import go
import measure
import readAllText

data class Gate(val op: String, val inputs: Set<String>) {
    val input1 = inputs.minOf { it }
    val input2 = inputs.maxOf { it }.also { require(it != input1) }
    override fun toString() = "$input1 $op $input2"
}

data class Input(val initial: Map<String, Boolean>, val gates: List<Pair<Gate, String>>)

fun solve(name: String, results: MutableMap<String, Boolean>, circuit: Map<String, Gate>): Boolean =
    results.getOrPut(name) {
        val gate = circuit[name]!!
        when (gate.op) {
            "AND" -> solve(gate.input1, results, circuit) and solve(gate.input2, results, circuit)
            "OR" -> solve(gate.input1, results, circuit) or solve(gate.input2, results, circuit)
            "XOR" -> solve(gate.input1, results, circuit) xor solve(gate.input2, results, circuit)
            else -> error("Unknown op: ${gate.op}")
        }
    }

//262651624 wrong
fun part1(input: Input): Long {
    val results = input.initial.toMutableMap()
    return count('z', results, input.gates.associate { (gate, out) -> out to gate })
}

private fun count(char: Char, results: MutableMap<String, Boolean>, map: Map<String, Gate>) =
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
    val allGates = input.gates.toMap().toMutableMap()
    val gatesByOutput = input.gates.associate { it.second to it.first }.toMutableMap()

    val swaps: List<Pair<String, String>> = listOf(
        "cnk" to "qwf",
        "z14" to "vhm",
        "z27" to "mps",
        "z39" to "msq",
    )

    val propagation = buildPropagation(input.gates)

    val groups = propagation.fold(mutableListOf<MutableList<String>>()) { acc, s ->
        if (s.startsWith("x")) acc.add(mutableListOf())
        acc.last().add(s)
        acc
    }.map { it.toList() }
    println(groups)

    val adders = groups.map { group ->
        val a = group[0]
        val b = group[1]
        val sum = group.last()
        check(a.startsWith('x'))
        check(b == a.replace('x', 'y'))
        check(sum == b.replace('y', 'z'))
        val adder = Adder(a, b, sum = sum)

        if (a != "x00") { // full adder
            adder.cin =
                group.flatMap { gatesByOutput[it]?.inputs ?: emptySet() }.distinct()
                    .single { it !in group }
        }
        group to adder
    }
    adders.zipWithNext().forEach { (prev, next) ->
        prev.second.cout = next.second.cin
    }
    adders.forEach { (group, adder) ->
        println(group.map { "$it=${gatesByOutput[it]}" })
        val gates = group.filter { it in gatesByOutput }
            .map { it to gatesByOutput[it]!! }
        val xor1 = gates.filter { (_, instr) -> instr.valid(adder.a, adder.b) && instr.op == "XOR" }
            .single().first
        val and1 = gates.filter { (_, instr) -> instr.valid(adder.a, adder.b) && instr.op == "AND" }
            .single().first
        adder.xor1 = xor1
        adder.and1 = and1
        if (adder.a != "x00") { // full adder
//            adder.xor2 = gates.filter { (_, instr) -> instr.valid(xor1, adder.cin!!) && instr.op == "XOR" }
//                .single().first
//            adder.and2 = gates.filter { (_, instr) -> instr.valid(xor1, adder.cin!!) && instr.op == "AND" }
//                .single().first

        }
        println(adder)
    }


    swaps.forEach { (a, b) -> gatesByOutput.swap(a, b) }

    val results = input.initial.toMutableMap()

    val diff = diffs(results, gatesByOutput)
    if (diff == 0L) {
        return swaps.flatMap { it.toList() }.sorted().joinToString(",")
    }

    error("Wrong")
}

private fun buildPropagation(gates: List<Pair<Gate, String>>) = buildList {
    repeat(45) { i ->
        val id = i.toString().padStart(2, '0')
        add("x$id")
        add("y$id")
        add("z$id")
    }
    val toGo = gates.filter { (gate, out) -> out !in this }.toMutableList()
    while (toGo.isNotEmpty()) {
        toGo.withIndex().filter { (_, pair) ->
            val (gate, out) = pair
            val i1 = this.indexOf(gate.input1)
            if (i1 >= 0) {
                val i2 = this.indexOf(gate.input2)
                if (i2 >= 0) {
                    add(i1.coerceAtLeast(i2) + 1, out)
                    true
                } else false
            } else false
        }.asReversed().forEach { (i) -> toGo.removeAt(i) }
    }
}

private fun diffs(
    results: MutableMap<String, Boolean>,
    gates: Map<String, Gate>
): Long {
    val x = count('x', results, gates)
    val y = count('y', results, gates)
    val z = count('z', results, gates)
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

private fun Gate.valid(i1: String, i2: String) =
    input1 == i1 && input2 == i2 || input1 == i2 && input2 == i1

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
    val gates = text.lineSequence().dropWhile { it.isNotEmpty() }.drop(1)
        .takeWhile { it.isNotEmpty() }
        .map {
            instrRegex.matchEntire(it)!!.destructured.let { (input1, op, input2, output) ->
                Gate(op, setOf(input1, input2)) to output
            }
        }
    return Input(initial, gates.toList())
}

fun main() {
    val text = readAllText("local/day24_input.txt")
    val input = parse(text)
    go(4) { part1(parse(example1)) }
    go(2024) { part1(parse(example2)) }
    go(45213383376616, desc = "Part 1: ") { part1(input) }
    go("cnk,mps,msq,qwf,vhm,z14,z27,z39", desc = "Part 2: ") { part2(input) }
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
