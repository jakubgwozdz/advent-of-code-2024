package day14

import display
import drawStringCentered
import readAllText
import useGraphics
import withAlpha
import withBrightness
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.geom.Ellipse2D
import java.awt.geom.Point2D
import java.awt.image.BufferedImage
import java.time.Duration.ofMillis
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.thread
import kotlin.math.absoluteValue
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random

typealias State = Long

fun main() {
    val input = parse(readAllText("local/day14_input.txt"))
    val state: AtomicReference<State> = AtomicReference(0L)

    display(state, "Day 14", Dimension(580 * 101 / 103, 580), op = Day14Painter(state.get(), input)::paintOn)
    val isClicked = AtomicReference(false)
    val t = thread {
        while (isClicked.get().not()) {
            state.updateAndGet { -Random.nextLong(36000000) }
            Thread.sleep(10)
        }
    }//.apply { start() }

    readln()
    isClicked.set(true)

    val end = 6516 * 1000L
    val duration = 26000L
    val start = System.currentTimeMillis()

    while (System.currentTimeMillis() - start < duration) {
        val elapsed = System.currentTimeMillis() - start
        val progress = elapsed * 2.0 / duration
        val factor = if (progress < 1) progress.pow(8) else 2 - (2 - progress).pow(8)
        val current = (end * factor / 2).toLong()
        state.set(current)
    }

    state.set(end)

}

class Day14Painter(private var prevState: State, val input: List<Pair<Pos, Pos>>) {
    private var font = Font("7-Segment", 0, 72)
    private val bgColor = Color(15, 15, 35, 100)
    private val fgColor = Color(204, 204, 204)
    private val fgColor2 = Color(0, 153, 0)//.withAlpha(200)
    private val fgColor3 = Color(255, 255, 102, 200)
    private val fgColor3a = Color(255, 255, 102, 100)
    private val fgColor3b = Color(255, 255, 102, 20)

    private val random = Random(0)
    private var counter = 0L

    fun paintOn(state: State, image: BufferedImage) = image.useGraphics { g ->
        val clockCenter = Point2D.Float(image.width / 2f, image.height - 100f)
        val scale = image.height / 103f
        g.color = bgColor
        g.fillRect(0, 0, image.width, image.height)

        g.font = font

        input.forEach { (p, v) ->
            val offset = random.nextFloat()
            val speed = random.nextFloat()
            val amplitude = random.nextFloat() / 2
            val brightness = amplitude * sin(offset + counter * speed / 100) + 0.5f
            g.color = fgColor2.withBrightness(brightness)
            val (px, py) = p
            val (vx, vy) = v

            val x = (px + vx * state.coerceAtLeast(0) / 1000.0).mod(101.0)
            val y = (py + vy * state.coerceAtLeast(0) / 1000.0).mod(103.0)

            val shape = Ellipse2D.Double(x * scale - 1, y * scale - 1, 3.0, 3.0)
            g.fill(shape)
        }

        g.drawStringCentered(state.time(), clockCenter) { s, x, y ->
            val outline = font.createGlyphVector(g.fontRenderContext, s).getOutline(x, y)
            g.color = fgColor3a
            g.fill(outline)
            g.color = fgColor3
            g.draw(outline)
        }

        g.drawStringCentered("8:88:88.8", clockCenter) { s, x, y ->
            val outline = font.createGlyphVector(g.fontRenderContext, s).getOutline(x, y)
            g.color = fgColor3b
            g.draw(outline)
        }

        prevState = state
        counter++
    }

    fun State.time(): String {
        val i = ofMillis(this.absoluteValue)
        return "%d:%02d:%02d.%d".format(i.toHoursPart(), i.toMinutesPart(), i.toSecondsPart(), i.toMillisPart() / 100)
    }

    operator fun Pos.times(l: Long) = Pos((first * l / 1000).toInt(), (second * l / 1000).toInt())

}
