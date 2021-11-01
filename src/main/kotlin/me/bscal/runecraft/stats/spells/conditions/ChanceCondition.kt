package me.bscal.runecraft.stats.spells.conditions

import me.bscal.runecraft.stats.SpellCondition
import me.bscal.runecraft.stats.StatInstance
import net.axay.kspigot.data.NBTDataType
import org.bukkit.entity.Entity
import kotlin.random.Random

class ChanceCondition : SpellCondition
{
	override fun CanCast(caster: Entity, instance: StatInstance): Boolean
	{
		val chance: Double = instance.additionalData["chance", NBTDataType.DOUBLE] ?: 0.0
		if (chance <= 0.0) return false
		return chance > Random.nextDouble()
	}
}