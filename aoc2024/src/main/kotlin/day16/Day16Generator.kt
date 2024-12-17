package day16

import java.awt.Color
import java.awt.Image
import java.awt.image.BufferedImage
import java.nio.file.Path
import javax.imageio.ImageIO
import kotlin.random.Random

fun main() {
//    Path.of("local/day16_input2.txt").toFile().writeText(m2())
}

fun m2(): String {
    val path = "local/Kodee_Assets_Digital_Kodee-regular-156px-more.png"
    val result: Grid = loadImage(path)
    return result.joinToString("\n")
}

fun loadImage(path:String):Grid {
    val image = ImageIO.read(Path.of(path).toFile())
    val i2 = BufferedImage(141, 141, BufferedImage.TYPE_INT_RGB)
    val gr = i2.graphics
    gr.drawImage(image.getScaledInstance(141, 141, Image.SCALE_SMOOTH), 0, 0, null)
    fun isOn(r: Int, c: Int) = if (r !in 0..140 || c !in 0..140) false else
        i2.getRGB(c, r).let { rgb ->
        val brightness = Color(rgb).run { red * 0.299 + green * 0.587 + blue * 0.114 }
        brightness > 0.5
    }
    fun isOn2(r: Int, c: Int) : Boolean {
        val on = isOn(r, c)
        return on == isOn(r-1, c) && on == isOn(r+1, c) && on == isOn(r, c-1) && on == isOn(r, c+1)
    }
    return buildList {
        repeat(141) { r ->
            add(buildString {
                repeat(141) { c ->
                    append(when {
                        r == 139 && c == 1 -> 'S'
                        r == 1 && c == 139 -> 'E'
                        r == 0 || r == 140 || c == 0 || c == 140 -> '#'
                        c == 1 || c == 139 -> '.'
                        r % 2 == 0 && c % 2 == 0 -> '#'
                        r % 2 == 1 && c % 2 == 1 -> '.'
                        !isOn2(r , c ) -> '.'
                        !isOn2(r-1, c ) -> '#'
                        !isOn2(r+1, c ) -> '#'
                        !isOn2(r-2, c ) -> '#'
                        !isOn2(r+2, c ) -> '#'
                        !isOn2(r, c-1 ) -> '#'
                        !isOn2(r, c+1 ) -> '#'
                        !isOn2(r, c-2 ) -> '#'
                        !isOn2(r, c+2 ) -> '#'
                        Random.nextBoolean() -> '#'
                        else -> '.'
                    })
                }
            })
        }
    }
}
