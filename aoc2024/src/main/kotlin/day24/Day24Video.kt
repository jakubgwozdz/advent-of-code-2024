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
    val position: Double = 0.0
)

fun main() {
    val input = parse(readAllText("local/day24_input.txt"))

    val adders = setUpAdders(input)
    val x = Random.nextBits(30).toLong() shl 15 or Random.nextBits(15).toLong()
    val y = Random.nextBits(30).toLong() shl 15 or Random.nextBits(15).toLong()
    val z = adders.calculate(x, y)
    val diff = (x + y) xor z

    println("id:       " + (45 downTo 0).joinToString("") { "${it / 10}" })
    println("          " + (45 downTo 0).joinToString("") { "${it % 10}" })
    println("x:         ${x.toString(2).padStart(45, '0')}")
    println("y:         ${y.toString(2).padStart(45, '0')}")
    println("(x+y):    ${(x + y).toString(2).padStart(46, '0')}")
    println("z:        ${z.toString(2).padStart(46, '0')}")

    println("diff:     ${diff.toString(2).padStart(46, '0').replace("0", " ").replace("1", "^")}")


    val anim = AtomicReference(AnimState(adders))

    display(anim, "Day 24: Crossed Wires", op = Day24Video()::paintOnImage)

    thread {
        while (true) {
            anim.updateAndGet {
                var position = it.position + 0.01
                if (position > 47) position = -4.0
                it.copy(position = position)
            }
            sleep(10)
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
                    z = adders.calculate(x, y),
                )
            }
            sleep(1000)
        }
    }
}

class Day24Video {

    private var labelFont = Font("Source Code Pro", 0, 12)
    private var digitFont = Font("7-Segment", 0, 72)

    //https://coolors.co/443742-f3dfa2-03cea4-fb4d3d-285943
    val bgColor = Color(0x44, 0x37, 0x42)
    val correctColor = Color(0xF3, 0xDF, 0xA2)
    val fgColor = Color(0x03, 0xCE, 0xA4)
    val errorColor = Color(0xFB, 0x4D, 0x3D)
//    Color(0x28, 0x59, 0x43)

    val distance = 120.0

    fun paintOnImage(state: AnimState, image: BufferedImage) = image.useGraphics { g ->
        g.color = bgColor
        g.fillRect(0, 0, image.width, image.height)
        g.color = fgColor
        g.translate(400.0 + distance * state.position, 40.0)

        state.adders.indices.forEach { i ->
            if (i in (state.position.roundToInt() - 3..state.position.roundToInt() + 6)) {
                val adder = state.adders[i]
                val xBit = state.x shr i and 1L
                val yBit = state.y shr i and 1L
                val zBit = state.z shr i and 1L
                val error = zBit != (state.x + state.y) shr i and 1L
                g.drawAdder(adder, xBit, yBit, zBit, error)
            }

            g.translate(-distance, 0.0)
        }
    }

    private fun Graphics2D.drawAdder(
        adder: Adder,
        xBit: Long,
        yBit: Long,
        zBit: Long,
        error: Boolean
    ) {
        val gates = mutableListOf<Pair<Gate, Point2D.Float>>()
        val outputs = mutableMapOf<String, Point2D.Float>()
        val inputs = mutableListOf<Pair<Set<String>, Point2D.Float>>()
        fun addGate(gate: Gate, out: String, x: Float, y: Float) {
            gates += gate to Point2D.Float(x, y)
            inputs += gate.inputs to Point2D.Float(x + 4, y)
            outputs[out] = Point2D.Float(x + 12, y + 40)
        }
        adder.and1?.let { (gate, out) -> addGate(gate, out, 0f, 230f) }
        adder.xor1?.let { (gate, out) -> addGate(gate, out, 76f, 230f) }
        adder.and2?.let { (gate, out) -> addGate(gate, out, 38f, 290f) }
        adder.xor2?.let { (gate, out) -> addGate(gate, out, 76f, 290f) }
        adder.or1?.let { (gate, out) -> addGate(gate, out, 0f, 350f) }
        outputs[adder.a] = Point2D.Float(96f, 190f)
        outputs[adder.b] = Point2D.Float(80f, 200f)
        inputs += setOf(adder.sum) to Point2D.Float(88f, 415f)
        inputs += setOf(adder.cout!!) to Point2D.Float(-10f, 280f - 0.2f)
        adder.cin?.let { outputs[it] = Point2D.Float(distance.toFloat() - 10f, 280f - 0.2f) }

        drawDigit(xBit, adder.a, 50f, 0f)
        drawDigit(yBit, adder.b, 50f, 100f)
        drawDigit(zBit, adder.sum, 50f, 420f, error)
        drawCircuit {
            moveTo(80f, 75f)
            lineTo(80f, 80f)
            lineTo(96f, 80f)
            lineTo(96f, 190f)
        }
        drawCircuit {
            moveTo(80f, 175f)
            lineTo(80f, 180f)
            lineTo(80f, 200f)
        }
        drawCircuit {
            moveTo(80f, 418f)
            lineTo(80f, 415f)
            lineTo(88f, 415f)
        }
//        drawCircuit(true) {
//            moveTo(20f, 230f)
//            lineTo(20f, 190f)
//            lineTo(96f, 190f)
//        }
//        drawCircuit(true) {
//            moveTo(4f, 230f)
//            lineTo(4f, 200f)
//            lineTo(80f, 200f)
//        }
        gates.forEach { (gate, pos) -> drawGate(gate, pos.x, pos.y) }
        inputs.forEach { (names, pos) ->
            names.map { outputs[it]!! }
                .sortedBy { it.x }.forEachIndexed { index, out ->
                    drawCircuit(Point2D.Float(pos.x + 16f * index, pos.y), out)
//            lineTo(pos.x, pos.y - 5)
//            lineTo(outputs[name]!!.x, pos.y - 5)
//                        lineTo(out.x, out.y)
                }
        }
    }

    private fun Graphics2D.drawGate(gate: Gate, x: Float, y: Float) {
        color = fgColor
        val tr = AffineTransform.getTranslateInstance(x.toDouble(), y.toDouble())
        transform(tr)

        val gatePath = Path2D.Float()

        when (gate.op) {
            "AND" -> gatePath.andGate()
            "OR" -> gatePath.orGate()
            "XOR" -> gatePath.xorGate()
        }
        draw(gatePath)
        draw(Line2D.Float(4f, 0f, 4f, 7f))
        draw(Line2D.Float(20f, 0f, 20f, 7f))
        draw(Line2D.Float(12f, 34f, 12f, 40f))
        transform(tr.createInverse())
    }

    private fun Path2D.Float.orGate() {
        moveTo(0f, 3f)
        curveTo(4f, 10f, 20f, 10f, 24f, 3f)
        lineTo(24f, 10f)
        curveTo(24f, 24f, 24f, 24f, 12f, 34f)
        curveTo(0f, 24f, 0f, 24f, 0f, 10f)
        lineTo(0f, 3f)
    }

    private fun Path2D.Float.xorGate() {
        moveTo(0f, 0f)
        curveTo(4f, 7f, 20f, 7f, 24f, 0f)
        orGate()
    }

    private fun Path2D.Float.andGate() {
        moveTo(0f, 7f)
        lineTo(0f, 10f)
        append(Arc2D.Float(0f, 10f, 24f, 24f, 180f, 180f, Arc2D.OPEN), true)
        lineTo(24f, 7f)
        lineTo(0f, 7f)
    }

    private fun Graphics2D.drawCircuit(connect: Boolean = false, pathOp: Path2D.Float.() -> Unit) {
        val path = Path2D.Float().apply(pathOp)
        color = bgColor
        stroke = BasicStroke(3f)
        draw(path)
        color = fgColor
        stroke = BasicStroke(1f)
        draw(path)
        if (connect) {
            fill(Ellipse2D.Double(path.currentPoint.x - 3, path.currentPoint.y - 3, 6.0, 6.0))
        }
    }

    private fun Graphics2D.drawCircuit(from: Point2D.Float, to: Point2D.Float, connect: Boolean = false) =
        drawCircuit(connect) {
            moveTo(from.x, from.y)
            if ((from.y - to.y).absoluteValue < 10.1f) lineTo(to.x, from.y) else lineTo(from.x, to.y)
            lineTo(to.x, to.y)
        }

    fun Graphics2D.drawDigit(bit: Long, name: String, x: Float, y: Float, error: Boolean = false) {
        font = labelFont
        color = fgColor
        stroke = BasicStroke(1f)
        drawString(name, x + 2, y + 12)

        color = fgColor.withAlpha(80)
        draw(Rectangle2D.Float(x, y, 38f, 73f))
        font = digitFont
        draw(font.createGlyphVector(fontRenderContext, "8").getOutline(x + 1, y + 70))
        val c = if (error) errorColor else correctColor
        color = c.withAlpha(80)
        val outline = font.createGlyphVector(fontRenderContext, bit.toString()).getOutline(x + 1, y + 70)
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