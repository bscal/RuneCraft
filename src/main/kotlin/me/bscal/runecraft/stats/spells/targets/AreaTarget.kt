package me.bscal.runecraft.stats.spells.targets

import me.bscal.runecraft.stats.SpellTarget
import me.bscal.runecraft.stats.StatInstance
import net.axay.kspigot.data.NBTDataType
import org.bukkit.entity.Entity

class AreaTarget : SpellTarget
{
	override fun GetTargets(caster: Entity, instance: StatInstance): List<Entity>?
	{
		val radius = instance.AdditionalData["radius", NBTDataType.DOUBLE] ?: 0.0
		if (radius < 1.0) return null
		return caster.getNearbyEntities(radius, radius, radius)
	}
}