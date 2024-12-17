package day17

import display
import readAllText
import useGraphics
import withAlpha
import java.awt.AlphaComposite
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.Graphics2D
import java.awt.font.TextAttribute
import java.awt.geom.Point2D
import java.awt.image.BufferedImage
import java.lang.Thread.sleep
import java.util.concurrent.atomic.AtomicReference

data class AnimState(val a: Long, val output: List<Long>)

fun main() {

    val animState = AtomicReference(AnimState(0, emptyList()))

    val input = parse(readAllText("local/day17_input.txt"))
    val video = Day17Video(input)

    display(animState, "Day 17: Chronospatial Computer", dimension = Dimension(700, 640), op = video::paintOnImage)
    println("Press enter to start")
    readln()
    sleep(1000)
    var done = false
    val q = mutableListOf(0L to 0)
    while (q.isNotEmpty()) {
        val (base, i) = q.removeFirst()
        val offset = (input.program.size - i - 1) * 3
        repeat(8) { j ->
            val a = base and (7L shl offset).inv() or (j.toLong() shl offset)
            val output = runProgram(input.copy(a = a))
            if (!done) animState.set(AnimState(a, output))
            sleep(100)
            if (output.takeLast(i + 1) == input.program.takeLast(i + 1)) {
                if (output == input.program) done = true.also { q.clear() }
                else if (!done) q.add(a to i + 1)
            }
        }
    }
    // just in case
    val p2 = part2(input)
    val p2o = runProgram(input.copy(a = p2))

    animState.set(AnimState(p2, p2o))

}

class Day17Video(val input: Input) {
    private var font = Font("7-Segment", 0, 64)
        .deriveFont(mapOf(TextAttribute.TRACKING to 0.08))
    private var font2 = Font("Arial", 0, 24)
        .deriveFont(mapOf(TextAttribute.TRACKING to 0.08))

    private val bgColor = Color(15, 15, 35, 16)
    private val fgColor = Color(204, 204, 204)//.darker().darker().darker().withAlpha(16)
    private val fgColor2 = Color(0, 153, 0, 20)//.withAlpha(200)
    private val fgColor3 = Color(255, 255, 102, 200)
    private val fgColor3a = Color(255, 255, 102, 16)
    private val fgColor3b = Color.RED.withAlpha(80)

    fun paintOnImage(state: AnimState, image: BufferedImage) = image.useGraphics { g ->
        g.color = bgColor
        g.fillRect(0, 0, image.width, image.height)

        g.stroke = BasicStroke(2f)
        val oc = g.composite
        g.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f)
        counter(g, "Input", input.program.joinToString("."), Point2D.Float(30f, 65f))

        counter(g, "Register A", state.a.toString(), Point2D.Float(30f, 205f))
        counter(g, "Register A (base8)", state.a.toString(8), Point2D.Float(30f, 345f))

        counter(g, "Output", state.output.joinToString("."), Point2D.Float(30f, 485f))

        g.stroke = BasicStroke(1f)
        g.composite = oc
        counter(g, "Input", input.program.joinToString("."), Point2D.Float(30f, 65f))

        counter(g, "Register A", state.a.toString(), Point2D.Float(30f, 205f))
        counter(g, "Register A (base8)", state.a.toString(8), Point2D.Float(30f, 345f))

        counter(g, "Output", state.output.joinToString("."), Point2D.Float(30f, 485f))

    }

    private fun counter(g: Graphics2D, label: String, counter: String, pos: Point2D.Float) {

        label(g, label, pos)

        g.font = font
        val outline = g.font.createGlyphVector(g.fontRenderContext, counter).getOutline(pos.x, pos.y + 75)
        g.color = fgColor3a
        g.fill(outline)
        g.color = fgColor3
        g.draw(outline)

        val str8 = counter.replace("""\d""".toRegex(), "8")
        val outline2 = g.font.createGlyphVector(g.fontRenderContext, str8).getOutline(pos.x, pos.y + 75)
        g.color = fgColor2
        g.draw(outline2)
    }

    private fun label(g: Graphics2D, str: String, pos: Point2D.Float) {
        g.font = font2
        g.color = fgColor3
        g.drawString(str, pos.x, pos.y)

//        val outline = g.font.createGlyphVector(g.fontRenderContext, str).getOutline(pos.x, pos.y)
//        g.color = fgColor3a
//        g.fill(outline)
//        g.color = fgColor3
//        g.draw(outline)
    }
}

