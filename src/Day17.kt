import java.io.File
import java.lang.IllegalStateException
import java.time.Year
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

 /*
 for input5-
 Result: 28246
 Result 2: 23107
 */
private const val CLAY = "#"
private const val SAND = "."
private const val WATER = "+"
private const val FALLING_WATER = "|"
private const val STILL_WATER = "~"
val WATER_SOURCE = 500 to 0
var waterCount = AtomicInteger(0)
var stillWaterCount = AtomicInteger(0)
fun main() {
    val xRange = Regex("y=(\\d+), x=(\\d+)..(\\d+)")
    val yRange = Regex("x=(\\d+), y=(\\d+)..(\\d+)")
    val map = List(10000) { MutableList(10000) { SAND } }
    var minY = Int.MAX_VALUE
    var maxY = 0
    map[500][0] = WATER
    File("src/input/day17-input.txt").forEachLine {
        when {
            it.matches(xRange) -> {
                val match = xRange.matchEntire(it)
                val (y, xMin, xMax) = match?.groupValues?.takeLast(3)?.map{n -> n.toInt()}.orEmpty()
                if (y < minY) { minY = y }
                if (y > maxY) { maxY = y }
                for (x in xMin..xMax) {
                    map[x+1][y] = CLAY
                }
            }
            it.matches(yRange) -> {
                val match = yRange.matchEntire(it)
                val (x, yMin, yMax) = match?.groupValues?.takeLast(3)?.map{n -> n.toInt()}.orEmpty()
                if (yMin < minY) { minY = yMin}
                if (yMax > maxY) { maxY = yMax}
                for (y in yMin..yMax) {
                    map[x+1][y] = CLAY
                }
            }
        }
    }
    print("Min Y:$minY")
    printMap(map)
    var nextWater = listOf(WATER_SOURCE)
    while (nextWater.isNotEmpty()) {
        nextWater = placeWaterIterative(map, minY, maxY, nextWater)
    }
    printMap(map)
    println("Water count:  $waterCount")
    val stillWaterCount = map.sumBy { column -> column.count { it == STILL_WATER } }
    println("Still water count:  $stillWaterCount")
}

fun isOnLedge(water: Pair<Int, Int>, map: List<List<String>>) : Boolean {
    if (water.first > 0 && map[water.first-1][water.second] == CLAY) {
        return true
    }
    if (water.first < 10000 && map[water.first+1][water.second] == CLAY) {
        return true
    }
    return false
}

fun isRowBeneathSpilling(water : Pair<Int, Int>, map: List<List<String>>) : Boolean {
    if (map[water.first][water.second + 1] == CLAY) return false
    var left = water.first-1
    while (left >= 0) {
        if (map[left][water.second + 1] == SAND) {
            return true
        }
        if (map[left][water.second+1] == CLAY) {
            break
        }
        left--
    }
    var right = water.first+1
    while (right < 10000) {
        if (map[right][water.second + 1] == SAND) {
            return true
        }
        if (map[right][water.second+1] == CLAY) {
            break
        }
        right++
    }
    return false
}

fun fillWaterToTheLeft(map: List<MutableList<String>>,
                       water: Pair<Int, Int>,
                       waterList: MutableList<Pair<Int,Int>>, minY: Int) : Pair<Boolean, Int> {
    var waterFallen = false
    var count = 0
    var left = water.first-1
    while (map[left][water.second] != CLAY) {
        if (map[left][water.second + 1] == SAND) {
            // If we have already left the edge, backtrack
            if (map[left+1][water.second+1] == FALLING_WATER) {
                left++
            }
            // Increment if not already water
            if (map[left][water.second] == SAND && water.second >= minY) {
                waterCount.incrementAndGet()
            }
            map[left][water.second] = FALLING_WATER
            waterFallen = true
            waterList.add(left to water.second)
            break
        } else {
            if (map[left][water.second] == SAND && water.second >= minY) {
                waterCount.incrementAndGet()
            }
            map[left][water.second] = FALLING_WATER
        }
        left--
    }
    //println("Count:  $count")
    return waterFallen to left
}

fun fillWaterToTheRight(map: List<MutableList<String>>,
                        water: Pair<Int, Int>,
                        waterList: MutableList<Pair<Int,Int>>, minY: Int) : Pair<Boolean, Int> {
    var waterFallen = false
    var right = water.first + 1
    var count = 0
    while (map[right][water.second] != CLAY) {
        if (map[right][water.second + 1] == SAND) {
            // If we have already left the edge, backtrack
            if (map[right-1][water.second+1] == FALLING_WATER) {
                right--
            }
            if (map[right][water.second] == SAND && water.second >= minY) {
                waterCount.incrementAndGet()
            }
            map[right][water.second] = FALLING_WATER
            waterFallen = true
            waterList.add(right to water.second)
            break
        } else {
            if (map[right][water.second] == SAND && water.second >= minY) {
                waterCount.incrementAndGet()
            }
            map[right][water.second] = FALLING_WATER
        }
        right++
    }
    return  waterFallen to right
}

fun makeWaterStill(map: List<MutableList<String>>, minX: Int, maxX: Int, y: Int) : Int {
    var count = 0
    for (i in minX..maxX) {
        map[i][y] = STILL_WATER
        count ++
    }
    return count
}

fun placeWaterIterative(map: List<MutableList<String>>, minY: Int, maxY: Int, waters: List<Pair<Int, Int>>) : List<Pair<Int, Int>> {
    val waterList = mutableListOf<Pair<Int,Int>>()
    if (waters.filterNot { it.second >= maxY}.isEmpty()) {
        println("End:  $waterCount")
        return waterList
    }

    waters.forEach { water ->
        //printMap(map)
        if (water.second >= maxY) {
            //println("End:  $waterCount")
            return@forEach
        }
        when {
            map[water.first][water.second + 1] == SAND -> {
                map[water.first][water.second + 1] = FALLING_WATER
                if (water.second+1 >= minY) {
                    waterCount.incrementAndGet()
                }
                waterList.add(water.first to water.second+1)
            } // end handle sand
            map[water.first][water.second + 1] in listOf(CLAY, STILL_WATER) -> {
                // If the row beneath this one is spilling off the map, don't build a second layer
                if (isRowBeneathSpilling(water.first to water.second, map)) {
                    return@forEach
                }
                // Fill this row with still water until it spills or fills
                if (map[water.first][water.second] == SAND && water.second >= minY) {
                    waterCount.incrementAndGet()
                }
                map[water.first][water.second] = FALLING_WATER
                val waterFallenLeft = fillWaterToTheLeft(map, water, waterList, minY)
                val waterFallenRight = fillWaterToTheRight(map, water, waterList, minY)
                // If the water has been filled in and can't escape, try filling the next layer
                if (!waterFallenLeft.first && !waterFallenRight.first) {
                    //val rowWater = waterFallenLeft.second + waterFallenRight.second + 1
                    val rowWater = makeWaterStill(map, waterFallenLeft.second+1, waterFallenRight.second-1, water.second)
                    println("Row water: $rowWater ${waterFallenLeft.second} + 1 + ${waterFallenRight.second}")
                    stillWaterCount.addAndGet(rowWater)
                    waterList.add(water.first to water.second - 1)
                }
            } // end handle clay, still water
        } // end when
    } // end water iteration

    return waterList
}

private fun printMap(map:  List<List<String>>) {
    val firstX = map.indexOfFirst{ it.contains(CLAY) || it.contains(STILL_WATER )|| it.contains(FALLING_WATER) } - 1
    val lastX = map.indexOfLast { it.contains(CLAY) || it.contains(STILL_WATER )|| it.contains(FALLING_WATER) } + 1
    var maxY = 0
    map.forEach { it ->
        val max = it.indexOfLast{ it.contains(CLAY) || it.contains(STILL_WATER )|| it.contains(FALLING_WATER) }
        if (max > maxY) maxY = max
    }
    for (y in 0..maxY) {
        print("%2d".format(y))
        for (x in firstX..lastX) {
            print(map[x][y])
        }
        println()
    }
    println()
}