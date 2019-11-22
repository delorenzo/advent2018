import java.io.File
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import java.math.BigInteger
import java.util.*
import java.util.regex.Pattern
import kotlin.system.exitProcess

fun main() {
    val before = Regex("Before: \\[(\\d+), (\\d+), (\\d+), (\\d+)\\]")
    val line = Regex("(\\d+) (\\d+) (\\d+) (\\d+)")
    val after = Regex("After:  \\[(\\d+), (\\d+), (\\d+), (\\d+)\\]")
    val beforeToken = Pattern.compile("Before:")
    var result = 0
    val opcodeMap = mutableMapOf<Int, Set<Instruction>>()
    val opcodes = arrayOfNulls<Instruction>(16)
    val scanner = Scanner(File("src/input/day16-input.txt"))
    while (scanner.hasNext(beforeToken)) {
        val beforeRegister = before.matchEntire(scanner.nextLine())?.groupValues?.takeLast(4)?.map { it.toInt()}.orEmpty()
        val (opcode, A, B, C) = line.matchEntire(scanner.nextLine())?.groupValues?.takeLast(4)?.map { it.toInt() }.orEmpty()
        val afterRegister = after.matchEntire(scanner.nextLine())?.groupValues?.takeLast(4)?.map{ it.toInt()}.orEmpty()
        scanner.nextLine()

        var instructions = 0
        Instruction.values().forEach { instruction ->
            val instructionSet = mutableSetOf<Instruction>()
            if (instruction.compute(beforeRegister, A, B, C) == afterRegister[C]) {
                instructions++
                instructionSet.add(instruction)
            }
            opcodeMap[opcode] = opcodeMap[opcode]?.plus(instructionSet) ?: instructionSet
        }

        if (instructions == 1) {
            opcodes[opcode] = opcodeMap[opcode]!!.first()
        }
        if (instructions >= 3) result++
    }
    println("Result:  $result")
    while(opcodes.contains(null)) {
        opcodeMap.forEach {entry ->
            if (entry.value.size > 1) {
                val newSet = entry.value.filter { instruction -> !opcodes.contains(instruction) }.toSet()
                if (newSet.size == 1) {
                    opcodes[entry.key] = newSet.first()
                }
                opcodeMap[entry.key] = newSet
            }
        }
    }
    println(opcodes.toList())

    scanner.nextLine()
    scanner.nextLine()
    val registers = mutableListOf(0, 0, 0, 0)
    while (scanner.hasNextLine()) {
        val (opCode, A, B, C) = line.matchEntire(scanner.nextLine())?.groupValues?.takeLast(4)?.map { it.toInt() }.orEmpty()
        registers[C] = opcodes[opCode]?.compute(registers, A, B, C) ?: throw IllegalStateException("Opcodes should be non null")
    }
    println("the value in register 0 is ${registers.first()}")
    scanner.close()
}

enum class Instruction {
    ADDR {
        override fun compute(before: List<Int>, A: Int, B: Int, C: Int): Int {
            return before[A] + before[B]
        }

        override fun compute(before: List<BigInteger>, A: BigInteger, B: BigInteger, C: BigInteger): BigInteger {
            return before[A.intValueExact()].add(before[B.intValueExact()])
        }
    },
    ADDI {
        override fun compute(before: List<Int>, A: Int, B: Int, C: Int): Int {
            return before[A] + B
        }

        override fun compute(before: List<BigInteger>, A: BigInteger, B: BigInteger, C: BigInteger): BigInteger {
            return before[A.intValueExact()].add(B)
        }
    },
    MULR {
        override fun compute(before: List<Int>, A: Int, B: Int, C: Int): Int {
            return before[A] * before[B]
        }

        override fun compute(before: List<BigInteger>, A: BigInteger, B: BigInteger, C: BigInteger): BigInteger {
            return before[A.intValueExact()].times(before[B.intValueExact()])
        }
    },
    MULI {
        override fun compute(before: List<Int>, A: Int, B: Int, C: Int): Int {
            return before[A] * B
        }

        override fun compute(before: List<BigInteger>, A: BigInteger, B: BigInteger, C: BigInteger): BigInteger {
            return before[A.intValueExact()].times(B)
        }
    },
    BANR {
        override fun compute(before: List<Int>, A: Int, B: Int, C: Int): Int {
            return before[A] and before[B]
        }

        override fun compute(before: List<BigInteger>, A: BigInteger, B: BigInteger, C: BigInteger): BigInteger {
            return before[A.intValueExact()].and(before[B.intValueExact()])
        }
    },
    BANI {
        override fun compute(before: List<Int>, A: Int, B: Int, C: Int): Int {
            return before[A] and B
        }

        override fun compute(before: List<BigInteger>, A: BigInteger, B: BigInteger, C: BigInteger): BigInteger {
            return before[A.intValueExact()].and(B)
        }
    },
    BORR {
        override fun compute(before: List<Int>, A: Int, B: Int, C: Int): Int {
            return before[A] or before[B]
        }

        override fun compute(before: List<BigInteger>, A: BigInteger, B: BigInteger, C: BigInteger): BigInteger {
            return before[A.intValueExact()].or(before[B.intValueExact()])
        }
    },
    BORI {
        override fun compute(before: List<Int>, A: Int, B: Int, C: Int): Int {
            return before[A] or B
        }

        override fun compute(before: List<BigInteger>, A: BigInteger, B: BigInteger, C: BigInteger): BigInteger {
            return before[A.intValueExact()].or(B)
        }
    },
    SETR {
        override fun compute(before: List<Int>, A: Int, B: Int, C: Int): Int {
            return before[A]
        }

        override fun compute(before: List<BigInteger>, A: BigInteger, B: BigInteger, C: BigInteger): BigInteger {
            return before[A.intValueExact()]
        }
    },
    SETI {
        override fun compute(before: List<Int>, A: Int, B: Int, C: Int): Int {
            return  A
        }

        override fun compute(before: List<BigInteger>, A: BigInteger, B: BigInteger, C: BigInteger): BigInteger {
            return A
        }
    },
    GTIR {
        override fun compute(before: List<Int>, A: Int, B: Int, C: Int): Int {
            return if (A > before[B]) 1 else 0
        }

        override fun compute(before: List<BigInteger>, A: BigInteger, B: BigInteger, C: BigInteger): BigInteger {
            return A.compareTo(before[B.intValueExact()]).coerceAtLeast(0).toBigInteger()
        }
    },
    GTRI {
        override fun compute(before: List<Int>, A: Int, B: Int, C: Int): Int {
            return if (before[A] > B) 1 else 0
        }

        override fun compute(before: List<BigInteger>, A: BigInteger, B: BigInteger, C: BigInteger): BigInteger {
            return before[A.intValueExact()].compareTo(B).coerceAtLeast(0).toBigInteger()
        }
    },
    GTRR {
        override fun compute(before: List<Int>, A: Int, B: Int, C: Int): Int {
            return if (before[A] > before[B]) 1 else 0
        }

        override fun compute(before: List<BigInteger>, A: BigInteger, B: BigInteger, C: BigInteger): BigInteger {
            return before[A.intValueExact()].compareTo(before[B.intValueExact()]).coerceAtLeast(0).toBigInteger()
        }
    },
    EQIR {
        override fun compute(before: List<Int>, A: Int, B: Int, C: Int): Int {
            return if (A == before[B]) 1 else 0
        }

        override fun compute(before: List<BigInteger>, A: BigInteger, B: BigInteger, C: BigInteger): BigInteger {
            return if (A == before[B.intValueExact()]) { BigInteger.ONE } else { BigInteger.ZERO }
        }
    },
    EQRI {
        override fun compute(before: List<Int>, A: Int, B: Int, C: Int): Int {
            return if (before[A] == B) 1 else 0
        }

        override fun compute(before: List<BigInteger>, A: BigInteger, B: BigInteger, C: BigInteger): BigInteger {
            return if (before[A.intValueExact()] == B) { BigInteger.ONE } else { BigInteger.ZERO }
        }
    },
    EQRR {
        override fun compute(before: List<Int>, A: Int, B: Int, C: Int): Int {
            return if (before[A] == before[B]) 1 else 0
        }

        override fun compute(before: List<BigInteger>, A: BigInteger, B: BigInteger, C: BigInteger): BigInteger {
            return if (before[A.intValueExact()] == before[B.intValueExact()]) { BigInteger.ONE } else { BigInteger.ZERO }
        }
    };

    abstract fun compute(before: List<Int>, A: Int, B: Int, C: Int) : Int
    abstract fun compute(before: List<BigInteger>, A: BigInteger, B: BigInteger, C: BigInteger) : BigInteger
}