package me.bscal.runecraft.stats.spells.targets

import me.bscal.runecraft.stats.SpellCastAction
import me.bscal.runecraft.stats.SpellTarget
import me.bscal.runecraft.stats.StatInstance
import net.axay.kspigot.data.NBTDataType
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity

class AreaTarget : SpellTarget
{
	override fun GetTargets(caster: Entity, instance: StatInstance): List<Entity>?
	{
		val radius = instance.additionalData["radius", NBTDataType.DOUBLE] ?: 0.0
		if (radius < 1.0) return null
		return caster.getNearbyEntities(radius, radius, radius)
	}
}