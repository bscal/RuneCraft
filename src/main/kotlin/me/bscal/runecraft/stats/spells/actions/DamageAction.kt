package me.bscal.runecraft.stats.spells.actions

import me.bscal.runecraft.stats.SpellCastAction
import me.bscal.runecraft.stats.StatInstance
import net.axay.kspigot.data.NBTDataType
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity

class DamageAction : SpellCastAction
{
	override fun OnCast(caster: Entity, instance: StatInstance, targets: List<Entity>?)
	{
		if (targets.isNullOrEmpty()) return

		val level = instance.AdditionalData["level", NBTDataType.INT] ?: 0
		val damage = instance.AdditionalData["damage", NBTDataType.DOUBLE] ?: 0.0
		val maxTargets = instance.AdditionalData["maxTargets", NBTDataType.INT] ?: 5
		if (level < 1 || damage < 1.0) return

		val totalTargets = maxTargets.coerceAtMost(targets.size)
		for (i in 0..totalTargets)
		{
			val target = targets[i]
			if (target is LivingEntity) target.damage(damage, caster)
		}
	}
}