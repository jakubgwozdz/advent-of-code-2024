package day18

import display
import readAllText
import useGraphics
import withAlpha
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics2D
import java.awt.geom.Ellipse2D
import java.awt.geom.Line2D
import java.awt.image.BufferedImage
import java.lang.Thread.sleep
import java.util.concurrent.atomic.AtomicReference
import kotlin.random.Random

data class AnimState(
    val left: Set<Pos> = emptySet(),
    val right: Set<Pos> = emptySet(),
    val other: Collection<Set<Pos>> = emptyList(),
    val last: Pos? = null,
    val done: Boolean = false
)

fun main() {
    val anim = AtomicReference(AnimState())
    val input = parse(readAllText("local/day18_input.txt"))
    val video = Day18Video(0..70, 0..70)

    display(anim, "Day 18: RAM Run", dimension = Dimension(600, 600), op = video::paintOnImage)

    repeat(10) {
        println(10-it)
        sleep(1000L)
    }

    val dropped = mutableSetOf<Pos>()

    val n = 73 // grid size + borders
    val uf = UnionFind<Pos>(n * n) { (r, c) -> (r + 1) * n + c + 1 }
    val leftBorder = Pos(-1, 1)
    val rightBorder = Pos(1, -1)

    fun drop(pos: Pos) {
        dropped += pos
        pos.neighborsPart2().forEach { if (it in dropped) uf.union(pos, it) }
        val grouped = dropped.groupBy { uf.find(it) }
        val leftIndex = uf.find(leftBorder)
        val rightIndex = uf.find(rightBorder)
        if (leftIndex != rightIndex) anim.set(
            AnimState(
                left = grouped[leftIndex]?.toSet() ?: emptySet(),
                right = grouped[rightIndex]?.toSet() ?: emptySet(),
                other = grouped.filterKeys { it != leftIndex && it != rightIndex }.values.map { it.toSet() },
                last = pos
            )
        ) else anim.updateAndGet { it.copy(last = pos, done = true) }
        sleep(5)
    }

    (0..70).forEach { i ->
        drop(Pos(-1, i + 1))
        drop(Pos(i + 1, -1))
    }

    (0..70).forEach { i ->
        drop(Pos(71, i - 1))
        drop(Pos(i - 1, 71))
    }

    fun isBlocked() = uf.find(leftBorder) == uf.find(rightBorder)

    input.asSequence().takeWhile { !isBlocked() }.forEach(::drop)

    println("FINISH")
}

class Day18Video(val rowRange: IntRange, val colRange: IntRange) {

    val bgColor = Color(0x443742).withAlpha(80)
    val leftColor = Color(0x03CEA4)
    val rightColor = Color(0xFB4D3D)
    val otherColor = Color(0x285943)
    val lastColor = Color(0xF3DFA2).withAlpha(50)

    fun paintOnImage(state: AnimState, image: BufferedImage) = image.useGraphics { g ->
        g.color = bgColor
        g.fillRect(0, 0, image.width, image.height)
        g.scale(
            image.width / (colRange.last - colRange.first + 4.0),
            image.width / (rowRange.last - rowRange.first + 4.0)
        )
        g.translate(2.0, 2.0)
        g.stroke = BasicStroke(0.1f)
        g.color = leftColor
        g.drawSet(state.left)
        g.color = rightColor
        g.drawSet(state.right)

        g.color = otherColor
        state.other.forEach { g.drawSet(it) }
        g.color = lastColor
        val blink = if (state.done) Random.nextDouble(0.2, 0.8) else 0.2
        repeat(3) { i ->
            state.last?.let { g.drawBrick(it, size = 0.2 + blink * (5 - i)) }
            state.last?.let { g.drawBrick(it) }
        }

        g.color = leftColor
        g.drawEnergized(state.left)
        g.color = rightColor
        g.drawEnergized(state.right)
        if (state.done) {
            g.color = leftColor
            g.drawEnergized(state.right)
            g.color = rightColor
            g.drawEnergized(state.left)
        }
    }

    private fun Graphics2D.drawBrick(pos: Pos, size: Double = 0.6) =
        fill(Ellipse2D.Double(pos.col - size / 2, pos.row - size / 2, size, size))


    private fun Graphics2D.drawSet(set: Set<Pos>) {
        set.forEach { drawBrick(it) }
    }

    private fun Graphics2D.drawEnergized(set: Set<Pos>) {
        set.forEach {
            val px = it.col + Random.nextDouble(-0.3, 0.3)
            val py = it.row + Random.nextDouble(-0.3, 0.3)
            it.neighborsPart2().filter { n -> n in set }.forEach { n ->
                val nx = n.col + Random.nextDouble(-0.3, 0.3)
                val ny = n.row + Random.nextDouble(-0.3, 0.3)
                draw(Line2D.Double(px, py, nx, ny))
            }
        }
    }

}
