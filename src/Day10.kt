import java.io.File
import java.io.PrintWriter
import kotlin.test.assertNotNull

const val STAR = "#"
const val SKY = "."

fun main(args: Array<String>) {
    val positions = mutableListOf<MutableList<Int>>()
    val velocities = mutableListOf<MutableList<Int>>()
    val positionMap = mutableMapOf<Int, MutableList<Int>>()
    val min = MutableList(2) {0}
    val max = MutableList(2) {0}

    val regex = Regex("position=< *(-?[0-9]+), *(-?[0-9]+)> velocity=< *(-?[0-9]+), *(-?[0-9]+)>")
    File("src/input/day10-input2.txt").forEachLine {
        val match = regex.matchEntire(it)
        assertNotNull(match, it)
        val position = mutableListOf(match.groupValues[1].toInt(), match.groupValues[2].toInt())
        updateMinMax(position, min, max)
        positions.add(position)
        velocities.add(mutableListOf(match.groupValues[3].toInt(), match.groupValues[4].toInt()))
        positionMap[position[0]]?.add(position[1])
        positionMap.putIfAbsent(position[0], mutableListOf(position[1]))
    }
    File("src/output/day10-output.txt").printWriter().use { out ->
        for (i in 0..100000) {
            if (i > 0) {
                val shouldPrint = updatePositions(positions, velocities, min, max, positionMap)
                if (shouldPrint) {
                    out.println("After $i seconds....")
                    printStars(positionMap, min, max, out)
                }
            }
            positionMap.clear()
            min.fill(0)
            max.fill(0)
        }
    }
    println("Done")
}

fun printStars(positionMap: Map<Int, List<Int>>, min: MutableList<Int>, max: MutableList<Int>, out: PrintWriter){
    for (y in min[1]..max[1]) {
        for (x in min[0]..max[0]) {
            if (positionMap[x]?.contains(y) == true) out.print(STAR) else out.print(SKY)
        }
        out.println()
    }
}

fun updatePositions(positions: MutableList<MutableList<Int>>, velocities: MutableList<MutableList<Int>>,
                    min: MutableList<Int>, max: MutableList<Int>, positionMap: MutableMap<Int, MutableList<Int>>) : Boolean {
    var shouldPrint = false;
    for (position in positions.withIndex()) {
        position.value[0] += velocities[position.index][0]
        position.value[1] += velocities[position.index][1]

        updateMinMax(position.value, min, max)
        positionMap[position.value[0]]?.add(position.value[1])
        positionMap.putIfAbsent(position.value[0], mutableListOf(position.value[1]))

        if (position.index == 0) continue
        when {
            position.value[0] == positions[position.index - 1][0] -> {
                when {
                    Math.abs(position.value[1] - positions[position.index - 1][1]) == 1 -> shouldPrint = true
                }
            }
            position.value[1] == positions[position.index - 1][1] -> {
                when {
                    Math.abs(position.value[0] - positions[position.index - 1][0]) == 1 -> shouldPrint = true
                }
            }
        }
    }
    return shouldPrint;
}

fun updateMinMax(position: MutableList<Int>, min: MutableList<Int>, max: MutableList<Int>) {
    when {
        position[0] < min[0] -> min[0] = position[0]
        position[0] > max[0] -> max[0] = position[0]
        position[1] < min[1] -> min[1] = position[1]
        position[1] > max[1] -> max[1] = position[1]
    }
}