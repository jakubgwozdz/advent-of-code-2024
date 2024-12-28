package day24

import display
import readAllText
import useGraphics
import withAlpha
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.geom.AffineTransform
import java.awt.geom.Arc2D
import java.awt.geom.Ellipse2D
import java.awt.geom.Line2D
import java.awt.geom.Path2D
import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D
import java.awt.image.BufferedImage
import java.lang.Thread.sleep
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.thread
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import kotlin.random.Random

data class AnimState(
    val adders: List<Adder> = emptyList(),
    val x: Long = Random.nextLong(),
    val y: Long = Random.nextLong(),
    val z: Long = Random.nextLong(),
    val position: Double = -5.0,
    val swap: List<String> = emptyList(),
    val swapProgress: Double = 0.0,
)

fun main() {
    val input = parse(readAllText("local/day24_input.txt"))

    val adders = setUpAdders(input)
//    val x = Random.nextBits(30).toLong() shl 15 or Random.nextBits(15).toLong()
//    val y = Random.nextBits(30).toLong() shl 15 or Random.nextBits(15).toLong()
//    val z = adders.calculate(x, y)
//    val diff = (x + y) xor z
//
//    println("id:       " + (45 downTo 0).joinToString("") { "${it / 10}" })
//    println("          " + (45 downTo 0).joinToString("") { "${it % 10}" })
//    println("x:         ${x.toString(2).padStart(45, '0')}")
//    println("y:         ${y.toString(2).padStart(45, '0')}")
//    println("(x+y):    ${(x + y).toString(2).padStart(46, '0')}")
//    println("z:        ${z.toString(2).padStart(46, '0')}")
//
//    println("diff:     ${diff.toString(2).padStart(46, '0').replace("0", " ").replace("1", "^")}")


    val anim = AtomicReference(AnimState(adders))

    display(anim, "Day 24: Crossed Wires", op = Day24Video()::paintOnImage)
    var sleep = 20.0

    thread {
        while (anim.get().position < 47) {
            anim.updateAndGet { state ->
                val current = state.adders.getOrNull(state.position.toInt())
                val swap = state.swap.takeIf { it.isNotEmpty() }
                    ?: current?.takeIf { it.cin != null }?.let { fixFullAdder(it) }
                    ?: emptyList()
                if (swap.isEmpty()) {
                    sleep -= 0.3
                    state.copy(position = state.position + 0.01)
//                    sleep = sleep - 0.03
                } else if (state.swapProgress < 1) {
                    sleep = 0.0
                    state.copy(swapProgress = state.swapProgress + 0.001, swap = swap)
                } else {
                    sleep = 200.0
                    state.copy(
                        adders = state.adders.map { adder ->
                            adder.takeIf { it != current } ?: with(current!!) {
                                copy(
                                    and1 = and1?.replaceOutput(swap),
                                    xor1 = xor1?.replaceOutput(swap),
                                    and2 = and2?.replaceOutput(swap),
                                    xor2 = xor2?.replaceOutput(swap),
                                    or1 = or1?.replaceOutput(swap),
                                )
                            }
                        },
                        z = state.adders.calculate(state.x, state.y),
                        swap = emptyList(),
                        swapProgress = 0.0,
                    )
                }
            }
            sleep(sleep.toLong().coerceAtLeast(2))
        }
    }
    thread {
        while (true) {
            anim.updateAndGet {
                val x = Random.nextBits(30).toLong() shl 15 or Random.nextBits(15).toLong()
                val y = Random.nextBits(30).toLong() shl 15 or Random.nextBits(15).toLong()
                it.copy(
                    x = x,
                    y = y,
                    z = it.adders.calculate(x, y),
                )
            }
            sleep(1000)
        }
    }
}

fun Pair<Gate, String>.replaceOutput(swap: List<String>) =
    if (second !in swap) this else first to swap.single { it != second }

class Day24Video {

    private var labelFont = Font("Source Code Pro", 0, 12)
    private var digitFont = Font("7-Segment", 0, 72)

    //https://coolors.co/443742-f3dfa2-03cea4-fb4d3d-285943
    val bgColor = Color(0x44, 0x37, 0x42, 0xc0)
    val correctColor = Color(0xF3, 0xDF, 0xA2)
    val fgColor = Color(0x03, 0xCE, 0xA4)
    val errorColor = Color(0xFB, 0x4D, 0x3D)
//    Color(0x28, 0x59, 0x43)

    val distance = 120.0

    fun paintOnImage(state: AnimState, image: BufferedImage) = image.useGraphics { g ->
        g.color = bgColor
        g.fillRect(0, 0, image.width, image.height)
        g.color = fgColor
        g.translate(400.0 + distance * state.position.coerceAtMost(41.7), 40.0)

        state.adders.indices.forEach { i ->
            if (i in (state.position.roundToInt() - 7..state.position.roundToInt() + 6)) {
                val adder = state.adders[i]
                val xBit = state.x shr i and 1L
                val yBit = state.y shr i and 1L
                val zBit = state.z shr i and 1L
                val cBit = state.z shr (i + 1) and 1L
                val error = zBit != (state.x + state.y) shr i and 1L
                g.drawAdder(adder, xBit, yBit, zBit, cBit, error, state.swap, state.swapProgress)
            }

            g.translate(-distance, 0.0)
        }
    }

    private fun Graphics2D.drawAdder(
        adder: Adder,
        xBit: Long,
        yBit: Long,
        zBit: Long,
        cBit: Long,
        error: Boolean,
        swap: List<String>,
        swapProgress: Double
    ) {
        val gates = mutableListOf<Pair<Gate, Point2D.Double>>()
        val outputs = mutableMapOf<String, Point2D.Double>()
        val inputs = mutableListOf<Pair<Set<String>, Point2D.Double>>()
        fun addGate(gate: Gate, out: String, row: Int, col: Int) {
            val x = 0.0 + col * 38
            val y = 230.0 + row * 60
            gates += gate to Point2D.Double(x, y)
            inputs += gate.inputs to Point2D.Double(x + 4, y)
            outputs[out] = Point2D.Double(x + 12, y + 40 + (2 - col) * 2)
        }
        adder.and1?.let { (gate, out) -> addGate(gate, out, 0, 0) }
        adder.xor1?.let { (gate, out) -> addGate(gate, out, 0, 2) }
        adder.and2?.let { (gate, out) -> addGate(gate, out, 1, 1) }
        adder.xor2?.let { (gate, out) -> addGate(gate, out, 1, 2) }
        adder.or1?.let { (gate, out) -> addGate(gate, out, 2, 0) }
        outputs[adder.a] = Point2D.Double(96.0, 190.0)
        outputs[adder.b] = Point2D.Double(80.0, 200.0)
        inputs += setOf(adder.sum) to Point2D.Double(88.0, 415.0)
        val cout = adder.cout
        when {
            cout == "z45" -> inputs += setOf(cout) to Point2D.Double(88.0 - distance.toDouble(), 415.0)
            cout != null -> inputs += setOf(cout) to Point2D.Double(-10.0, 280.0 - 0.2)
        }
        adder.cin?.let { outputs[it] = Point2D.Double(distance.toDouble() - 10.0, 280.0 - 0.2) }
        if (swap.isNotEmpty() && swap.all { it in outputs }) {
            val x0 = outputs[swap[0]]!!.x
            val x1 = outputs[swap[1]]!!.x
            val y0 = outputs[swap[0]]!!.y
            val y1 = outputs[swap[1]]!!.y
            outputs[swap[0]] =
                Point2D.Double(x0 + (x1 - x0) * swapProgress.toDouble(), y0 + (y1 - y0) * swapProgress.toDouble())
            outputs[swap[1]] =
                Point2D.Double(x1 - (x1 - x0) * swapProgress.toDouble(), y1 - (y1 - y0) * swapProgress.toDouble())
        }
        drawDigit(xBit, adder.a, 50.0, 0.0)
        drawDigit(yBit, adder.b, 50.0, 100.0)
        drawDigit(zBit, adder.sum, 50.0, 420.0, error)
        if (adder.cout == "z45") drawDigit(cBit, adder.cout!!, 50.0 - distance.toDouble(), 420.0)

        drawCircuit { // adder.a
            moveTo(80.0, 75.0)
            lineTo(80.0, 80.0)
            lineTo(96.0, 80.0)
            lineTo(96.0, 190.0)
        }
        drawCircuit { // adder.b
            moveTo(80.0, 175.0)
            lineTo(80.0, 180.0)
            lineTo(80.0, 200.0)
        }
        drawCircuit { // adder.sum
            moveTo(80.0, 418.0)
            lineTo(80.0, 415.0)
            lineTo(88.0, 415.0)
        }
        if (adder.cout == "z45") drawCircuit { // adder.cout
            moveTo(80.0 - distance.toDouble(), 418.0)
            lineTo(80.0 - distance.toDouble(), 415.0)
            lineTo(88.0 - distance.toDouble(), 415.0)
        }
        gates.forEach { (gate, pos) -> drawGate(gate, pos.x, pos.y) }
        inputs.flatMap { (names, pos) ->
            names.map { outputs[it]!! }
                .sortedBy { it.x }
                .mapIndexed { index, out -> Point2D.Double(pos.x + 16.0 * index, pos.y) to out }
        }.groupBy { it.second }.mapValues { (_, list) -> list.map { it.first } }
            .forEach { (to, froms) ->
                froms.forEach { from -> drawCircuit(from, to) }
                if (froms.size > 1) {
                    val xs = (froms.map { it.x } + to.x).sorted().drop(1).dropLast(1)
                    xs.forEach { x -> drawConnect(x, to.y) }
                }
            }
    }

    private fun Graphics2D.drawGate(gate: Gate, x: Double, y: Double) {
        color = fgColor
        val tr = AffineTransform.getTranslateInstance(x.toDouble(), y.toDouble())
        transform(tr)

        val gatePath = Path2D.Double()

        when (gate.op) {
            "AND" -> gatePath.andGate()
            "OR" -> gatePath.orGate()
            "XOR" -> gatePath.xorGate()
        }
        draw(gatePath)
        draw(Line2D.Double(4.0, 0.0, 4.0, 7.0))
        draw(Line2D.Double(20.0, 0.0, 20.0, 7.0))
        draw(Line2D.Double(12.0, 34.0, 12.0, if (x > 60) 40.0 else if (x > 30) 42.0 else 44.0))
        transform(tr.createInverse())
    }

    private fun Path2D.Double.orGate() {
        moveTo(0.0, 3.0)
        curveTo(4.0, 10.0, 20.0, 10.0, 24.0, 3.0)
        lineTo(24.0, 10.0)
        curveTo(24.0, 24.0, 24.0, 24.0, 12.0, 34.0)
        curveTo(0.0, 24.0, 0.0, 24.0, 0.0, 10.0)
        lineTo(0.0, 3.0)
    }

    private fun Path2D.Double.xorGate() {
        moveTo(0.0, 0.0)
        curveTo(4.0, 7.0, 20.0, 7.0, 24.0, 0.0)
        orGate()
    }

    private fun Path2D.Double.andGate() {
        moveTo(0.0, 7.0)
        lineTo(0.0, 10.0)
        append(Arc2D.Double(0.0, 10.0, 24.0, 24.0, 180.0, 180.0, Arc2D.OPEN), true)
        lineTo(24.0, 7.0)
        lineTo(0.0, 7.0)
    }

    private fun Graphics2D.drawCircuit(pathOp: Path2D.Double.() -> Unit) {
        val path = Path2D.Double().apply(pathOp)
        color = bgColor
        stroke = BasicStroke(3f)
        draw(path)
        color = fgColor
        stroke = BasicStroke(1f)
        draw(path)
    }

    private fun Graphics2D.drawConnect(x: Double, y: Double) {
        fill(Ellipse2D.Double(x - 3, y - 3, 6.0, 6.0))
    }

    private fun Graphics2D.drawCircuit(from: Point2D.Double, to: Point2D.Double) =
        drawCircuit {
            moveTo(from.x, from.y)
            val dy = to.y - from.y
            val dx = to.x - from.x
            val firstH = dy.absoluteValue < 10.1
            if (firstH) lineTo(to.x, from.y) else lineTo(from.x, to.y)
//            if (firstH)
            lineTo(to.x, to.y)
//            else curveTo(from.x + dx / 3, to.y - 10, to.x - dx / 3, to.y - 10, to.x, to.y)
        }

    fun Graphics2D.drawDigit(bit: Long, name: String, x: Double, y: Double, error: Boolean = false) {
        font = labelFont
        color = fgColor
        stroke = BasicStroke(1f)
        drawString(name, x.toFloat() + 2, y.toFloat() + 12)

        color = fgColor.withAlpha(80)
        draw(Rectangle2D.Double(x, y, 38.0, 73.0))
        font = digitFont
        draw(font.createGlyphVector(fontRenderContext, "8").getOutline(x.toFloat() + 1, y.toFloat() + 70))
        val c = if (error) errorColor else correctColor
        color = c.withAlpha(80)
        val outline =
            font.createGlyphVector(fontRenderContext, bit.toString()).getOutline(x.toFloat() + 1, y.toFloat() + 70)
        fill(outline)
        color = c
        draw(outline)


    }
}

fun List<Adder>.calculate(x: Long, y: Long): Long {
    val (c, sum) = foldIndexed(false to 0L) { i, (cin, sum), adder ->
        val xBit = x shr i and 1L != 0L
        val yBit = y shr i and 1L != 0L
        val (cout, s) = adder.calculate(xBit, yBit, cin)
        cout to if (s) 1L shl i or sum else sum
    }
    return if (c) 1L shl size or sum else sum
}

fun Adder.calculate(x: Boolean, y: Boolean, cin: Boolean): Pair<Boolean, Boolean> {
    val set = buildSet {
        if (x) add(a)
        if (y) add(b)
        if (cin) add(this@calculate.cin!!)
        var changed = true
        while (changed) {
            changed = false
            xor1?.let { (gate, out) -> if (gate.calculate(this)) add(out).also { changed = changed or it } }
            and1?.let { (gate, out) -> if (gate.calculate(this)) add(out).also { changed = changed or it } }
            xor2?.let { (gate, out) -> if (gate.calculate(this)) add(out).also { changed = changed or it } }
            and2?.let { (gate, out) -> if (gate.calculate(this)) add(out).also { changed = changed or it } }
            or1?.let { (gate, out) -> if (gate.calculate(this)) add(out).also { changed = changed or it } }
        }
    }
    return (cout in set) to (sum in set)
}
