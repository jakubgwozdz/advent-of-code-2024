package day12

import drawStringCentered
import shifted
import withAlpha
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Point
import java.awt.RenderingHints
import java.awt.geom.Line2D
import java.awt.geom.Point2D
import java.awt.image.BufferedImage
import java.lang.Thread.sleep
import java.util.concurrent.atomic.AtomicReference
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.Timer
import kotlin.math.pow
import kotlin.random.Random
import kotlin.random.nextInt


data class State(
    val input: Input,
    val regions: Collection<Region> = emptyList(),
    val step: Int = 0,
    val perimeters: Map<Region, Perimeter> = emptyMap(),
    val discounted: Set<Pair<List<Pos>, Dir>> = emptySet(),
)

fun State.next() = copy(step = step + 1)
fun State.addFence(region: Region, fence: Fence): State {
    val plantFences = perimeters.getOrDefault(region, emptySet())
    return copy(perimeters = perimeters.plus(region to plantFences + fence)).next()
}

fun State.joinFence(): State {
    val region = perimeters.filterValues { it.isNotEmpty() }.keys.random()
    val perimeter = perimeters.getValue(region)
    val random = perimeter.filterNot { (pos, dir) -> pos + dir.turnRight() to dir in perimeter }
        .random()
    val dir = random.second
    val joined = generateSequence(random.first) { pos ->
        val next = pos + dir.turnLeft()
        if (next to dir in perimeter) next else null
    }.toList()
    return copy(
        perimeters = perimeters.plus(region to perimeter - joined.map { it to dir }.toSet()),
        discounted = discounted + (joined to dir)
    ).next()
}

fun main() {
//    val text = readAllText("local/day12_input.txt")
    val delay = 50L
    val text = test3
    val state = AtomicReference(State(parse(text)))
    display(state)
    sleep(10000)
    state.updateAndGet { it.copy(regions = it.input.asRegions()) }

    repeat(100) {
        state.updateAndGet { prev -> prev.next() }
        sleep(delay)
    }

    val shuffled = state.get().regions.shuffled()
    shuffled.forEach { region ->
        region.perimeter().let {
            when (Random.nextInt(10)) {
                in 0..2 -> it.shuffled()
                in 3..5 -> it.reversed()
                else -> it
            }
        }.forEach { fence ->
            state.updateAndGet { prev -> prev.addFence(region, fence) }
            sleep(delay)
        }
    }

    while (state.get().perimeters.values.any { it.isNotEmpty() }) {
        state.updateAndGet { prev -> prev.joinFence() }
        sleep(delay)
    }

    println(state.get())

}

private fun display(state: AtomicReference<State>) {
    val image = BufferedImage(600, 580, BufferedImage.TYPE_INT_RGB).also {
        state.get().paintOn(it)
    }
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
    val pct: Int.() -> Float = { (this * scale / 100) }
    val random = MyRandom(scale)

    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    g.color = bgColor
    g.fillRect(0, 0, image.width, image.height)

    g.cameraFit(step, image.width, image.height)

    g.translate(image.width / 2 - image.height / 2 + 10, 0 + 10)
    g.color = fgColor
    regions.forEach { region ->
        region.positions.forEach { pos ->
            val order = step - random.nextInt(0..50)
            if (order > 0) {
                val point = pos.scaled(scale)
                val size = 70f * (1f - 1f / order).pow(10)
                g.font = font.deriveFont(size * scale / 100)
                g.color = fgColor2.shifted(region.plant.code)
                g.drawStringCentered("${region.plant}", point)
            }
        }
    }

    random.reset(0)
    val distance = 0.09f * scale
    val inP: Float.() -> Float = { this + random.nextMiddle(-2..1) - distance }
    val inN: Float.() -> Float = { this - random.nextMiddle(-1..2) + distance }
    val outP: Float.() -> Float = { this + random.nextMiddle(-2..1) + distance }
    val outN: Float.() -> Float = { this - random.nextMiddle(-1..2) - distance }
    val midP: Float.() -> Float = { this + random.nextMiddle(-2..1) }
    val midN: Float.() -> Float = { this - random.nextMiddle(-1..2) }

    g.color = fgColor
    perimeters.values.forEach { p ->
        p.forEach { (pos, dir) ->
            random.reset(pos.hashCode())
            val (x, y) = pos.scaled(scale).let { it.x to it.y }
            val (p1, p2) = when (dir) {
                Dir.U -> (x.inN() to y.outP()) to (x.inP() to y.outP())
                Dir.L -> (x.outP() to y.inN()) to (x.outP() to y.inP())
                Dir.D -> (x.inN() to y.outN()) to (x.inP() to y.outN())
                Dir.R -> (x.outN() to y.inN()) to (x.outN() to y.inP())
            }
            g.draw(Line2D.Float(p1.first, p1.second, p2.first, p2.second))
        }
    }

    random.reset(0)
    g.color = fgColor3
    g.stroke = BasicStroke(2.5f)
    discounted.forEach { (list, dir) ->
        val first = list.first()
        val last = list.last()
        random.reset(first.hashCode())
        val (x1, y1) = first.scaled(scale).let { it.x to it.y }
        val (x2, y2) = last.scaled(scale).let { it.x to it.y }
        val (p1, p2) = when (dir) {
            Dir.U -> (x2.inN() to y2.outP()) to (x1.inP() to y1.outP())
            Dir.L -> (x1.outP() to y1.inN()) to (x2.outP() to y2.inP())
            Dir.D -> (x1.inN() to y1.outN()) to (x2.inP() to y2.outN())
            Dir.R -> (x2.outN() to y2.inN()) to (x1.outN() to y1.inP())
        }
        g.draw(Line2D.Float(p1.first, p1.second, p2.first, p2.second))
    }
    g.dispose()
}

class MyRandom(val scale: Float) {
    fun nextInt(range: IntRange) = random.nextInt(range)

        fun nextMiddle(range: IntRange) = (50 + random.nextFloat() * range.count() + range.first) * scale / 100
//    fun nextMiddle(range: IntRange) = (50) * scale / 100
    fun reset(i: Int) {
        random = Random(i)
    }

    private var random = Random(0)
}


private fun Graphics2D.cameraFit(step: Int, width: Int, height: Int) {
    if (step in 0..100) {
        val sc = ((100.0 - step) / 100).pow(3.0)
        translate(width / 2, height / 2)
        scale(1.0 + sc, 1.0 + sc)
        rotate(sc)
        translate(width / -2, height / -2)
    }

}

fun Pos.scaled(scale: Float) = Point2D.Float((second + 0.5f) * scale, (first + 0.5f) * scale)

