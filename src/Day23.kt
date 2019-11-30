import java.io.File
import java.math.BigInteger
import java.util.*
import kotlin.system.measureTimeMillis


/*
    Recommended reading for part 2 -
    {@see https://www.gamedev.net/articles/programming/general-and-gameplay-programming/introduction-to-octrees-r3529}
    81396996 is the right answer
    PriorityQueues are in ascending order by default, not descending!  ....................................>:(
*/
val MIN_SIZE : BigInteger = BigInteger.ONE
private val ORIGIN = Position(
    BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO)
fun main() {
    val nanobots = mutableListOf<Nanobot>()
    val nanobotRegex = Regex("^pos=<(-?[0-9]+),(-?[0-9]+),(-?[0-9]+)>, r=([0-9]+)$")
    var min = BigInteger.ZERO
    var max = BigInteger.ZERO
    File("src/input/day23-input.txt").forEachLine {
        val nanobot = nanobotRegex.matchEntire(it)
        nanobot?.groupValues?.let { groups ->
            val (X, Y, Z, radius) = groups.takeLast(4).map { num -> num.toBigInteger() }
            val newMin = minOf(X-radius, minOf(Y-radius, Z-radius))
            if (newMin < min) min = newMin
            val newMax = maxOf(X+radius, maxOf(Y+radius, Z+radius))
            if (newMax > max) max = newMax
            nanobots.add(Nanobot(Position(X,Y,Z), radius))
        }
    }
    nanobots.sortDescending()
    val strongest = nanobots[0]
    println("Strongest is $strongest")
    val botsInRange = nanobots.filter { strongest.mayTransmit(it) }
    println("Bots in range of strong boi:  ${botsInRange.count()}")
    val edge = maxOf(min.abs(), max.abs())
    val boundingBox = BoundingBox(Position(-edge, -edge, -edge), Position(edge, edge, edge)).findNewBoundingBox()
    val root = OctNode(boundingBox, null, nanobots)
    val runtime = measureTimeMillis {
        val best = traverse(root)
        println("Best --- " + best?.boundingBox?.max)
        println("Manhattan distance is ${best?.boundingBox?.max?.manhattanDistance(ORIGIN)}")
    }
    println("Runtime was $runtime")
}

private fun traverse(root: OctNode) : OctNode? {
    var best : OctNode? = null

    val nodes = PriorityQueue<OctNode>()
    nodes.add(root)

    loop@ while (nodes.isNotEmpty()) {
        val node = nodes.poll()

        if (best != null && best.bots.size > node.bots.size) { continue@loop }

        if (node.isLeaf()) {
            if (best == null || node.bots.size > best.bots.size || node.distanceFrom(ORIGIN) < best.distanceFrom(ORIGIN)) {
                best = node
                println("New best...${node.boundingBox.min},${node.boundingBox.max}:${node.bots.size}")
            }
        } else {
            node.build()
            for (child in node.children) {
                nodes.add(child)
            }
        }
    }
    return best
}

private data class OctNode(val boundingBox: BoundingBox,
                           val parent: OctNode?,
                           val bots: MutableList<Nanobot>,
                           val children: MutableList<OctNode> = mutableListOf(),
                           var activeNodes : Int = 0) : Comparable<OctNode> {
    fun isLeaf() : Boolean {
        return boundingBox.dimensions.x == BigInteger.ONE
    }

    override fun compareTo(other: OctNode): Int {
        // 1- greater number of bots
        val botsNum = other.bots.size.compareTo(this.bots.size)
        if (botsNum != 0) return botsNum
        // 2- greater size
        val size = other.boundingBox.dimensions.x.compareTo(this.boundingBox.dimensions.x)
        if (size != 0) return size
        // 3- closest to origin
        return this.distanceFrom(ORIGIN).compareTo(other.distanceFrom(ORIGIN))
    }

    lateinit var distanceToOrigin: BigInteger
    fun distanceFrom(position: Position) : BigInteger {
        return this.boundingBox.clamp(position).manhattanDistance(position)
    }

    fun build() {
        //val buildTime = measureTimeMillis {
            // Find the bounding box
            val dimensions = boundingBox.dimensions

            if (dimensions.x < MIN_SIZE) return

            val half = dimensions / BigInteger.TWO
            val center = boundingBox.min + half

            val octants = buildOctants(boundingBox, center)
            val octantObjects = List<MutableList<Nanobot>>(8) { mutableListOf() }
            val removedObjects = mutableListOf<Nanobot>()

            // Add all the nanobots that are contained in this quadrant to the new children, and remove
            // them from this node's set of objects
            for (nanobot in bots) {
                if (nanobot.radius == BigInteger.ZERO) continue
                for (i in 0 until 8) {
                    if (nanobot.inRangeOf(octants[i])) {
                        octantObjects[i].add(nanobot)
                        removedObjects.add(nanobot)
                    }
                }
            }
            bots.removeAll(removedObjects)

            // Create the children
            for (i in 0 until 8) {
                if (octantObjects[i].isEmpty()) continue
                val potentialChild = createNode(octants[i], octantObjects[i])
                if (potentialChild != null) {
                    children.add(potentialChild)
                }
            }
//        }
//        if (buildTime > 0) {
//            println("Build time was $buildTime")
//        }
    }

    fun MutableMap<Nanobot, MutableList<Nanobot>>.contains(key: Nanobot, value: Nanobot) : Boolean {
        return this[key]?.contains(value) == true
    }

    /**
     * Divide our big boi cube into 8 smaller octants, via the X Y Z values of the min, max, and half positions
     */
    private fun buildOctants(boundingBox: BoundingBox, center: Position) : Array<BoundingBox> {
        return Array(8) { i ->
            when (i) {
                0 -> BoundingBox(boundingBox.min, center)
                1 -> BoundingBox(
                    Position(center.x, boundingBox.min.y, boundingBox.min.z),
                    Position(boundingBox.max.x, center.y, center.z))
                2 -> BoundingBox(
                    Position(center.x, boundingBox.min.y, center.z),
                    Position(boundingBox.max.x, center.y, boundingBox.max.z))
                3 -> BoundingBox(
                    Position(boundingBox.min.x, boundingBox.min.y, center.z),
                    Position(center.x, center.y, boundingBox.max.z))
                4 -> BoundingBox(
                    Position(boundingBox.min.x, center.y, boundingBox.min.z),
                    Position(center.x, boundingBox.max.y, center.z))
                5 -> BoundingBox(
                    Position(center.x, center.y, boundingBox.min.z),
                    Position(boundingBox.max.x, boundingBox.max.y, center.z))
                6 -> BoundingBox(center, boundingBox.max)
                7 -> BoundingBox(
                    Position(boundingBox.min.x, center.y, center.z),
                    Position(center.x, boundingBox.max.y, boundingBox.max.z))
                else -> throw Exception("This should not be possible.")
            }
        }
    }

    override fun toString() : String {
        return children.toString()
    }

    private fun createNode(boundingBox: BoundingBox, nanobots: MutableList<Nanobot>) : OctNode? {
        if (nanobots.isEmpty()) return null
        return OctNode(boundingBox, this, nanobots)
    }
}

private data class BoundingBox(var min: Position, var max: Position) {
    val dimensions = max - min
    val center = min + (dimensions/BigInteger.TWO)

    fun clamp(other: Position) : Position {
        var x = other.x
        var y = other.y
        var z = other.z
        if (other.x > max.x) {
            x = max.x
        } else if (other.x < min.x) {
            x = min.x
        }
        if (other.y > max.y) {
            y = max.y
        } else if (other.y < min.y) {
            y = min.y
        }
        if (other.z > max.z) {
            z = max.z
        } else if (other.z < min.z) {
            z = min.z
        }
        return Position(x, y, z)
    }

    fun findNewBoundingBox() : BoundingBox {
        val nextValue = getNewMax(max.largestCoordinate())
        return BoundingBox(
            Position(nextValue.negate(), nextValue.negate(), nextValue.negate()),
            Position(nextValue, nextValue, nextValue))
    }

    /**
     * Returns true if the nanobot is _fully_ contained by the bounding box
     */
    fun contains(nanobot: Nanobot) : Boolean {
        return when {
            //it is outside of the cube on the right side
            nanobot.position.x + nanobot.radius > max.x -> false
            //it is outside of the cube on the left side
            nanobot.position.x - nanobot.radius < min.x -> false
            //it is outside of the cube on the bottom side
            nanobot.position.y + nanobot.radius > max.y -> false
            //it is outside of the cube on the top side
            nanobot.position.y - nanobot.radius < min.y -> false
            //it is outside of the cube on the near side
            nanobot.position.z + nanobot.radius > max.z -> false
            //it is outside of the cube on the far side
            nanobot.position.z - nanobot.radius < min.z -> false
            else -> true
        }
    }
}

private fun getNewMax(highestValue : BigInteger) : BigInteger {
    return if (highestValue.isPowerOfTwo()) {
        println("is pow2 : $highestValue")
        highestValue
    } else {
        val nextPowerOfTwo = highestValue.nextPowerOfTwo()
        println("next power : $nextPowerOfTwo")
        nextPowerOfTwo
    }
}

private fun BigInteger.isPowerOfTwo() : Boolean {
    return bitCount() == 1
}

private fun BigInteger.nextPowerOfTwo() : BigInteger {
    return BigInteger.TWO.pow(bitLength())
}

private data class Nanobot(val position: Position, val radius: BigInteger) : Comparable<Nanobot> {
    override fun compareTo(other: Nanobot): Int {
        return radius.compareTo(other.radius)
    }

    fun mayTransmit(other: Nanobot): Boolean {
        return manhattanDistance(other) <= radius
    }

    fun inRangeOf(other: BoundingBox) : Boolean {
        val closestPoint = other.clamp(position)
        return position.manhattanDistance(closestPoint) <= radius
    }

    fun manhattanDistance(other: Nanobot) : BigInteger {
        return position.manhattanDistance(other.position)
    }
}

private data class Position(val x: BigInteger, val y: BigInteger, val z: BigInteger) {
    fun manhattanDistance(other: Position) : BigInteger {
        return (x - other.x).abs() + (y - other.y).abs() + (z - other.z).abs()
    }

    fun euclideanDistance(other: Position) : BigInteger {
        return ((x - other.x).pow(2) + (y - other.y).pow(2) + (z - other.z).pow(2)).sqrt()
    }

    fun translate(other: Position) : Position {
        return Position(this.x + other.x, this.y + other.y, this.z + other.z)
    }

    operator fun minus(other: Position) : Position {
        return Position(this.x - other.x, this.y -other.y, this.z -other.z)
    }

    operator fun minus(r: BigInteger) : Position {
        return Position(this.x - r, this.y -r, this.z - r)
    }

    operator fun plus(r: BigInteger) : Position {
        return Position(this.x + r, this.y +r, this.z + r)
    }

    fun largestCoordinate() : BigInteger {
        return maxOf(x, maxOf(y, z))
    }

    operator fun div(divisor: BigInteger) : Position {
        return Position(x / divisor, y / divisor, z / divisor)
    }

    operator fun plus(other: Position) : Position {
        return Position(this.x + other.x, this.y + other.y, this.z + other.z)
    }
}