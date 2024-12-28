package day24

import display
import linearInterpolation
import readAllText
import useGraphics
import withAlpha
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.Graphics2D
import java.awt.Point
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
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

enum class Stage { WAIT, ZOOMIN, SCROLL, FIX, ZOOMOUT, END }

private const val MAX_ZOOM = 7.5
private const val INIT_POS = 22.0 * MAX_ZOOM //2.9
private const val MIN_ZOOM_SPEED = 0.001
private const val LAST_POS = 47.0

data class AnimState(
    val adders: List<Adder> = emptyList(),
    val x: Long = Random.nextLong(),
    val y: Long = Random.nextLong(),
    val z: Long = Random.nextLong(),
//    val stage: Stage = Stage.WAIT,
    val swap: List<String> = emptyList(),
    val swapProgress: Double = 0.0,
    val position: Double = INIT_POS,
    val zoom: Double = MAX_ZOOM,
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

    display(
        anim,
        "Day 24: Crossed Wires",
        dimension = Dimension(800, 800),
        location = Point(500, 100),
        op = Day24Video()::paintOnImage
    )

    thread {
        val animSpeed = AtomicReference(0.0)
//        sleep(10000)
        println("Zooming in")
        var zooming = true
        animSpeed.set(MIN_ZOOM_SPEED)
        while (zooming) {
            anim.updateAndGet { state ->
                val zoom = (state.zoom - animSpeed.get()).coerceAtLeast(1.0).let { if (it < 1.0001) 1.0 else it }
                zooming = zoom > 1
                animSpeed.set((sin((zoom - 1) * PI / (MAX_ZOOM - 1)) * 0.01).coerceAtLeast(MIN_ZOOM_SPEED))
                state.copy(zoom = zoom, position = linearInterpolation(1.0, MAX_ZOOM, 0.0, INIT_POS, zoom))
            }
            sleep(8)
        }



        zooming = true
        animSpeed.set(MIN_ZOOM_SPEED)
        while (zooming) {
            anim.updateAndGet { state ->
                val zoom = (state.zoom + animSpeed.get()).coerceAtMost(MAX_ZOOM).let { if (it < 1.0001) 1.0 else it }
                zooming = zoom < MAX_ZOOM
                animSpeed.set((sin((zoom - 1) * PI / (MAX_ZOOM - 1)) * 0.01).coerceAtLeast(MIN_ZOOM_SPEED))
                state.copy(zoom = zoom, position = linearInterpolation(1.0, MAX_ZOOM, LAST_POS, INIT_POS, zoom))
            }
            sleep(8)
        }
        println("Zoomed out")
    }
    thread {
        while (true) {
            anim.updateAndGet {
                val x = Random.nextBits(30).toLong() shl 15 or Random.nextBits(15).toLong()
                val y = Random.nextBits(30).toLong() shl 15 or Random.nextBits(15).toLong()
                it.copy(x = x, y = y, z = it.adders.calculate(x, y))
            }
            sleep(1000)
        }
    }
}

private fun scroll(state: AnimState, animSpeed: AtomicReference<Double>): AnimState? {
    val current = state.adders.getOrNull(state.position.toInt())
    val swap = state.swap.takeIf { it.isNotEmpty() }
        ?: current?.takeIf { it.cin != null }?.let { fixFullAdder(it) }
        ?: emptyList()
    return if (swap.isEmpty()) {
        state.copy(position = state.position + 0.01)
    } else if (state.swapProgress < 1) {
        state.copy(swapProgress = state.swapProgress + 0.001, swap = swap)
    } else {
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
        state.adders.indices.forEach { i ->
//            if (i in (state.position.roundToInt() - 7..state.position.roundToInt() + 6)) {
            val adder = state.adders[i]
            val xBit = state.x shr i and 1L
            val yBit = state.y shr i and 1L
            val zBit = state.z shr i and 1L
            val cBit = state.z shr (i + 1) and 1L
            val error = zBit != (state.x + state.y) shr i and 1L
            g.drawAdder(adder, xBit, yBit, zBit, cBit, error, state.swap, state.swapProgress)
//            }

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
            val x = 0.0 + col * 38 + if (row > 0) 8 else 0
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
        val zx = if (adder.sum == "z00") 88.0 else 96.0
        inputs += setOf(adder.sum) to Point2D.Double(zx, 395.0)
        val cout = adder.cout
        when {
            cout == "z45" -> inputs += setOf(cout) to Point2D.Double(96.0 - distance, 395.0)
            cout != null -> inputs += setOf(cout) to Point2D.Double(-2.0, 280.0 - 0.2)
        }
        adder.cin?.let { outputs[it] = Point2D.Double(distance - 2.0, 280.0 - 0.2) }
        if (swap.isNotEmpty() && swap.all { it in outputs }) {
            val x0 = outputs[swap[0]]!!.x
            val x1 = outputs[swap[1]]!!.x
            val y0 = outputs[swap[0]]!!.y
            val y1 = outputs[swap[1]]!!.y
            val r = sqrt((x1 + x0).pow(2) / 4 + (y1 - y0).pow(2) / 4)
            val dy = r * sin(swapProgress * Math.PI * 2) / 3
            if (swapProgress < 0.5) {
                val x = x0 + (x1 - x0) * swapProgress * 2
                val y = y0 + (y1 - y0) * swapProgress * 2 + dy
                outputs[swap[0]] = Point2D.Double(x, y)
            } else {
                outputs[swap[0]] = Point2D.Double(x1, y1)
                val x = x1 - (x1 - x0) * (swapProgress - 0.5) * 2
                val y = y1 - (y1 - y0) * (swapProgress - 0.5) * 2 - dy
                outputs[swap[1]] = Point2D.Double(x, y)
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
        drawCircuit(Point2D.Double(96.0, 190.0), Point2D.Double(80.0, 80.0)) // adder.a part2
        drawCircuit { // adder.b
            moveTo(80.0, 175.0)
            lineTo(80.0, 180.0)
            lineTo(80.0, 200.0)
        }
        drawCircuit { // adder.sum
            moveTo(80.0, 418.0)
            lineTo(80.0, 415.0)
        }
        drawCircuit(Point2D.Double(zx, 390.0), Point2D.Double(80.0, 415.0)) // adder.sum part 2
        if (adder.cout == "z45") {
            drawCircuit { // adder.cout
                moveTo(80.0 - distance, 418.0)
                lineTo(80.0 - distance, 415.0)
            }
            drawCircuit(
                Point2D.Double(zx - distance, 395.0),
                Point2D.Double(80.0 - distance, 415.0)
            ) // adder.sum part 2
        }
        gates.forEach { (gate, pos) -> drawGate(gate, pos.x, pos.y) }
        inputs.flatMap { (names, pos) ->
            names.map { it to outputs[it]!! }
                .sortedBy { it.second.x }
                .mapIndexed { index, out -> Point2D.Double(pos.x + 16.0 * index, pos.y) to out }
        }.groupBy { it.second }.mapValues { (_, list) -> list.map { it.first } }
            .forEach { (to, froms) ->
                froms.forEach { from ->
                    drawCircuit(
                        from,
                        to.second,
                        to.first == swap.getOrNull(1) || to.first == swap.getOrNull(0) && swapProgress < 0.5
                    )
                }
                if (froms.size > 1) {
                    val xs = (froms.map { it.x } + to.second.x).sorted().drop(1).dropLast(1)
                    xs.forEach { x -> drawConnect(x, to.second.y) }
                }
                if (to.first in swap) {
                    drawConnect(to.second.x, to.second.y)
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

    private fun Graphics2D.drawCircuit(isError: Boolean = false, pathOp: Path2D.Double.() -> Unit) {
        val path = Path2D.Double().apply(pathOp)
        color = bgColor
        stroke = BasicStroke(3f)
        draw(path)
        color = if (isError) errorColor else fgColor
        stroke = BasicStroke(1f)
        draw(path)
    }

    private fun Graphics2D.drawConnect(x: Double, y: Double) {
        fill(Ellipse2D.Double(x - 3, y - 3, 6.0, 6.0))
    }

    private fun Graphics2D.drawCircuit(from: Point2D.Double, to: Point2D.Double, isError: Boolean = false) =
        drawCircuit(isError) {
            moveTo(from.x, from.y)
            val dy = to.y - from.y
            val dx = to.x - from.x
            val firstH = false
//            val firstH = dy.absoluteValue < 10.1
//            if (firstH) lineTo(to.x, from.y) else lineTo(from.x, to.y)
//            lineTo(to.x, to.y)
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
        val c = if (error) errorColor else correctColor
        color = c.withAlpha(80)
        val outline =
            font.createGlyphVector(fontRenderContext, str).getOutline(x.toFloat() + 1, y.toFloat() + 70)
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
