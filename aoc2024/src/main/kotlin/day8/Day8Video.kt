package day8

import readAllText
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.GraphicsEnvironment
import java.awt.Point
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.Timer
import kotlin.time.measureTime

data class State(
    val grid: Grid,
    val found: Set<Pos> = emptySet(),
    val current: Char? = null,
    val currentWave: List<Pos> = emptyList(),
    val currentPair: Pair<Pos, Pos>? = null,
)

fun main() {
    val screenshots = mutableListOf<State>()

    val grid = parse(readAllText("local/day8_input.txt"))
    screenshots += State(grid)

    grid.antennas.forEach { (ch, list) ->
        screenshots += screenshots.last().copy(current = ch)
        list.indices.forEach {
            screenshots += screenshots.last().copy(currentWave = list.slice(0..it))
        }
        list.asSequence().flatMap { p1 -> list.asSequence().map { p2 -> p1 to p2 } }
            .filter { (p1, p2) -> p1 != p2 }
            .forEach { pair ->
                screenshots += screenshots.last().copy(currentPair = pair)
                val (p1, p2) = pair
                val delta = p2 - p1
                var p = p1 + delta
                while (p in grid) {
                    screenshots += screenshots.last().let { it.copy(found = it.found + p) }
                    p += delta
                }
            }
        screenshots += screenshots.last().copy(current = null, currentWave = emptyList(), currentPair = null)
    }

    val found = screenshots.last().found.size
    check(found == 1417) { "expected 1417, got $found" }

    display(screenshots)
}

fun display(screenshots: List<State>) {

    val image = BufferedImage(1000, 1000, BufferedImage.TYPE_INT_ARGB)
    var state = screenshots.first()
    var timer: Timer

    val slideshow = object : JPanel() {
        init {
            preferredSize = Dimension(1000, 1000)
            size = preferredSize
            timer = Timer(10) {
                drawState(image, state)
                repaint()
            }
        }

        override fun paintComponent(g: Graphics) {
            super.paintComponent(g)
            g.drawImage(image, 0, 0, null)
        }
    }

    val frame = JFrame("Day 8 Part 2").apply {
        val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
        val screen = ge.screenDevices.maxBy { it.defaultConfiguration.bounds.height }
        location = screen.defaultConfiguration.bounds.location.let { Point(it.x + 50, it.y + 50) }
        add(slideshow)
        pack()
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        isVisible = true
    }

    timer.start()

    Thread.sleep(2000)
    var sleep = 400L
    screenshots.forEach {
        state = it
        sleep = sleep * 90 / 100 + 1
        Thread.sleep(sleep)
    }

    println(frame.bounds)
}

fun drawState(image: BufferedImage, state: State) = measureTime {
    image.createGraphics().run {
        setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        color = Color(200, 200, 200, 20)
        fillRect(0, 0, image.width, image.height)

        color = Color.GRAY
        state.currentPair?.let { (p1, p2) ->
            fillOval(p1.x() - 20, p1.y()-20,40,40)
            fillOval(p2.x() - 20, p2.y()-20,40,40)
//            drawString("${state.current}", p1.x() - 3, p1.y() + 5)
//            drawString("${state.current}", p2.x() - 3, p2.y() + 5)
        }


        color = Color(180, 180, 180, 40)
        state.grid.antennas.forEach { (ch, list) ->
            list.forEach {
                drawString("$ch", it.x() - 3, it.y() + 5)
            }
        }
        state.found.forEach {
            color = Color.GRAY
            drawString("#", it.x() - 3, it.y() + 5)
        }
        state.currentWave.forEach {
            color = Color(200, 200, 200)
            fillOval(it.x() - 10, it.y() - 10, 20, 20)
            color = Color.DARK_GRAY
            drawString("${state.current}", it.x() - 3, it.y() + 5)
        }
    }
}//.also { println(it) }

fun Pos.x() = second * 20 + 10 //+ Random.nextInt(-1, 2)
fun Pos.y() = first * 20 + 10 // + Random.nextInt(-1, 2)
