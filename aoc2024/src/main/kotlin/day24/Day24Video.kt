package day24

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
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

data class Pt(val x: Double, val y: Double)

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

        g.drawDigits(state.x.toString().padStart(14), 210.0, 540.0, false)
        g.drawDigits(state.y.toString().padStart(14), 210.0, 620.0, false)
        g.drawDigits("+", 160.0, 620.0, false)
        g.draw(Line2D.Double(190.0, 710.0, 710.0, 710.0))
        g.drawDigits(state.z.toString().padStart(14), 210.0, 715.0, state.z != state.x + state.y)


//        g.translate(600.0 + distance * state.position.coerceAtMost(41.7), 40.0)
//        g.translate(image.width / 2 + distance * state.position, 40.0)
        g.translate(image.width / 2.0, 40.0)
        g.scale(1 / state.zoom, 1 / state.zoom)
        g.translate(distance * state.position / state.zoom, 0.0)

        g.color = fgColor
        state.addersWithSwaps.indices.forEach { i ->
            val (adder, swap) = state.addersWithSwaps[i]
            val xBit = state.x shr i and 1L
            val yBit = state.y shr i and 1L
            val zBit = state.z shr i and 1L
            val cBit = state.z shr (i + 1) and 1L
            val error = zBit != (state.x + state.y) shr i and 1L
            g.drawAdder(
                adder, xBit, yBit, zBit, cBit, error,
//                if (state.adderToFix == adder) swap else emptyList(),
                swap,
                if (state.adderToFix == adder) state.swapProgress else -0.5
            )
//            }

            g.translate(-distance, 0.0)
        }
    }

    data class Wire(
        val name: String,
        var input: Pt,
        var output: Pt,
        var transitFrom: Pt? = null,
        var transitTo: Pt? = null,
    )

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
        val gates = mutableListOf<Pair<Gate, Pt>>()
        val outputs = mutableMapOf<String, Pt>()
        val inputs = mutableListOf<Pair<Set<String>, Pt>>()
        fun addGate(gate: Gate, out: String, row: Int, col: Int) {
            val x = 0.0 + col * 38 + if (row > 0) 8 else 0
            val y = 230.0 + row * 60
            gates += gate to Pt(x, y)
            inputs += gate.inputs to Pt(x + 4, y)
            val outputY = when (col) {
                0 -> y + 49
                1 -> y + 44
                else -> y + 40
            }
            outputs[out] = Pt(x + 12, outputY)
        }
        adder.and1?.let { (gate, out) -> addGate(gate, out, 0, 0) }
        adder.xor1?.let { (gate, out) -> addGate(gate, out, 0, 2) }
        adder.and2?.let { (gate, out) -> addGate(gate, out, 1, 1) }
        adder.xor2?.let { (gate, out) -> addGate(gate, out, 1, 2) }
        adder.or1?.let { (gate, out) -> addGate(gate, out, 2, 0) }
        outputs[adder.a] = Pt(96.0, 190.0)
        outputs[adder.b] = Pt(80.0, 200.0)
        val zx = if (adder.sum == "z00") 88.0 else 96.0
        inputs += setOf(adder.sum) to Pt(zx, 395.0)
        val cout = adder.cout
        when {
            cout == "z45" -> inputs += setOf(cout) to Pt(80.0 - distance, 415.0)
            cout != null -> inputs += setOf(cout) to Pt(-2.0, 280.0 - 0.2)
        }
        adder.cin?.let { outputs[it] = Pt(distance - 2.0, 280.0 - 0.2) }
        val wires = inputs.flatMap { (names, pos) ->
            names.map { it to outputs[it]!! }
                .sortedBy { (name, p) -> if (name in swap) outputs[swap.single { it != name }]!!.x else p.x }
                .mapIndexed { index, out ->
                    Wire(out.first, Pt(pos.x + 16.0 * index, pos.y), out.second)
                }
        }

        if (swap.any { it in outputs }) {
            val wiresAffected = wires.filter { it.name in swap }
//            val pointsAffected = wiresAffected.map { it.input }.distinct()
//                .also { check(it.size == 2) { "on swap $swap, wires affected: $wiresAffected, points $it" } }
            wiresAffected.forEach { wire ->
                wire.transitFrom = wire.output
                wire.transitTo = wiresAffected.first { it.name != wire.name }.output
            }

            if (swapProgress in (0.0..1.0)) {
                val changing = (swapProgress * wiresAffected.size).toInt().coerceAtMost(wiresAffected.size - 1)
                val progress = swapProgress * wiresAffected.size - changing

                val x0 = wiresAffected[changing].transitFrom!!.x
                val x1 = wiresAffected[changing].transitTo!!.x
                val y0 = wiresAffected[changing].transitFrom!!.y
                val y1 = wiresAffected[changing].transitTo!!.y

                val r = sqrt((x1 + x0).pow(2) / 4 + (y1 - y0).pow(2) / 4)
                val dy = r * sin(progress * Math.PI) / 3

                repeat(changing) { i ->
                    val wire = wiresAffected[i]
                    wire.output = wire.transitTo!!
                }
                val x = x0 + (x1 - x0) * progress - dy
                val y = y0 + (y1 - y0) * progress + dy
                wiresAffected[changing].output = Pt(x, y)
                wiresAffected[changing].input = wiresAffected[changing].input.let { Pt(it.x - dy, it.y - dy) }
            } else if (swapProgress > 1.0) {
                wiresAffected.forEach { wire ->
                    wire.output = wire.transitTo!!
                }
            }
        }
        drawDigit(xBit, adder.a, 50.0, 0.0)
        drawDigit(yBit, adder.b, 50.0, 100.0)
        drawDigit(zBit, adder.sum, 50.0, 420.0, error)
        if (adder.cout == "z45") drawDigit(cBit, adder.cout!!, 50.0 - distance, 420.0)

        drawCircuit { // adder.a part1
            moveTo(80.0, 75.0)
            lineTo(80.0, 80.0)
        }
        drawCircuit(Pt(96.0, 190.0), Pt(80.0, 80.0)) // adder.a part2
        drawCircuit { // adder.b
            moveTo(80.0, 175.0)
            lineTo(80.0, 180.0)
            lineTo(80.0, 200.0)
        }
        drawCircuit { // adder.sum
            moveTo(80.0, 418.0)
            lineTo(80.0, 415.0)
        }
        drawCircuit(Pt(zx, 390.0), Pt(80.0, 415.0)) // adder.sum part 2
        if (adder.cout == "z45") {
            drawCircuit { // adder.cout
                moveTo(80.0 - distance, 418.0)
                lineTo(80.0 - distance, 415.0)
            }
//            drawCircuit(
//                Pt(zx - distance, 395.0),
//                Pt(80.0 - distance, 415.0)
//            ) // adder.sum part 2
        }
        gates.forEach { (gate, pos) -> drawGate(gate, pos.x, pos.y) }
        wires.groupBy { it.name }.values
            .forEach { group ->
                group.forEach { (name, from, to) ->
                    val isError = name in swap
                    val errorIntensity = when {
                        isError && swapProgress < 0.0 -> swapProgress * 2 + 1.0
                        isError && swapProgress > 1.0 -> 1.0 - (swapProgress - 1.0) * 2
                        isError -> 1.0
                        else -> 0.0
                    }
                    drawCircuit(from, to, isError, errorIntensity)
                }
                if (group.size > 1) {
                    val to = group.first().output
                    val xs = (group.map { (_, from, _) -> from.x } + to.x).sorted().drop(1).dropLast(1)
                    xs.forEach { x -> drawConnect(x, to.y) }
                }
            }
    }

    private fun Graphics2D.drawGate(gate: Gate, x: Double, y: Double) {
        color = fgColor
        val tr = AffineTransform.getTranslateInstance(x, y)
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
        draw(Line2D.Double(12.0, 34.0, 12.0, if (x > 60) 40.0 else if (x > 30) 45.0 else 49.0))
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

    private fun Graphics2D.drawCircuit(
        isError: Boolean = false,
        errorIntensity: Double = 0.0,
        pathOp: Path2D.Double.() -> Unit
    ) {
        val path = Path2D.Double().apply(pathOp)
        color = if (isError) errorColor.withAlpha((200 * errorIntensity).toInt()) else bgColor
        stroke = BasicStroke(3f)
        draw(path)
        stroke = BasicStroke(1f)
        color = fgColor
        draw(path)
        if (isError) {
            color = errorColor.withAlpha((255 * errorIntensity).toInt())
            draw(path)
        }
    }

    private fun Graphics2D.drawConnect(x: Double, y: Double) {
        fill(Ellipse2D.Double(x - 3, y - 3, 6.0, 6.0))
    }

    private fun Graphics2D.drawCircuit(from: Pt, to: Pt, isError: Boolean = false, errorIntensity: Double = 0.0) =
        drawCircuit(isError, errorIntensity) {
            moveTo(from.x, from.y)
            val dy = to.y - from.y
            val dx = to.x - from.x
            val firstH = false
            val c = 0.25
            if (firstH) curveTo(
                from.x + (1 + c) * dx, from.y,
                from.x + dx, from.y - dy * c,
                from.x + dx, from.y + dy
            ) else curveTo(
                from.x, from.y + (1 + c) * dy,
                from.x - dx * c, from.y + dy,
                from.x + dx, from.y + dy
            )

            //            if (firstH)
//            else curveTo(from.x + dx / 3, to.y - 10, to.x - dx / 3, to.y - 10, to.x, to.y)
        }

    fun Graphics2D.drawDigit(bit: Long, name: String, x: Double, y: Double, error: Boolean = false) {
        font = labelFont
        color = fgColor
        stroke = BasicStroke(1f)
        drawString(name, x.toFloat() + 2, y.toFloat() + 12)
        color = fgColor.withAlpha(80)
        draw(Rectangle2D.Double(x, y, 38.0, 73.0))
        drawDigits(bit.toString(), x, y, error)
    }

    private fun Graphics2D.drawDigits(str: String, x: Double, y: Double, error: Boolean) {
        color = fgColor.withAlpha(80)
        stroke = BasicStroke(1f)
        font = digitFont
        if (str != "+") draw(
            font.createGlyphVector(fontRenderContext, str.map { '8' }.joinToString(""))
                .getOutline(x.toFloat() + 1, y.toFloat() + 70)
        )
        val c = if (error) (if (str.length == 1) errorColor else correctColor.withAlpha(40)) else correctColor
        color = c.withAlpha(c.alpha * 80 / 256)
        val outline =
            font.createGlyphVector(fontRenderContext, str).getOutline(x.toFloat() + 1, y.toFloat() + 70)
        fill(outline)
        color = c
        draw(outline)
        if (error && str.length > 1) {
            color = errorColor
            fill(
                font.createGlyphVector(fontRenderContext, "Error    ".padStart(str.length))
                    .getOutline(x.toFloat(), y.toFloat() + 70)
            )
        }
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
