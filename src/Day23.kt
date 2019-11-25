import java.io.File
import java.math.BigInteger
import kotlin.experimental.or

/*
    Recommended reading for part 2 -
    {@see https://www.gamedev.net/articles/programming/general-and-gameplay-programming/introduction-to-octrees-r3529}
*/
val MIN_SIZE : BigInteger = BigInteger.ONE
fun main() {
    val nanobots = mutableListOf<Nanobot>()
    val nanobotRegex = Regex("^pos=<(-?[0-9]+),(-?[0-9]+),(-?[0-9]+)>, r=([0-9]+)$")
    File("src/input/day23-input2.txt").forEachLine {
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
    val botsInRange = nanobots.filter { strongest.mayTransmit(it) }
    println("Bots in range of strong boi:  ${botsInRange.count()}")

    val radiusOffset = Position(strongest.radius, strongest.radius, strongest.radius)
    val nanobotMin = strongest.position - radiusOffset
    val nanobotMax = strongest.position + radiusOffset

    val boundingBox = BoundingBox(nanobotMin, nanobotMax)
    val octoTree = OctTree(boundingBox, null, botsInRange.toMutableList())

    println("Octo tree:")
    println(octoTree)
}


private fun getInstersection(point: Position, root: OctTree) : List<IntersectionRecord> {
    if (root.objects.isEmpty() || !root.children.any { it != null }) return emptyList()

    val result = mutableListOf<IntersectionRecord>()
//
//    result.addAll(root.objects.map { it.intersects(point) })
//
//    for (i in 0 until 8) {
//        root.children[i]?.let { child ->
//            if (point.contains(child.boundingBox) || point.intersects(child.boundingBox)) {
//                List<IntersectionRecord> intersectionList = child.getIntersection(point)
//                result.addAll(intersectionList)
//            }
//        }
//    }

    return result.toList()
}

private data class OctTree(var boundingBox: BoundingBox,
                           val parent: OctTree?,
                           val objects: MutableList<Nanobot>,
                           val children: MutableList<OctTree?> = MutableList(8) { null },
                           var activeNodes : Int = 0)  {
    init {
        build()
    }

    private fun build() {
        // Skip leaf nodes
        if (objects.size <= 1) return

        // Find the bounding box
        val dimensions = boundingBox.dimensions
        if (dimensions == Position.ZERO) {
            boundingBox = boundingBox.findNewBoundingBox()
        }

        // If the box is <1,1,1> or less, don't bother
        if (dimensions.largestCoordinate() <= MIN_SIZE) return

        val half = dimensions / BigInteger.TWO
        val center = boundingBox.min + half

        val octants = buildOctants(boundingBox, center)
        val octantObjects = List<MutableList<Nanobot>>(8) { mutableListOf() }
        val removedObjects = mutableListOf<Nanobot>()

        // Add all the nanobots that are contained in this quadrant to the new children, and remove
        // them from this node's set of objects
        for (nanobot in objects) {
            if (nanobot.radius == BigInteger.ZERO) continue
            for (i in 0 until 8) {
                if (octants[i].contains(nanobot)) {
                    octantObjects[i].add(nanobot)
                    removedObjects.add(nanobot)
                    break // can't be in 2 octants
                }
            }
        }
        objects.removeAll(removedObjects)

        // Create the children
        for (i in 0 until 8) {
            if (octantObjects[i].isEmpty()) continue
            children[i] = createNode(octants[i], octantObjects[i])
            activeNodes = activeNodes or (1 shl i)
        }

        println("done building")
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

    private fun createNode(boundingBox: BoundingBox, nanobots: MutableList<Nanobot>) : OctTree? {
        if (nanobots.isEmpty()) return null
        return OctTree(boundingBox, this, nanobots)
    }

    private fun createNode(boundingBox: BoundingBox, nanobot: Nanobot) : OctTree? {
        val nanobots = mutableListOf(nanobot)
        return OctTree(boundingBox, this, nanobots)
    }
}

private data class BoundingBox(var min: Position, var max: Position) {
    val dimensions = max - min

    fun findNewBoundingBox() : BoundingBox {
        //translate to origin
        val offset = Position.ZERO.minus(min)
        min += offset
        max += offset

        max = getNewMax(max.largestCoordinate())

        //translate back to where we were
        min -= offset
        min -= offset

        return BoundingBox(min, max)
    }

    /**
     * Returns true if the nanobot is _fully_ contained by the bounding box
     */
    fun contains(nanobot: Nanobot) : Boolean {
        val radiusOffset = Position(nanobot.radius, nanobot.radius, nanobot.radius)
        val nanobotMin = nanobot.position - radiusOffset
        val nanobotMax = nanobot.position + radiusOffset
        return  nanobotMin.x > min.x && nanobotMax.x < max.x &&
                nanobotMin.y > min.y && nanobotMax.y < max.y &&
                nanobotMin.z > min.z && nanobotMax.z < max.z
    }

    // https://gamedevelopment.tutsplus.com/tutorials/collision-detection-using-the-separating-axis-theorem--gamedev-169
    fun intersects(nanobot: Nanobot) : Boolean {
        return false
    }
}

private fun getNewMax(highestValue : BigInteger) : Position {
    return if (highestValue.isPowerOfTwo()) {
        Position(highestValue, highestValue, highestValue)
    } else {
        val nextPowerOfTwo = highestValue.nextPowerOfTwo()
        Position(nextPowerOfTwo, nextPowerOfTwo, nextPowerOfTwo)
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

    fun manhattanDistance(other: Nanobot) : BigInteger {
        return position.manhattanDistance(other.position)
    }
}

private data class Position(val x: BigInteger, val y: BigInteger, val z: BigInteger) {
    companion object {
        val ZERO = Position(BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO)
    }

    fun manhattanDistance(other: Position) : BigInteger {
        return (x - other.x).abs() + (y - other.y).abs() + (z - other.z).abs()
    }

    fun translate(other: Position) : Position {
        return Position(this.x + other.x, this.y + other.y, this.z + other.z)
    }

    operator fun minus(other: Position) : Position {
        return Position(this.x - other.x, this.y -other.y, this.z -other.z)
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

private data class IntersectionRecord(
    val position: Position,
    val normal: Position,
    val intersectedObject: Nanobot,
    val intersectedObject2: Nanobot,
    val intersectingRay: Ray,
    val distance: Double)

private data class Ray (val origin: Position, val direction: Position)