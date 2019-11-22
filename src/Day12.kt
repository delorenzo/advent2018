import java.io.File
import kotlin.test.assertNotNull

const val PLANT = "#"
const val SOIL = "."

fun main() {
    val plantParser = Regex("^initial state:\\s*([#.]+)$")
    val ruleParser = Regex("([#.]+) => ([#.]+)")
    var iterations = 0
    var oldTotal = 0
    for (GENERATIONS in 0..2000) {
        var numNegatives = 5
        var plants = MutableList(5) { SOIL }
        val rules = mutableMapOf<String, String>()


        File("src/input/day12-input2.txt").forEachLine { line ->
            when {
                plantParser.matches(line) -> {
                    val match = plantParser.matchEntire(line)
                    assertNotNull(match)
                    val startingPlants = match.groupValues[1].split("")
                    plants.addAll(startingPlants.subList(1, startingPlants.size - 1))
                    plants.addAll(MutableList(3) { SOIL })
                }
                ruleParser.matches(line) -> {
                    val match = ruleParser.matchEntire(line)
                    assertNotNull(match)
                    rules[match.groupValues[1]] = match.groupValues[2]
                }
            }
        }

        for (x in 0 until GENERATIONS) {
            var firstPlantIndex = plants.indexOfFirst { it == PLANT } - 2
            if (firstPlantIndex - 3 < 0) {
                plants.add(0, SOIL)
                numNegatives += 1
            }
            val lastPlantIndex = plants.indexOfLast { it == PLANT }
            if (lastPlantIndex + 6 > plants.size) {
                plants.addAll(MutableList(3) { SOIL })
            }

            val newPlants = mutableListOf<String>().apply { addAll(plants) }
            for (p in 2 until plants.size - 2) {
                val plant = plants.slice(p - 2..p + 2)
                val plantString = plant.joinToString("")

                newPlants[p] = rules[plantString] ?: SOIL
            }
            plants = newPlants.toMutableList()

        }

        // Get total
        var total = 0
        plants.forEachIndexed { index, pot ->
            if (pot == PLANT) {
                total += (index - numNegatives)
            }
        }
        val difference = total - oldTotal
        oldTotal = total
        println("At $iterations generations:  $total ($difference)")
        if (iterations == 2000) {
            val answer = total + ((50000000000 - 2000)*53)
            println("Total at 5 billion generations:  $answer")
        }
        iterations++
    }
}