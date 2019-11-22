import java.io.File

// not 11 for part 2
// need 2 to hit 15 to hit instruction 16 (2 * 2 into 2)
fun main() {
    val ipValueRegex = Regex("^#ip ([0-9])$")
    val instructionRegex = Regex("([a-z]+) ([0-9]+) ([0-9]+) ([0-9]+)")
    var instructionPointer = -1
    val registers = mutableListOf(0, 0, 0, 0, 0, 0)
    val instructions = mutableListOf<Pair<Instruction, Array<Int>>>()
    File("src/input/day19-input.txt").forEachLine {
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
    while (instructionPointer < instructions.size) {
        registers[boundRegister] = instructionPointer
        //print("ip=$instructionPointer $registers ")
        val instructionType = instructions[instructionPointer].first
        val (A, B, C) = instructions[instructionPointer].second
        registers[C] = instructionType.compute(registers, A, B, C)
        instructionPointer = registers[boundRegister]
        instructionPointer++
        //println("$instructionType $A $B $C $registers")
    }
    println("The current value of register 0 is ${registers[0]}")
    val bigRegister: Int = 10551387
    println("The sum of factors of 10551387 is ${bigRegister.factors().sum()}")
}

private fun Int.factors() : List<Int> =
    (1..this).mapNotNull {
        if (this % it == 0) it else null
    }

