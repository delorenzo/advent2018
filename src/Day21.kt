import java.io.File

// not 11 for part 2
// need 2 to hit 15 to hit instruction 16 (2 * 2 into 2)
fun main() {
    val ipValueRegex = Regex("^#ip ([0-9])$")
    val instructionRegex = Regex("([a-z]+) ([0-9]+) ([0-9]+) ([0-9]+)")
    var instructionPointer = -1
    val initialValue = 0
    val registers = mutableListOf(initialValue, 0, 0, 0, 0, 0)
    val instructions = mutableListOf<Pair<Instruction, Array<Int>>>()
    File("src/input/day21-input.txt").forEachLine {
        if (instructionPointer == -1) {
            instructionPointer = ipValueRegex.matchEntire(it)?.groupValues?.last()?.toInt()!!
        } else {
            val instructionValues = instructionRegex.matchEntire(it)?.groupValues!!
            val instruction = instructionValues[1].toUpperCase()
            val (A, B, C) = instructionValues.takeLast(3).map { num -> num.toInt() }

            val instructionType = Instruction.valueOf(instruction)
            instructions.add(Pair(instructionType, arrayOf(A, B, C)))
        }
    }
    val boundRegister = instructionPointer
    instructionPointer = 0
    var instructionsExecuted = 0L
    var firstRegisterFiveValues = mutableSetOf<Int>()
    var firstRegisterFiveValueCountPairs = mutableSetOf<Pair<Int,Long>>()

    while (instructionPointer < instructions.size) {
        registers[boundRegister] = instructionPointer
        val instructionType = instructions[instructionPointer].first
        val (A, B, C) = instructions[instructionPointer].second
        registers[C] = instructionType.compute(registers, A, B, C)
        instructionPointer = registers[boundRegister]
        instructionPointer++
        instructionsExecuted++
        if (instructionType == Instruction.EQRR && B == 0) {
            if (!firstRegisterFiveValues.contains(registers[5])) {
                firstRegisterFiveValues.add(registers[5])
                firstRegisterFiveValueCountPairs.add(registers[5] to instructionsExecuted)
            }

        }
        if (instructionsExecuted == 10000000000L) break
    }
    println("Most instructions executed:")
    val sortedPairs = firstRegisterFiveValueCountPairs.sortedByDescending { it.second }
    sortedPairs.take(5).forEach {
        println("Value :  ${it.first}  count:  ${it.second}")
    }
    println("Least instructions executed:")
    sortedPairs.takeLast(5).forEach {
        println("Value :  ${it.first}  count:  ${it.second}")
    }
}

