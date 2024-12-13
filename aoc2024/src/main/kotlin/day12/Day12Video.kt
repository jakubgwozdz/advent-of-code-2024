package day12

import java.awt.BasicStroke
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Point
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.lang.Thread.sleep
import java.util.concurrent.atomic.AtomicReference
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.Timer
import kotlin.random.Random
import kotlin.random.nextInt


data class State(
    val input: Input,
    val regions: Collection<Region> = emptyList(),
    val wakeUpPercentage: Int = 0,
    val fences: Perimeter = emptySet(),
    val discounted: Set<Pair<Fence, Int>> = emptySet(),
)

fun main() {
//    val text = readAllText("local/day12_input.txt")
    val text = test3
    val state = AtomicReference(State(parse(text)))
    display(state)
//    state.updateAndGet {
//        val regions = it.input.asRegions()
//        it.copy(
//            regions = regions,
//            wakeUpPercentage = 100,
//            fences = regions.flatMap { it.perimeter() }.toSet()
//        )
//    }
//    sleep(10000)

//    state.set(State(parse(text)))

    state.updateAndGet { it.copy(regions = it.input.asRegions()) }

    repeat(100) {
        state.updateAndGet { prev -> prev.copy(wakeUpPercentage = it + 1) }
        sleep(50)
    }

    state.get().regions.shuffled().flatMap {
        it.perimeter().let {
            when (Random.nextInt(10)) {
                in 0..2 -> it.shuffled()
                in 3..5 -> it.reversed()
                else -> it
            }
        }
    }.forEach {
        state.updateAndGet { prev -> prev.copy(fences = prev.fences + it) }
        sleep(50)
    }

}

private fun display(state: AtomicReference<State>) {
    val image = BufferedImage(600, 580, BufferedImage.TYPE_INT_RGB)
    var timer: Timer
    val panel = object : JPanel() {
        init {
            preferredSize = Dimension(600, 580)
            size = preferredSize
            timer = Timer(10) {
                state.get().paintOn(image)
                repaint()
            }
        }

        override fun paintComponent(g: Graphics) {
            super.paintComponent(g)
            g.drawImage(image, 0, 0, null)
        }
    }

    JFrame("Day 12").apply {
        location = Point(500, 300)
        add(panel)
        pack()
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        isVisible = true
    }

    timer.start()
}

private var font = Font("Source Code Pro", 0, 24)
private val bgColor = Color(15, 15, 35)
private val fgColor = Color(204, 204, 204)
private val fgColor2 = Color(0, 153, 0).withAlpha(200)
private val fgColor3 = Color(255, 255, 102)

fun State.paintOn(image: BufferedImage) = image.createGraphics().let { g ->
    val scale = image.height.coerceAtMost(image.width).minus(20) / this.input.size.toFloat()
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    g.color = bgColor
    g.fillRect(0, 0, image.width, image.height)

    g.cameraFit(wakeUpPercentage, image.width, image.height)

    g.translate(image.width / 2 - image.height / 2 + 10, 0 + 10)
    g.color = fgColor
    var random = Random(0)
    regions.forEach { region ->
        val order = wakeUpPercentage - random.nextInt(0, 30)
        val size = order.coerceIn(0, 70)
        g.font = font.deriveFont(size * scale / 100)
        g.color = fgColor2.shifted(region.plant.code)
        region.positions.forEach { pos ->
            val (x, y) = pos.scaled(scale)
            g.drawStringCentered("${region.plant}", x, y)
        }
    }

    random = Random(0)
    val pct: Int.() -> Int = { (this * scale / 100).toInt() }
    val distance = 9
    val inP: Int.() -> Int = { this + (50 - distance + random.nextInt(-2..1)).pct() }
    val inN: Int.() -> Int = { this - (50 - distance + random.nextInt(-1..2)).pct() }
    val outP: Int.() -> Int = { this + (50 + distance + random.nextInt(-2..1)).pct() }
    val outN: Int.() -> Int = { this - (50 + distance + random.nextInt(-1..2)).pct() }
    val midP: Int.() -> Int = { this + (50 + random.nextInt(-2..1)).pct() }
    val midN: Int.() -> Int = { this - (50 + random.nextInt(-1..2)).pct() }

    g.color = fgColor
    fences.forEach { (pos, dir) ->
        val (x, y) = pos.scaled(scale)
        when (dir) {
            Dir.U -> g.drawLine(x.inN(), y.outP(), x.inP(), y.outP())
            Dir.L -> g.drawLine(x.outP(), y.inN(), x.outP(), y.inP())
            Dir.D -> g.drawLine(x.inN(), y.outN(), x.inP(), y.outN())
            Dir.R -> g.drawLine(x.outN(), y.inN(), x.outN(), y.inP())
        }
    }

    random = Random(0)
    g.color = fgColor3
    g.stroke = BasicStroke(1.5f)
    discounted.forEach { (fence, num) ->
        val (pos, dir) = fence
        val (x, y) = pos.scaled(scale)
        when (dir) {
            Dir.U -> g.drawLine(x.midN(), y.outP(), (x + num - 1).midP(), y.outP())
            Dir.L -> g.drawLine(x.outP(), y.midN(), x.outP(), y.midP())
            Dir.D -> g.drawLine(x.midN(), y.outN(), x.midP(), y.outN())
            Dir.R -> g.drawLine(x.outN(), y.midN(), x.outN(), y.midP())
        }
    }
}

private fun Graphics2D.cameraFit(percentage: Int, width: Int, height: Int) {
    translate(width / 2, height / 2)
    val sc = (100.0 - percentage) / 100
    scale(1.0 + sc * sc, 1.0 + sc * sc)
    val theta = (100 - percentage) / (40 * Math.PI)
    rotate(theta * theta)
    translate(width / -2, height / -2)
}

fun Pos.scaled(scale: Float) = ((second + 0.5f) * scale).toInt() to ((first + 0.5f) * scale).toInt()

fun Graphics2D.drawStringCentered(str: String, x: Int, y: Int) =
    fontMetrics.getStringBounds(str, this).let {
        drawString(
            str,
            x - it.x.toInt() - it.width.toInt() / 2,
            y - it.y.toInt() - it.height.toInt() / 2
        )
    }

fun Color.withAlpha(a: Int) = Color(red, green, blue, a)
fun Color.shifted(id: Int): Color = Color.RGBtoHSB(red, green, blue, null)
    .let { (h, s, b) -> Color.getHSBColor(h + id * 0.1f + 0.5f, s, 1.0f - (1.0f - b) * 0.4f) }
    .withAlpha(alpha)
