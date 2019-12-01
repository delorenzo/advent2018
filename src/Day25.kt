import java.io.File
import java.math.BigInteger
import kotlin.math.absoluteValue

fun main() {
    // 387 is too low
    println(constellations("src/input/day25-input.txt"))
}


fun Array<Int>.manhattanDistance(other:  Array<Int>) : Int {
    return  (this[0] - other[0]).absoluteValue +
            (this[1] - other[1]).absoluteValue +
            (this[2] - other[2]).absoluteValue +
            (this[3] - other[3]).absoluteValue
}

fun constellations(filename: String) : Int {
    val stars = mutableListOf<Array<Int>>()
    val lineRegex = Regex("(-?[0-9]+),(-?[0-9]+),(-?[0-9]+),(-?[0-9]+)")
    File(filename).forEachLine { line ->
        val match = lineRegex.matchEntire(line)
        match?.let { result ->
            val (A, B, C, D) = result.groupValues.takeLast(4).map { num -> num.toInt() }
            stars.add(arrayOf(A, B, C, D))
        }
    }
    stars.sortWith( compareBy<Array<Int>> { it[0] }.thenBy { it[1] }.thenBy { it[2] }.thenBy { it[3] } )
    var constellations = mutableListOf<MutableList<Array<Int>>>()
    loop@ for (i in 0 until stars.size-1) {
        var added = false
        constellations.forEach { constellation ->
            // Combine it with an existing constellation
            if (constellation.any { it.manhattanDistance(stars[i]) <= 3}) {
                constellation.add(stars[i])
                added = true
                return@forEach
            }
        }
        // Start a new constellation
        if (!added) {
            constellations.add(mutableListOf(stars[i]))
        }
    }
    loop@ for (i in 0 until stars.size) {
        val dupes = constellations.partition { constellation ->
            constellation.any {
                    point -> point.manhattanDistance(stars[i]) <= 3 } }
        if (dupes.first.size <= 1) {
            continue
        } else {
            val newConstellation = mutableListOf<Array<Int>>()
            for (dupe in dupes.first) {
                newConstellation.addAll(dupe)
            }
            val newConstellations = dupes.second.toMutableList()
            newConstellations.add(newConstellation)
            constellations = newConstellations
        }
    }
    return constellations.size
}