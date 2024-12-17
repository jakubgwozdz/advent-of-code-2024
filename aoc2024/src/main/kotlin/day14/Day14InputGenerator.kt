package day14

import java.awt.Color
import java.awt.Image
import java.awt.image.BufferedImage
import java.nio.file.Files
import java.nio.file.Path
import javax.imageio.ImageIO

fun main() {
    val path = "local/Kodee_Assets_Digital_Kodee-regular-156px-more.png"
    val result: Input = loadImage(path)
    val output= result.joinToString("\n") { (p, v) -> "p=${p.first},${p.second} v=${v.first},${v.second}" }
    Files.writeString(Path.of("local/day14_input_2.txt"), output)
}

fun loadImage(path: String): Input {
    val primes = listOf(
        2, 3, 5, 7, 11, 13, 17, 19, 23, 29,
    ).let { it + it.map(Int::unaryMinus) }.shuffled()
    val randomVelo = { Pos(primes.random(), primes.random()) }
    val image = ImageIO.read(Path.of(path).toFile())
    val i2 = BufferedImage(60, 60, BufferedImage.TYPE_INT_RGB)
    val gr = i2.graphics
    gr.drawImage(image.getScaledInstance(60, 60, Image.SCALE_SMOOTH), 0, 0, null)
    val result: Input = buildSet {
        repeat(60) { y ->
            repeat(60) { x ->
                val rgb = i2.getRGB(x, y)
                val brightness = Color(rgb).run { red * 0.299 + green * 0.587 + blue * 0.114 }
                if (brightness > 0.5) {
                    add(Pos(x + 20, y + 21) to randomVelo())
                }
                print(if (brightness < 0.5) '.' else '#')
            }
            println()
        }
        repeat(80) {
            add(Pos(it+10, 11) to randomVelo())
            add(Pos(it+10, 91) to randomVelo())
            add(Pos(10, it+11) to randomVelo())
            add(Pos(90, it+11) to randomVelo())
        }
    }.toList()
    return result.step(101 to 103, 9000)
}
