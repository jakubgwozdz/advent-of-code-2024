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
import java.awt.geom.Rectangle2D
import java.awt.image.BufferedImage
import java.lang.Thread.sleep
import java.util.concurrent.atomic.AtomicReference
import kotlin.random.Random

data class AnimState(
    val adders: List<Adder> = emptyList(),
    val x: Long = Random.nextLong(),
    val y: Long = Random.nextLong(),
    val z: Long = Random.nextLong(),
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
        sleep(10000)
    }

}

class Day24Video() {

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
        g.translate(400.0, 40.0)

        state.adders.forEachIndexed { i, adder ->
            val xBit = state.x shr i and 1L
            val yBit = state.y shr i and 1L
            val zBit = state.z shr i and 1L
            val error = zBit != (state.x + state.y) shr i and 1L
            g.drawDigit(xBit, adder.a, 50f, 0f)
            g.drawDigit(yBit, adder.b, 50f, 100f)
            g.drawDigit(zBit, adder.sum, 50f, 400f, error)
            g.drawCircuit(Path2D.Float().apply {
                moveTo(80f, 72f)
                lineTo(80f, 80f)
                lineTo(96f, 80f)
                lineTo(96f, 230f)
            })
            g.drawCircuit(Path2D.Float().apply {
                moveTo(80f, 172f)
                lineTo(80f, 230f)
            })
            g.drawCircuit(Path2D.Float().apply {
                moveTo(20f, 230f)
                lineTo(20f, 190f)
                lineTo(96f, 190f)
            }, true)
            g.drawCircuit(Path2D.Float().apply {
                moveTo(4f, 230f)
                lineTo(4f, 200f)
                lineTo(80f, 200f)
            }, true)
            g.drawAndGate(0f, 230f)
            g.drawAndGate(76f, 230f)
            g.drawAndGate(38f, 290f)
            g.drawAndGate(76f, 290f)
            g.drawAndGate(0f, 350f)


            g.translate(-distance, 0.0)
        }
    }

    private fun Graphics2D.drawAndGate(x: Float, y: Float) {
        color = fgColor
        val tr = AffineTransform.getTranslateInstance(x.toDouble(), y.toDouble())
        transform(tr)
        draw(Path2D.Float().apply {
            moveTo(0f, 3f)
            lineTo(0f, 10f)
            append(Arc2D.Float(0f, 10f, 24f, 24f, 180f, 180f, Arc2D.OPEN), true)
            lineTo(24f, 3f)
            lineTo(0f, 3f)
        })
        draw(Line2D.Float(4f, 0f, 4f, 3f))
        draw(Line2D.Float(20f, 0f, 20f, 3f))
        draw(Line2D.Float(12f, 34f, 12f, 40f))
        transform(tr.createInverse())
    }

    private fun Graphics2D.drawCircuit(path: Path2D, connect: Boolean = false) {
        color = bgColor
        stroke = BasicStroke(3f)
        draw(path)
        color = fgColor
        stroke = BasicStroke(1f)
        draw(path)
        if (connect) {
            fill(Ellipse2D.Double(path.currentPoint.x - 5, path.currentPoint.y - 5, 10.0, 10.0))
        }
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
