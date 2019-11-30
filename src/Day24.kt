import java.io.File

private data class Army(val id: Int, var numUnits: Int, val side: Allegiance, val hitPoints: Int,
                        val initiative: Int, val damage: Int, val weakTo: Set<AttackType>,
                        val immuneTo: Set<AttackType>, val attackType: AttackType) {
    fun power() : Int {
        return numUnits * damage
    }
    var target: Army? = null
    fun damageFrom(other: Army) : Int {
        return when {
            weakTo.contains(other.attackType) -> other.power() * 2
            immuneTo.contains(other.attackType) -> 0
            else -> other.power()
        }
    }
}

private enum class Allegiance {
    INFECTION,
    IMMUNE_SYSTEM
}

private enum class AttackType {
    BLUDGEONING,
    SLASHING,
    FIRE,
    COLD,
    RADIATION,
}

private fun parse() : List<Army> {
    var id = 0
    var currentSide: Allegiance = Allegiance.IMMUNE_SYSTEM
    val results = mutableListOf<Army>()
    val regex = Regex("([0-9]+) units each with ([0-9]+) hit points (\\(?((immune|weak) to ((fire|cold|bludgeoning|slashing|radiation),? ?)+)?;? ?((immune|weak) to ((fire|cold|bludgeoning|slashing|radiation),? ?)+)?\\)? )?with an attack that does ([0-9]+) (fire|cold|bludgeoning|slashing|radiation) damage at initiative ([0-9]+)")
    File("src/input/day24-input.txt").forEachLine {
        if (it.isBlank()) {
            //do nothing
        }
        else if (it.trim().startsWith("Immune System:")) {
            currentSide = Allegiance.IMMUNE_SYSTEM
        } else if (it.trim().startsWith("Infection:")) {
            currentSide = Allegiance.INFECTION
        } else {
            val army = regex.matchEntire(it)
            var groups = army?.groupValues
            if (groups != null) {

                groups = groups.filterNot { group -> group == "" }
                val units = groups[1].toInt()
                val hp = groups[2].toInt()
                groups = groups.drop(3)
                if (groups.first().startsWith("(")) {
                    groups = groups.drop(1)
                }
                val weaknesses = mutableSetOf<AttackType>()
                val immunities = mutableSetOf<AttackType>()
                // weak to and immune to appear in different orders, which is annoying so just do each one twice
                if (groups.first().startsWith("weak")) {
                    val weak = groups[0].removePrefix("weak to ")
                    val weaknessStrings = weak.split(", ")
                    weaknesses.addAll(weaknessStrings.map { string -> AttackType.valueOf(string.toUpperCase()) })
                    groups = groups.drop(4)
                }
                if (groups.first().startsWith("immune")) {
                    val immune = groups[0].removePrefix("immune to ")
                    val immuneStrings = immune.split(", ")
                    immunities.addAll(immuneStrings.map { string -> AttackType.valueOf(string.toUpperCase()) })
                    groups = groups.drop(4)
                }
                if (groups.first().startsWith("weak")) {
                    val weak = groups[0].removePrefix("weak to ")
                    val weaknessStrings = weak.split(", ")
                    weaknesses.addAll(weaknessStrings.map { string -> AttackType.valueOf(string.toUpperCase()) })
                    groups = groups.drop(4)
                }
                if (groups.first().startsWith("immune")) {
                    val immune = groups[0].removePrefix("immune to ")
                    val immuneStrings = immune.split(", ")
                    immunities.addAll(immuneStrings.map { string -> AttackType.valueOf(string.toUpperCase()) })
                    groups = groups.drop(4)
                }
                val damage = groups[0].toInt()
                val attackType = AttackType.valueOf(groups[1].toUpperCase())
                val initiative = groups[2].toInt()

                results.add(
                    Army(
                        numUnits = units, hitPoints = hp, weakTo = weaknesses, immuneTo = immunities,
                        damage = damage, attackType = attackType, initiative = initiative, side = currentSide,
                        id = id
                    )
                )
                id++
            }
        }
    }
    return results
}

fun main() {
    var armies = parse()
    var numImmune = armies.partition { it.side == Allegiance.IMMUNE_SYSTEM }
    while(numImmune.first.isNotEmpty() && numImmune.second.isNotEmpty()) {
        armies = fight(armies)
        numImmune = armies.partition { it.side == Allegiance.IMMUNE_SYSTEM }
    }

    println(numImmune.first.sumBy { it.numUnits })
    println(numImmune.second.sumBy { it.numUnits })
}

private fun fight(armies: List<Army>) : List<Army> {
    targetSelection(armies)
    return attacking(armies)
}

//each group attempts to choose a target
// in decreasing order of effective power groups choose their targets
// in a tie the group with higher initiative goes first
private fun targetSelection(armies : List<Army>) {
    val targeted = mutableMapOf<Int, Boolean>().withDefault { false }
    val sortedArmies = armies.sortedWith(compareByDescending<Army> { it.power() }.thenByDescending { it.initiative })
    for (army in sortedArmies) {
        army.target = sortedArmies
            .filter { it.side != army.side && !targeted.getOrDefault(it.id, false) && it.damageFrom(army) > 0 }
            .sortedWith(compareByDescending<Army> {it.damageFrom(army)}
                .thenByDescending { it.power() }
                .thenByDescending { it.initiative })
            .firstOrNull()
        //println("${army.side} #${army.id} will attack ${army.target} with ${army.target?.damageFrom(army)}")
        army.target?.let {target ->
            targeted[target.id] = true
        }
    }
}

private fun attacking (armies: List<Army>) : List<Army> {
    val sortedArmies = armies.sortedWith(compareByDescending{it.initiative } )
    for (army in sortedArmies) {
        if (army.numUnits == 0) continue
        army.target?.let {
            val damage = it.damageFrom(army)
            val deaths = damage / it.hitPoints
            it.numUnits = maxOf(0, it.numUnits - deaths)
            //println("${army.side} #${army.id} killed $deaths units with $damage")
        }
    }
    return sortedArmies.filterNot { it.numUnits == 0 }
}