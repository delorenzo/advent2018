import java.io.File
import java.math.BigInteger

fun main() {
    val nanobots = mutableListOf<Nanobot>()
    val nanobotRegex = Regex("^pos=<(-?[0-9]+),(-?[0-9]+),(-?[0-9]+)>, r=([0-9]+)$")
    File("src/input/day23-input.txt").forEachLine {
        val nanobot = nanobotRegex.matchEntire(it)
        nanobot?.groupValues?.let { groups ->
            val (X, Y, Z, radius) = groups.takeLast(4).map { num -> num.toBigInteger() }
            nanobots.add(Nanobot(Position(X,Y,Z), radius))
        }
    }
    nanobots.sortDescending()
    val strongest = nanobots[0]
    println("Strongest is $strongest")
    //nanobots.map { println(" Distance from $it:  ${strongest.manhattanDistance(it)}") }
    val botsInRange = nanobots.filter { strongest.mayTransmit(it) }.count()
    println("Bots in range of strong boi:  $botsInRange")
}

private data class Nanobot(val position: Position, val radius: BigInteger) : Comparable<Nanobot> {
    override fun compareTo(other: Nanobot): Int {
        return radius.compareTo(other.radius)
    }

    fun mayTransmit(other: Nanobot): Boolean {
        return manhattanDistance(other) <= radius
    }

    fun manhattanDistance(other: Nanobot) : BigInteger {
        return position.manhattanDistance(other.position)
    }
}


private data class Position(val x: BigInteger, val y: BigInteger, val z: BigInteger) {
    fun manhattanDistance(other: Position) : BigInteger {
        return (x - other.x).abs() + (y - other.y).abs() + (z - other.z).abs()
    }
}