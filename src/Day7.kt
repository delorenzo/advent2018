import java.io.File
import kotlin.test.assertNotNull

const val LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"

fun main(args : Array<String>) {
    //part 1
    val dependencyMap = mutableMapOf<String, MutableSet<String>>()
    val regex = Regex("Step ([A-Z]) must be finished before step ([A-Z]) can begin.")
    File("src/input/day7-input2.txt").forEachLine{
       val match = regex.matchEntire(it)
        assertNotNull(match)
        val step1 = match.groupValues[1]
        val step2 = match.groupValues[2]
        dependencyMap[step2]?.add(step1)
        dependencyMap.putIfAbsent(step2, mutableSetOf(step1))
        dependencyMap.putIfAbsent(step1, mutableSetOf())
    }

    val stepOrder = mutableSetOf<String>()
    val stepMap = dependencyMap.toMutableMap()
    println(stepMap)
    println(dependencyMap)
    while (stepMap.isNotEmpty()) {
        val step = stepMap.filterValues{ it -> it.sumBy { if (stepOrder.contains(it))  0 else  1 } == 0}.toSortedMap().firstKey()
        stepOrder.add(step)
        stepMap.remove(step)
    }
    println(stepOrder.joinToString(""))

    println(dependencyMap)
    //part 2
    var seconds = 0
    val workers = 5
    val stepsRemaining = stepOrder.toMutableSet()
    val stepsCompleted = mutableSetOf<String>()
    val timeMap = mutableMapOf<String, Int>()
    val workerMap = mutableMapOf<String, Int>()
    var workingMap = mutableMapOf<Int, String>()
    stepOrder.forEach{
        timeMap[it] = LETTERS.indexOf(it) + 61
    }
    while (stepsRemaining.isNotEmpty()) {

        print("$seconds   ")
        //println(stepsCompleted)
        for (i in 1..workers) {
            val stepInProgress = workingMap[i]
            val workableSteps = stepsRemaining.filter {
                !workerMap.containsKey(it) && dependencyMap[it]?.sumBy { if (stepsCompleted.contains(it)) 0 else 1} == 0
            }.toSortedSet()
            //println(dependencyMap)
            if (!stepInProgress.isNullOrEmpty() or workableSteps.isNotEmpty()) {
                val step = stepInProgress ?: workableSteps.first()
                print("$step   ")
                workerMap[step] = i
                workingMap[i] = step
                timeMap[step] = timeMap[step]!!.minus(1)
            }
        }

        seconds ++
        print("$stepsCompleted\n")
        timeMap.forEach{
            if (it.value == 0) {
                stepsRemaining.remove(it.key)
                stepsCompleted.add(it.key)
                workerMap.remove(it.key)
                workingMap = workingMap.filterNot { workingMap ->
                    workingMap.value == it.key
                }.toMutableMap()
            }
        }
    }
    print(seconds)

}
