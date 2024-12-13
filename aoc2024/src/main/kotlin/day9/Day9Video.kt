package day9

import java.awt.BasicStroke
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Point
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.Timer

data class State(
    val line: String = "",
    val sectors: Input = emptyList(),
    val inTransfer: Pair<Chunk, Chunk>? = null,
    val progress: Int = 0,
)

fun main() {

    val image = BufferedImage(1000, 1000, BufferedImage.TYPE_INT_RGB)
    val line = "2333133121414131402"
    var state = State()
    var timer: Timer

    val panel = object : JPanel() {
        init {
            preferredSize = Dimension(600, 580)
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

    JFrame("Day 9").apply {
        location = Point(500,300)
        add(panel)
        pack()
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        isVisible = true
    }

    timer.start()

    Thread.sleep(4000)

    repeat(line.length) {
        val line1 = line.take(it + 1)
        state = state.copy(line = line1, sectors = parse(line1))
        Thread.sleep(180)
    }

    val sectors = state.sectors.map(Sector::toMutableSector)

    var nextFreeSector = 0
    sectors.asReversed().forEach { current ->
        current.chunks.asReversed().forEach { chunk ->
            while (chunk.size > 0) {
                while (sectors[nextFreeSector].free == 0) nextFreeSector++
                if (sectors[nextFreeSector].nextFree < chunk.start) {
                    val dest = sectors[nextFreeSector]
                    val toTransfer = chunk.size.coerceAtMost(dest.free)
                    val copy = chunk.copy(start = dest.nextFree, size = toTransfer, done = true)
                    val src = Chunk(chunk.id, chunk.start + chunk.size - toTransfer, toTransfer)
                    val dst = Chunk(copy.id, copy.start, toTransfer)
                    chunk.size -= copy.size
                    state = state.copy(
                        sectors = sectors.map { Sector(it.chunks.map { c -> Chunk(c.id, c.start, c.size) }, it.size) },
                        inTransfer = src to dst,
                        progress = 0
                    )
                    repeat(100) {
                        Thread.sleep(18)
                        state = state.copy(progress = it)
                    }
                    dest.add(copy)
                    state = state.copy(
                        sectors = sectors.map { Sector(it.chunks.map { c -> Chunk(c.id, c.start, c.size) }, it.size) },
                        inTransfer = null,
                        progress = 0
                    )
                } else break
            }
        }
    }
}

private val scale = 70
private var derivedFont: Font? = null
private val bgColor = Color(15, 33, 67)
private val fgColor = Color(139, 98, 18, 255)

fun drawState(image: BufferedImage, state: State) = image.createGraphics().let { g ->
    g.font = if (derivedFont != null) derivedFont else g.font.deriveFont(36f).also { derivedFont = it }
    g.translate(20, 20)
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    g.color = bgColor
    g.clearRect(0, 0, image.width, image.height)
    g.color = fgColor
    g.stroke = BasicStroke(5.0f)
    g.drawRect(-10, -10, scale * 8 + 20, scale * 8)
    g.color = fgColor.withAlpha(20)
    g.stroke = BasicStroke(2.0f)
    repeat(state.sectors.lastOrNull()?.let { it.chunks.first().start + it.size } ?: 0) {
        g.draw3DRect(it.x() - 45.pct(), it.y() - 45.pct(), 90.pct(), 90.pct(), false)
    }
    state.sectors.forEach { sector ->
        g.color = fgColor.withAlpha(200)
        val first = sector.chunks.first().start
        val last = first + sector.size - 1
        (first + 1..<last).forEach { i ->
            val x = i.x()
            val y = i.y()
            g.drawLine(x - 50.pct(), y - 48.pct(), x + 50.pct() - 1, y - 48.pct())
            g.drawLine(x - 50.pct(), y + 48.pct(), x + 50.pct() - 1, y + 48.pct())
        }
        first.let { i ->
            val x = i.x()
            val y = i.y()
            g.drawLine(x - 48.pct(), y - 48.pct(), x + 50.pct() - 1, y - 48.pct())
            g.drawLine(x - 48.pct(), y + 48.pct(), x + 50.pct() - 1, y + 48.pct())
            g.drawLine(x - 48.pct(), y - 48.pct(), x - 48.pct(), y + 48.pct())
        }
        last.let { i ->
            val x = i.x()
            val y = i.y()
            g.drawLine(x - 50.pct(), y - 48.pct(), x + 48.pct(), y - 48.pct())
            g.drawLine(x - 50.pct(), y + 48.pct(), x + 48.pct(), y + 48.pct())
            g.drawLine(x + 48.pct(), y - 48.pct(), x + 48.pct(), y + 48.pct())
        }
        sector.chunks.forEach { chunk ->
            repeat(chunk.size) {
                val i = it + chunk.start
                val x = i.x()
                val y = i.y()
                g.drawChunkPiece(x, y, chunk.id)
            }
        }
    }
    state.inTransfer?.let { (src, dst) ->
        repeat(src.size) {
            val xs = (src.start + it).x()
            val ys = (src.start + it).y()
            val xd = (dst.start + it).x()
            val yd = (dst.start + it).y()
            val x = xs + (xd - xs) * state.progress / 100
            val y = ys + (yd - ys) * state.progress / 100
            g.drawChunkPiece(x, y, src.id)
        }
    }
    g.color = fgColor.withAlpha(200)
    g.drawString(state.line, 80.pct(), 7 * scale)
}

private fun Graphics2D.drawChunkPiece(x: Int, y: Int, id: Int) {
    color = fgColor.shifted(id).withAlpha(100)
    fill3DRect(x - 47.pct(), y - 40.pct(), 94.pct(), 80.pct(), true)
    color = bgColor
    fillOval(x - 30.pct(), y - 30.pct(), 60.pct(), 60.pct())
    color = fgColor.shifted(id).withAlpha(250)
    fontMetrics.getStringBounds("$id", this).let {
        drawString(
            "$id",
            x - it.x.toInt() - it.width.toInt() / 2,
            y - it.y.toInt() - it.height.toInt() / 2
        )
    }
}

private fun Color.withAlpha(a: Int) = Color(red, green, blue, a)
private fun Color.shifted(id: Int): Color = Color.RGBtoHSB(red, green, blue, null)
    .let { (h, s, b) -> Color.getHSBColor(h + id * 0.1f + 0.5f, s, 1.0f - (1.0f - b) * 0.4f) }
    .withAlpha(alpha)

fun Int.x() = rem(8) * scale + scale / 2
fun Int.y() = div(8) * scale + scale / 2
fun Int.pct() = this * scale / 100
